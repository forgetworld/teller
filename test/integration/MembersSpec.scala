/*
* Happy Melly Teller
* Copyright (C) 2013 - 2015, Happy Melly http -> //www.happymelly.com
*
* This file is part of the Happy Melly Teller.
*
* Happy Melly Teller is free software ->  you can redistribute it and/or modify
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
* along with Happy Melly Teller.  If not, see <http -> //www.gnu.org/licenses/>.
*
* If you have questions concerning this license or the applicable additional
* terms, you may contact by email Sergey Kotlov, sergey.kotlov@happymelly.com
* or in writing Happy Melly One, Handelsplein 37, Rotterdam,
* The Netherlands, 3071 PR
*/
package integration

import java.math.RoundingMode

import controllers.{ Security, Members }
import models.{ Person, Organisation, Member }
import org.joda.money.{ CurrencyUnit, Money }
import org.joda.time.{ DateTime, LocalDate }
import org.specs2.mutable.After
import play.api.cache.Cache
import play.api.db.slick._
import play.api.mvc.SimpleResult
import play.api.Play.current
import stubs.{ StubLoginIdentity, FakeServices }

import scala.concurrent.Future
import scala.slick.jdbc.{ StaticQuery ⇒ Q, GetResult }
import scala.slick.session.Session

class TestMembers() extends Members with Security with FakeServices

class MembersSpec extends PlayAppSpec {
  def setupDb() {}
  def cleanupDb() {}

  implicit val getMemberResult = GetResult(r ⇒
    Member(r.<<, r.<<, r.<<, r.<<,
      Money.of(CurrencyUnit.of(r.nextString()), r.nextBigDecimal().bigDecimal, RoundingMode.DOWN),
      LocalDate.parse(r.nextString()), existingObject = false,
      DateTime.parse(r.nextString().replace(' ', 'T')), r.<<,
      DateTime.parse(r.nextString().replace(' ', 'T')), r.<<))

  val controller = new TestMembers()

  "While creating membership fee, objectId and id are unknown and a system" should {
    "reset them to None to prevent cheating" in new cleanDb {
      val m = member()
      val fakeId = 400
      val req = prepareSecuredPostRequest(StubLoginIdentity.editor, "/").
        withFormUrlEncodedBody(("id", fakeId.toString),
          ("objectId", "3"),
          ("person", "1"), ("funder", m.funder.toString),
          ("fee.currency", m.fee.getCurrencyUnit.toString),
          ("fee.amount", m.fee.getAmountMajorLong.toString),
          ("since", m.since.toString))
      val result: Future[SimpleResult] = controller.create().apply(req)

      status(result) must equalTo(SEE_OTHER)
      val insertedM = Cache.getAs[Member](Members.cacheId(1L))
      insertedM.nonEmpty must_== true
      insertedM.get.id must_!= fakeId
    }
  }

  "Incomplete member object" should {
    "be destroyed after successful creation of a person" in new cleanDb {
      val m = member()
      val oldList = Person.findAll
      val request = prepareSecuredPostRequest(StubLoginIdentity.editor, "/").
        withFormUrlEncodedBody(("emailAddress", "ttt@ttt.ru"),
          ("address.country", "RU"), ("firstName", "Test"),
          ("lastName", "Test"), ("signature", "false"),
          ("role", "0"))

      Cache.set(Members.cacheId(1L), m, 1800)
      controller.createNewPerson().apply(request)
      // test check
      Cache.getAs[Member](Members.cacheId(1L)).isEmpty must_== true

      Person.findAll.diff(oldList).headOption map { p ⇒
        Person.delete(p.id); success
      } getOrElse failure
    }

    "be destroyed after successful creation of an organisation" in new cleanDb {
      val m = member()
      val oldList = Organisation.findAll
      val req = prepareSecuredPostRequest(StubLoginIdentity.editor, "/").
        withFormUrlEncodedBody(("name", "Test"), ("country", "RU"))

      Cache.set(Members.cacheId(1L), m, 1800)
      controller.createNewOrganisation().apply(req)
      // test check
      Cache.getAs[Member](Members.cacheId(1L)).isEmpty must_== true

      Organisation.findAll.diff(oldList).headOption map { o ⇒
        Organisation.delete(o.id.get); success
      } getOrElse failure
    }

  }

  "The organisation created on step 2" should {
    "be connected with member data" in new cleanDb {
      val m = member()
      val oldList = Organisation.findAll
      val req = prepareSecuredPostRequest(StubLoginIdentity.editor, "/").
        withFormUrlEncodedBody(("name", "Test"), ("country", "RU"))

      Cache.set(Members.cacheId(1L), m, 1800)
      val result = controller.createNewOrganisation().apply(req)
      status(result) must equalTo(SEE_OTHER)
      //@TODO this should be moved to acceptance module
      headers(result).get("Location").nonEmpty must_== true
      headers(result).get("Location").get must_== "/members"

      Organisation.findAll.diff(oldList).headOption map { org ⇒
        val updatedM = retrieveMember(org.id.get.toString)
        updatedM.nonEmpty must_== true
        updatedM.get.objectId.nonEmpty must_== true
        updatedM.get.objectId must_== org.id
        //@TODO make a separate test for this
        updatedM.get.person must_== false

        // clean up. We don't need this organisation anymore
        Organisation.delete(org.id.get)
        success
      } getOrElse failure

    }
  }

  "The person created on step 2" should {
    "be connected with member data" in new cleanDb {
      val m = member()
      val oldList = Person.findAll
      val request = prepareSecuredPostRequest(StubLoginIdentity.editor, "/").
        withFormUrlEncodedBody(("emailAddress", "ttt@ttt.ru"),
          ("address.country", "RU"), ("firstName", "Test"),
          ("lastName", "Test"), ("signature", "false"),
          ("role", "0"))

      Cache.set(Members.cacheId(1L), m, 1800)
      val result = controller.createNewPerson().apply(request)
      status(result) must equalTo(SEE_OTHER)
      //@TODO this should be moved to acceptance module
      headers(result).get("Location").nonEmpty must_== true
      headers(result).get("Location").get must_== "/members"

      Person.findAll.diff(oldList).headOption map { person ⇒
        val updatedM = retrieveMember(person.id.toString)
        updatedM.nonEmpty must_== true
        updatedM.get.objectId.nonEmpty must_== true
        updatedM.get.objectId.get must_== person.id

        // clean up. We don't need this person anymore
        Person.delete(person.id)
        success
      } getOrElse failure
    }
  }

  /**
   * Retrieves member from database if it exists
   * @param id Id of related object
   * @return Member or None
   */
  private def retrieveMember(id: String) = DB.withSession {
    implicit session: Session ⇒
      val q = Q.queryNA[Member]("SELECT * FROM member WHERE OBJECT_ID = " + id)
      q.firstOption
  }

  private def member(): Member = {
    new Member(None, None, person = true, funder = false,
      Money.parse("EUR 100"), LocalDate.now(), existingObject = false,
      DateTime.now(), 1L, DateTime.now(), 1L)
  }
}

trait cleanDb extends After {
  def after = DB.withSession { implicit session: Session ⇒
    Q.updateNA("TRUNCATE `MEMBER`").execute
  }
}
