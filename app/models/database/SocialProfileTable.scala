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

package models.database

import models.{ProfileType, SocialProfile}
import slick.driver.JdbcProfile

private[models] trait SocialProfileTable {

  protected val driver: JdbcProfile
  import driver.api._

  /**
    * `SocialProfile` table mapping
    */
  class SocialProfiles(tag: Tag)
    extends Table[SocialProfile](tag, "SOCIAL_PROFILE") {

    implicit val profileTypeMapper = MappedColumnType.base[ProfileType.Value, Int](
      { objectType ⇒ objectType.id }, { id ⇒ ProfileType(id) })

    def objectId = column[Long]("OBJECT_ID")
    def objectType = column[ProfileType.Value]("OBJECT_TYPE")
    def twitterHandle = column[Option[String]]("TWITTER_HANDLE")
    def facebookUrl = column[Option[String]]("FACEBOOK_URL")
    def linkedInUrl = column[Option[String]]("LINKEDIN_URL")
    def googlePlusUrl = column[Option[String]]("GOOGLE_PLUS_URL")
    def skype = column[Option[String]]("SKYPE")
    def phone = column[Option[String]]("PHONE")
    def contactForm = column[Option[String]]("CONTACT_FORM")

    def * = (objectId, objectType, twitterHandle, facebookUrl,
      linkedInUrl, googlePlusUrl, skype, phone,
      contactForm) <>(SocialProfile.tupled, SocialProfile.unapply)

  }

  object SocialProfilesStatic {

    implicit val profileTypeMapper = MappedColumnType.base[ProfileType.Value, Int](
      { objectType ⇒ objectType.id }, { id ⇒ ProfileType(id) })

  }

}