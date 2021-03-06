/*
 * Happy Melly Teller
 * Copyright (C) 2013 - 2015, Happy Melly http://www.happymelly.com
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
package controllers.cm.brand

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers._
import models.DateStamp
import models.cm.brand.Badge
import models.repository.Repositories
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.libs.Crypto
import play.twirl.api.Html
import services.TellerRuntimeEnvironment

class Badges @javax.inject.Inject() (override implicit val env: TellerRuntimeEnvironment,
                                     override val messagesApi: MessagesApi,
                                     val repos: Repositories,
                                     deadbolt: DeadboltActions, handlers: HandlerCache, actionBuilder: ActionBuilders)
  extends Security(deadbolt, handlers, actionBuilder, repos)(messagesApi, env)
  with Files {

  def form(editorName: String) = Form(mapping(
    "id" -> ignored(Option.empty[Long]),
    "brandId" -> ignored(0L),
    "name" -> nonEmptyText,
    "hash" -> ignored("dummyvalue"),
    "recordInfo" -> mapping(
      "created" -> ignored(DateTime.now()),
      "createdBy" -> ignored(editorName),
      "updated" -> ignored(DateTime.now()),
      "updatedBy" -> ignored(editorName))(DateStamp.apply)(DateStamp.unapply)
    )(Badge.apply)(Badge.unapply))

  /**
   * Renders add form for a badge
   *
   * @param brandId Brand identifier
   */
  def add(brandId: Long) = BrandAction(brandId) { implicit request ⇒ implicit handler ⇒ implicit user ⇒
    ok(views.html.v2.badge.form(user, brandId, form(user.name)))
  }

  /**
   * Creates new badge for the given brand
   *
   * @param brandId Brand identifier
   */
  def create(brandId: Long) = BrandAction(brandId) { implicit request ⇒ implicit handler ⇒ implicit user ⇒
    val formWithData = form(user.name).bindFromRequest
    formWithData.fold(
      error ⇒ badRequest(views.html.v2.badge.form(user, brandId, error)),
      badge ⇒ {
        val hash = generatedName
        uploadImage(Badge.picture(hash), "file") flatMap { _ ⇒
          repos.cm.rep.brand.badge.insert(badge.copy(brandId = brandId, file = hash)) map { badge =>
            Redirect(controllers.routes.Brands.details(brandId).url + "#badges")
          }
        } recover {
          case e: RuntimeException ⇒
            val error = formWithData.withGlobalError(e.getMessage)
            BadRequest(views.html.v2.badge.form(user, brandId, error))
        }
      })
  }

  /**
   * Deletes the given badge
   *
   * @param brandId Brand identifier
   * @param id Badge identifier
   */
  def delete(brandId: Long, id: Long) = BrandAction(brandId) {
    implicit request ⇒ implicit handler ⇒ implicit user ⇒
      repos.cm.rep.brand.badge.delete(brandId, id) flatMap { _ =>
        jsonSuccess("ok")
      }
  }

  /**
   * Renders an edit form for the given badge
   * Brand identifier is used to check access rights
   *
   * @param brandId Brand identifier
   * @param id Badge identifier
   */
  def edit(brandId: Long, id: Long) = BrandAction(brandId) { implicit request ⇒
    implicit handler ⇒ implicit user ⇒
      repos.cm.rep.brand.badge.find(id) flatMap {
        case None => notFound(Html("Badge not found"))
        case Some(badge) => ok(views.html.v2.badge.form(user, brandId, form(user.name).fill(badge), Some(id)))
      }
  }

  /**
   * Retrieve and cache a picture of the given badge
   *
   * @param hash Badge hash
   */
  def picture(hash: String, size: String = "") = file(Badge.picture(hash).file(size))

  /**
    * Renders badge settings page for the given brand
    * @param brandId Brand identifier
    */
  def settings(brandId: Long) = BrandAction(brandId) { implicit request ⇒ implicit handler ⇒ implicit user ⇒
    repos.cm.rep.brand.badge.findByBrand(brandId) flatMap { badges =>
      ok(views.html.v2.brand.tabs.badges(brandId, badges.sortBy(_.name)))
    }
  }

  /**
   * Updates the given badge if it's valid
   *
   * @param brandId Member identifier
   * @param id Badge identifier
   */
  def update(brandId: Long, id: Long) = BrandAction(brandId) { implicit request ⇒
    implicit handler ⇒ implicit user ⇒
      val formWithData = form(user.name).bindFromRequest
      formWithData.fold(
        error ⇒ badRequest(views.html.v2.badge.form(user, brandId, error)),
        badge ⇒
          repos.cm.rep.brand.badge.find(id) flatMap {
            case None => notFound(Html("Badge not found"))
            case Some(existing) ⇒
              if (existing.brandId == brandId) {
                val hash = generatedName
                uploadImage(Badge.picture(hash), "file") flatMap { _ ⇒
                  repos.cm.rep.brand.badge.update(badge.copy(id = Some(id), file = hash)) map { _ =>
                    Badge.picture(existing.file).remove()
                    Redirect(controllers.routes.Brands.details(brandId) + "#badges")
                  }
                } recover {
                  case e: RuntimeException ⇒
                    repos.cm.rep.brand.badge.update(badge.copy(id = Some(id), file = existing.file))
                    Redirect(controllers.routes.Brands.details(brandId) + "#badges")
                }
              } else {
                notFound(Html("Badge not found"))
              }
        })
  }

  protected def generatedName = Crypto.sign(DateTime.now().toString) + ".jpg"
}

object Badges {

  /**
    * Returns url to an badge's picture
    *
    * @param badge Badge
    */
  def pictureUrl(badge: Badge, size: String = ""): String = {
    val picture = Badge.picture(badge.file).file(size)
    Utilities.cdnUrl(picture.name).getOrElse(Utilities.fullUrl(controllers.cm.brand.routes.Badges.picture(badge.file).url))
  }
}