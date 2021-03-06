/*
 * Happy Melly Teller
 * Copyright (C) 2013 - 2014, Happy Melly http://www.happymelly.com
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
 * If you have questions concerning this license or the applicable additional terms, you may contact
 * by email Sergey Kotlov, sergey.kotlov@happymelly.com or
 * in writing Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */
package models.cm

import models.cm.event.{EventCancellation, Attendee}
import models.{ActivityRecorder, Person, Activity}
import models.repository.{IRepositories, Repositories}
import org.joda.money.Money
import org.joda.time.{Days, LocalDate}
import views.Languages

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

/**
 * Contains schedule-related data
 *
 *   - when an event starts/ends
 *   - how many hours per day it takes
 *   - how many total hours it takes
 */
case class Schedule(start: LocalDate,
    end: LocalDate,
    hoursPerDay: Int,
    totalHours: Int) {

  lazy val formatted: String = views.ViewHelpersV2.dateInterval(start,end)

  /**
   * Returns true if number of total hours is inside a threshold for an allowed
   * number of total hours
   */
  def validateTotalHours: Boolean = {
    val days = math.abs(Days.daysBetween(start, end).getDays) + 1
    val idealHours = days * hoursPerDay
    val difference = (idealHours - totalHours) / idealHours.toFloat
    if (difference > 0.2)
      false
    else
      true
  }
}

case class Organizer(
  id: Long,
  webSite: Option[String],
  registrationPage: Option[String])

/**
 * Contains optional descriptive data
 */
case class Details(
  description: Option[String],
  specialAttention: Option[String])

/**
 * Contains location-related data
 */
case class Location(city: String, countryCode: String) {

  lazy val online: Boolean = countryCode == "00"

  lazy val countryName: String = views.Countries.name(countryCode)
}

/**
 * Contains language-related data
 */
case class Language(spoken: String, secondSpoken: Option[String], materials: Option[String])

case class EventView(event: Event, invoice: EventInvoice)

/** An event such as a Management 3.0 course or a DARE Festival */
case class Event(
    id: Option[Long],
    eventTypeId: Long,
    brandId: Long,
    title: String,
    language: Language,
    location: Location,
    details: Details,
    organizer: Organizer,
    schedule: Schedule,
    notPublic: Boolean = false,
    archived: Boolean = false,
    confirmed: Boolean = false,
    free: Boolean = false,
    followUp: Boolean = true,
    rating: Float = 0.0f,
    hashedId : String = Random.alphanumeric.take(64).mkString,
    publicPage: Boolean = false,
    postEventTemplate: Option[String] = None,
    fee: Option[Money] = None) extends ActivityRecorder {

  private var _facilitators: Option[List[Person]] = None
  private var _facilitatorIds: Option[List[Long]] = None

  def attendees(services: Repositories): List[Attendee] =
    Await.result(services.cm.rep.event.attendee.findByEvents(List(identifier)), 3.seconds).map(_._2).toList

  /**
    * Cancels the event
    *
    * @param facilitatorId Identifier of the facilitator who requested the action
    * @param reason Reason why the event is cancelled
    * @param participants Number of participants already registered to the event
    * @param details Details (emails, names) of registered participants
    */
  def cancel(facilitatorId: Long,
             reason: Option[String],
             participants: Option[Int],
             details: Option[String],
             repos: Repositories): Unit = {
    repos.cm.rep.brand.eventType.find(this.eventTypeId) map { types =>
      val eventType = types.map(_.name).getOrElse("")
      val cancellation = EventCancellation(None, this.brandId, facilitatorId,
        this.title, eventType, this.location.city, this.location.countryCode,
        this.schedule.start, this.schedule.end, this.free, reason, participants, details)
      repos.cm.rep.event.cancellation.insert(cancellation)
      repos.cm.event.delete(this.id.get)
    }
  }

  def current: Boolean = !future && !past

  def deletable(services: Repositories): Boolean = attendees(services).isEmpty

  /** Returns (and retrieves from db if needed) a list of facilitators */
  def facilitators(services: IRepositories): List[Person] = if (_facilitators.isEmpty) {
    val data = Await.result(services.cm.event.facilitators(identifier), 3.seconds)
    facilitators_=(data)
    data
  } else {
    _facilitators.get
  }

  def facilitators_=(facilitators: List[Person]): Unit = {
    _facilitators = Some(facilitators)
  }

  def facilitatorIds(services: IRepositories): List[Long] = if (_facilitatorIds.isEmpty) {
    val ids = Await.result(services.cm.event.facilitatorIds(identifier), 3.seconds)
    facilitatorIds_=(ids)
    _facilitatorIds.get
  } else {
    _facilitatorIds.get
  }

  def facilitatorIds_=(facilitatorIds: List[Long]): Unit = {
    _facilitatorIds = Some(facilitatorIds)
  }

  def future: Boolean = schedule.start.isAfter(LocalDate.now())

  /**
   * Returns identifier of the object
   */
  def identifier: Long = id.getOrElse(0)

  /**
   * Returns string identifier which can be understood by human
   *
   * For example, for object 'Person' human identifier is "[FirstName] [LastName]"
   */
  def humanIdentifier: String = title

  /**
   * Returns type of this object
   */
  def objectType: String = Activity.Type.Event

  val longTitle: String = {
    val printableTitle = if (title.length <= 70)
      title
    else
      title.substring(0, 70)
    printableTitle + " / " + location.city + " / " + schedule.start.toString
  }

  val materialsLanguage = Languages.all.get(language.materials.getOrElse("English"))

  def past: Boolean = schedule.end.isBefore(LocalDate.now())

  lazy val spokenLanguage: String = {
    val langs = Languages.all
    if (language.secondSpoken.isEmpty)
      langs.getOrElse(language.spoken, "")
    else
      langs.getOrElse(language.spoken, "") + " / " + langs.getOrElse(language.secondSpoken.get, "")
  }

  lazy val spokenLanguages: List[String] = {
    val langs = Languages.all
    if (language.secondSpoken.isEmpty)
      List(langs.getOrElse(language.spoken, ""))
    else
      List(langs.getOrElse(language.spoken, ""), langs.getOrElse(language.secondSpoken.get, ""))
  }

  def isFacilitator(personId: Long, services: IRepositories): Boolean = facilitatorIds(services).contains(personId)

  def pageUrl(relativeHashedUrl: String): Option[String] = if (publicPage)
    Some(controllers.Utilities.fullUrl(relativeHashedUrl))
  else
    organizer.webSite
}

object Event {

  /**
   * Returns new event with a fee calculated the given one and a number of hours
    *
    * @param event Source event
   * @param fee Country Fee for 1 hour
   * @param maxHours Maximum number of chargeable hours
   */
  def withFee(event: Event, fee: Money, maxHours: Int): Event = {
    val hours = scala.math.min(maxHours, event.schedule.totalHours)
    val eventFee = fee.multipliedBy(hours)
    event.copy(fee = Some(eventFee))
  }
}
