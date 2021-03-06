/*
 * Happy Melly Teller
 * Copyright (C) 2013 - 2016, Happy Melly http://www.happymelly.com
 *
 * This file is part of the Happy Melly Teller.
 *
 * Happy Melly Teller is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Happy Melly Teller is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Happy Melly Teller.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you have questions concerning this license or the applicable additional
 * terms, you may contact by email Sergey Kotlov, sergey.kotlov@happymelly.com or
 * in writing Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */
package controllers.admin

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers.Security
import models.UserRole.Role
import models._
import models.cm.{Event, License}
import models.repository.Repositories
import org.joda.time.LocalDate
import play.api.i18n.MessagesApi
import services.TellerRuntimeEnvironment
import views.Countries

import scala.concurrent.Future

/**
  * Pages for calculating the usage of Teller by brands
  */
class Usage @javax.inject.Inject() (override implicit val env: TellerRuntimeEnvironment,
                                    override val messagesApi: MessagesApi,
                                    val repos: Repositories,
                                    deadbolt: DeadboltActions,
                                    handlers: HandlerCache,
                                    actionBuilder: ActionBuilders)
  extends Security(deadbolt, handlers, actionBuilder, repos)(messagesApi, env) {

  case class StatByCountry(stat: Map[String, List[(LocalDate, Int)]])
  case class Item(brand: Long, month: LocalDate, fee: Float)

  /**
    * Renders Teller usage fee for brands
    */
  def index() = RestrictedAction(Role.Admin) { implicit request => implicit handler => implicit user =>
    val result = for {
      brands <- repos.cm.brand.findAll
      licenses <- licensesInChargeablePeriod() flatMap { licenses =>
        licenseStatsByCountry(licenses) map { stats =>
          licenseUsageByMonth(stats)
        }
      }
      events <- eventUsageByMonth(eventStatsByCountry(eventsInChargeablePeriod()))
    } yield (brands, licenses, events)
    result flatMap { case (brands, licenses, events) =>
      val fees = brandUsageFeeByMonth(events, licenses).map { byBrand =>
        (byBrand._1, brandName(brands, byBrand._1), byBrand._2)
      }
      ok(views.html.v2.usage.index(user, fees))
    }
  }

  protected def brandName(brands: List[Brand], identifier: Long): String =
    brands.find(_.identifier == identifier).map(_.name).getOrElse("")

  /**
    * Calculates usage fee for brand
    *
    * @param events Event usage fees per brand per month
    * @param licenses License usage fees per brand per month
    */
  protected def brandUsageFeeByMonth(events: List[Item], licenses: List[Item]) = {
    val fees = events.map(x => (x.brand, x.month, x.fee, true)) ::: licenses.map(x => (x.brand, x.month, x.fee, false))
    fees.groupBy(_._1).map { byBrand =>
      val usage = byBrand._2.groupBy(_._2).map { byMonth =>
        (byMonth._1, byMonth._2.filter(_._4).map(_._3).sum, byMonth._2.filterNot(_._4).map(_._3).sum)
      }.toList.sortBy(_._1.toString)(Ordering[String].reverse).map(x => (x._1.toString("MMMM yyyy"), x._2, x._3))
      (byBrand._1, usage)
    }
  }

  /**
    * Returns events with calculated usage fee
    *
    * @param byLocation Events grouped by country
    * @return
    */
  protected def calculateEventFee(byLocation: (String, List[Event])): List[(Event, Float)] = {
    val fee = countryBasedEventFees(byLocation._1)
    byLocation._2.map(event => (event, event.schedule.totalHours * fee))
  }

  protected def chargeablePeriod(): (LocalDate, LocalDate) =
    (LocalDate.parse("2015-09-30"), LocalDate.now().plusMonths(1).withDayOfMonth(1))

  /**
    * Returns system fee per event per hour
    *
    * @param code Country code
    */
  protected def countryBasedEventFees(code: String): Float = {
    Countries.gdp.get(code) map { category ⇒
      category match {
        case 1 => 3.25f
        case 2 => 2.75f
        case 3 => 2.00f
        case 4 => 1.25f
        case _ => 0.75f
      }
    } getOrElse 0.75f
  }

  /**
    * Returns system fee per facilitator per month
    *
    * @param code Country code
    */
  protected def countryBasedFacilitatorFees(code: String): Float = {
    Countries.gdp.get(code) map { category ⇒
      category match {
        case 1 => 3.45f
        case 2 => 2.95f
        case 3 => 2.45f
        case 4 => 1.45f
        case _ => 0.95f
      }
    } getOrElse 0.95f
  }

  /**
    * Returns events valid in a chargeable period with tweaked start date
    */
  protected def eventsInChargeablePeriod(): Future[List[Event]] =
    repos.cm.event.findAll map { events =>
      filterEventsByDate(events).map { event =>
        val start = event.schedule.start.withDayOfMonth(1)
        event.copy(schedule = event.schedule.copy(start = start))
      }
    }

  /**
    * Returns event usage fee per country
    *
    * @param value Events
    */
  protected def eventStatsByCountry(value: Future[List[Event]]) = value map { events =>
    events.groupBy(_.location.countryCode).flatMap { byLocation =>
      calculateEventFee(byLocation)
    }
  }

  /** Calculates usage fee for events per month **/
  protected def eventUsageByMonth(value: Future[Map[Event, Float]]): Future[List[Item]] =
    value map { events =>
      events.groupBy(_._1.brandId).flatMap { byBrand =>
        byBrand._2.groupBy(_._1.schedule.start).map { byMonth =>
          Item(byBrand._1, byMonth._1, byMonth._2.aggregate(0.0f)(_ + _._2, _ + _))
        }
      }.toList
  }

  /**
    * Returns only events in a chargeable period: from Oct 2015 to the end of current month
    *
    * @param events Events
    */
  protected def filterEventsByDate(events: List[Event]): List[Event] = {
    val (start, end) = chargeablePeriod()
    events.filter(event => event.schedule.start.isAfter(start) && event.schedule.start.isBefore(end))
  }

  /**
    * Returns only licenses valid in a chargeable period: from Oct 2015 to the end of current month
    *
    * @param licenses Licenses
    */
  protected def filterLicensesByDate(licenses: List[License]): List[License] = {
    val (start, end) = chargeablePeriod()
    licenses.filterNot(license => license.end.isBefore(start) || license.start.isAfter(end))
  }

  /**
    * Returns licenses valid in a chargeable period with tweaked start/end dates
    */
  protected def licensesInChargeablePeriod(): Future[List[License]] =
    repos.cm.license.findAll map { licenses =>
      filterLicensesByDate(licenses).sortBy(_.start.toString).map { license =>
        val start = license.start.withDayOfMonth(1)
        license.copy(start = start, end = start.plusMonths(1).minusDays(1))
      }
    }

  /**
    * Returns usage fee per country per month
    *
    * @param licenses Licenses
    */
  protected def licenseStatsByCountry(licenses: List[License]) = {
    val (start, _) = chargeablePeriod()
    val query = for {
      facilitators <- repos.person.find(licenses.map(_.licenseeId).distinct)
      _ <- repos.person.collection.addresses(facilitators)
    } yield facilitators
    query map { facilitators =>
      licenses.map { license =>
        (license, facilitators.find(_.identifier == license.licenseeId).map(_.address.countryCode).get)
      }.groupBy(_._1.brandId).map { byBrand =>
        val statsByLocation = byBrand._2.groupBy(_._2).map { byLocation =>
          (byLocation._1, License.numberPerMonth(byLocation._2.map(_._1)).filter(_._1.isAfter(start)))
        }
        (byBrand._1, StatByCountry(statsByLocation))
      }
    }
  }

  /**
    * Returns usage fee per brand per month
    *
    * @param licenses License data
    */
  protected def licenseUsageByMonth(licenses: Map[Long, StatByCountry]): List[Item] =
    licenses.flatMap { byBrand =>
      byBrand._2.stat.flatMap { byCountry =>
        val fee = countryBasedFacilitatorFees(byCountry._1)
        byCountry._2.map(data => (byCountry._1, data._1, data._2 * fee))
      }.groupBy(_._2).map { byMonth =>
        Item(byBrand._1, byMonth._1, byMonth._2.aggregate(0.0f)(_ + _._3, _ + _))
      }
    }.toList
}
