package models.database

import com.github.tototoshi.slick.JodaSupport._
import models.{ Address, Person }
import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._

/**
 * `Organisation` database table mapping.
 */
private[models] object People extends Table[Person]("PERSON") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def firstName = column[String]("FIRST_NAME")
  def lastName = column[String]("LAST_NAME")
  def emailAddress = column[String]("EMAIL_ADDRESS")

  def addressId = column[Long]("ADDRESS_ID")

  def bio = column[Option[String]]("BIO", O.DBType("TEXT"))
  def interests = column[Option[String]]("INTERESTS", O.DBType("TEXT"))

  def twitterHandle = column[Option[String]]("TWITTER_HANDLE")
  def facebookUrl = column[Option[String]]("FACEBOOK_URL")
  def linkedInUrl = column[Option[String]]("LINKEDIN_URL")
  def googlePlusUrl = column[Option[String]]("GOOGLE_PLUS_URL")
  def boardMember = column[Boolean]("BOARD_MEMBER")
  def stakeholder = column[Boolean]("STAKEHOLDER")
  def active = column[Boolean]("ACTIVE")

  def created = column[DateTime]("CREATED")
  def createdBy = column[String]("CREATED_BY")
  def updated = column[DateTime]("UPDATED")
  def updatedBy = column[String]("UPDATED_BY")

  def address = foreignKey("ADDRESS_FK", addressId, Addresses)(_.id)

  // Note that this projection does not include the address, which must be joined in queries.
  def * = id.? ~ firstName ~ lastName ~ emailAddress ~ addressId ~ bio ~ interests ~ twitterHandle ~ facebookUrl ~
    linkedInUrl ~ googlePlusUrl ~ boardMember ~ stakeholder ~ active ~ created ~ createdBy ~ updated ~ updatedBy <> (
      { p ⇒ Person(p._1, p._2, p._3, p._4, Address.find(p._5), p._6, p._7, p._8, p._9, p._10, p._11, p._12, p._13, p._14, p._15, p._16, p._17, p._18) },
      { (p: Person) ⇒
        Some((p.id, p.firstName, p.lastName, p.emailAddress, p.address.id.get, p.bio, p.interests, p.twitterHandle, p.facebookUrl,
          p.linkedInUrl, p.googlePlusUrl, p.boardMember, p.stakeholder, p.active, p.created, p.createdBy, p.updated, p.updatedBy))
      })

  def forInsert = * returning id

  def forUpdate = firstName ~ lastName ~ emailAddress ~ bio ~ interests ~ twitterHandle ~ facebookUrl ~ linkedInUrl ~
    googlePlusUrl ~ boardMember ~ stakeholder ~ updated ~ updatedBy
}