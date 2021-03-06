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
 * terms, you may contact by email Sergey Kotlov, sergey.kotlov@happymelly.com
 * or in writing
 * Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */
package models.repository.cm.event

import com.github.tototoshi.slick.MySQLJodaSupport._
import models.cm.event.{Attendee, AttendeeView}
import models.database.event.AttendeeTable
import models.database.{EvaluationTable, EventTable}
import play.api.Application
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by sery0ga on 04/01/16.
  */
class AttendeeRepository(app: Application)  extends HasDatabaseConfig[JdbcProfile]
  with AttendeeTable
  with EvaluationTable
  with EventTable {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](app)
  import driver.api._
  private val attendees = TableQuery[Attendees]

  /**
    * Deletes the given attendee from the database
    *
    * @param attendeeId Attendee identifier
    * @param eventId Event identifier
    */
  def delete(attendeeId: Long, eventId: Long): Future[Int] = {
    val action = attendees.filter(_.id === attendeeId).filter(_.eventId === eventId).delete
    db.run(action)
  }

  /**
    * Returns the given attendee if exists
    *
    * @param attendeeId Attendee identifier
    * @param eventId Event identifier
    * @return
    */
  def find(attendeeId: Long, eventId: Long): Future[Option[Attendee]] =
    db.run(attendees.filter(_.id === attendeeId).filter(_.eventId === eventId).result).map(_.headOption)

  /**
    * Find all participants for all events of the specified brand
    *
    * @param brandId Brand id
    * @return
    */
  def findByBrand(brandId: Option[Long]): Future[List[AttendeeView]] = {
    val baseQuery = for {
      ((part, e), ev) ← attendees join
        TableQuery[Events] on (_.eventId === _.id) joinLeft
        TableQuery[Evaluations] on (_._1.evaluationId === _.id)
    } yield (part, e, ev)

    val brandQuery = brandId.map { value ⇒ baseQuery.filter(_._2.brandId === value) }.getOrElse(baseQuery)
    db.run(brandQuery.result).map(_.toList.map(AttendeeView.tupled)).flatMap { list =>
      val withEvaluation = list.filterNot(view ⇒ view.evaluation.isEmpty).distinct
      val withoutEvaluation = list.filter(obj ⇒ obj.evaluation.isEmpty).
        map(obj ⇒ AttendeeView(obj.attendee, obj.event, None))
      Future.successful(withEvaluation.union(withoutEvaluation.distinct))
    }
  }

  /**
    * Returns attendees for the given set of events
    *
    * @param eventIds a list of event ids
    */
  def findByEvents(eventIds: List[Long]) = {
    val query = for {
      e ← TableQuery[Events] if e.id inSet eventIds
      a ← attendees if a.eventId === e.id
    } yield (e, a)
    db.run(query.result)
  }

  /**
    * Inserts new attendee to database
    *
    * @param attendee Attendee
    */
  def insert(attendee: Attendee): Future[Attendee] = {
    val query = attendees returning attendees.map(_.id) into ((value, id) => value.copy(id = Some(id)))
    db.run(query += attendee)
  }

  /**
    * Updates the given attendee in database
    *
    * @param attendee Attendee
    */
  def update(attendee: Attendee): Future[Int] = {
    val forUpdate = (attendee.personId, attendee.firstName, attendee.lastName, attendee.email, attendee.dateOfBirth,
      attendee.countryCode, attendee.city, attendee.street_1, attendee.street_2, attendee.province, attendee.postcode,
      attendee.role, attendee.recordInfo.updated, attendee.recordInfo.updatedBy)
    val query = attendees.filter(_.id === attendee.id).map(_.forUpdate)
    db.run(query.update(forUpdate))
  }

  /**
    * Updates certificates data for the given attendee
    *
    * @param attendee Attendee
    */
  def updateCertificate(attendee: Attendee) = {
    val forUpdate = (attendee.certificate, attendee.issued)
    db.run(attendees.filter(_.id === attendee.id).map(x => (x.certificate, x.issued)).update(forUpdate))
  }

  /**
    * Updates evaluation for the given attendee
    *
    * @param attendeeId Attendee identifier
    * @param evaluationId Evaluation identifier
    */
  def updateEvaluationIdQuery(attendeeId: Long, evaluationId: Option[Long]) =
    attendees.filter(_.id === attendeeId).map(_.evaluationId).update(evaluationId)
}
