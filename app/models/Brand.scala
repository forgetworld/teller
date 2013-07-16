package models

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB.withSession
import play.api.Play.current

/**
 * A person, such as the owner or employee of an organisation.
 */
case class Brand(id: Option[Long], code: String, name: String, coordinatorId: Long)

case class BrandView(name: String, coordinator: Person)

object Brand {

  /**
   * Returns true if and only if there is a brand with the given code.
   */
  def exists(code: String): Boolean = withSession { implicit session ⇒
    Query(Query(Brands).filter(_.code === code).exists).first
  }

  def findAll: List[BrandView] = withSession { implicit session ⇒
    val query = for {
      brand ← Brands
      coordinator ← brand.coordinator
    } yield (brand.name, coordinator.*)
    query.sortBy(_._1).list map {
      case (name, coordinator) ⇒
        BrandView(name, coordinator)
    }
  }
}

object Brands extends Table[Brand]("BRAND") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def code = column[String]("CODE")
  def name = column[String]("NAME")
  def coordinatorId = column[Long]("COORDINATOR_ID")

  def coordinator = foreignKey("COORDINATOR_FK", coordinatorId, People)(_.id)

  def * = id.? ~ code ~ name ~ coordinatorId <> (Brand.apply _, Brand.unapply _)
}