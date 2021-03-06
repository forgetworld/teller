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

package models.database.brand

import models.cm.brand.EventType
import models.database.BrandTable
import slick.driver.JdbcProfile

private[models] trait EventTypeTable extends BrandTable {

  protected val driver: JdbcProfile
  import driver.api._

  /**
    * `EventType` database table mapping.
    */
  class EventTypes(tag: Tag) extends Table[EventType](tag, "EVENT_TYPE") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def brandId = column[Long]("BRAND_ID")
    def name = column[String]("NAME")
    def defaultTitle = column[Option[String]]("DEFAULT_TITLE")
    def maxHours = column[Int]("MAX_HOURS")
    def free = column[Boolean]("FREE")

    def brand = foreignKey("EVENT_BRAND_FK", brandId, TableQuery[Brands])(_.id)

    def * = (id.?, brandId, name, defaultTitle,
      maxHours, free) <>(EventType.tupled, EventType.unapply)

    def forUpdate = (name, defaultTitle, maxHours, free)
  }

}