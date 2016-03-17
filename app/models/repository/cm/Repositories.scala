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

package models.repository.cm

import models.repository.cm.brand.CertificateTemplateRepository
import play.api.Application

/**
  * Contains references to all repositories in this package
  */
class Repositories(app: Application, root: models.repository.Repositories) {

  lazy val brand: BrandRepository = new BrandRepository(app, root)
  lazy val certificate: CertificateTemplateRepository = new CertificateTemplateRepository(app)
  lazy val evaluation: EvaluationRepository = new EvaluationRepository(app, root)
  lazy val event: EventRepository = new EventRepository(app, root)
  lazy val facilitator: FacilitatorRepository = new FacilitatorRepository(app)
  lazy val license: LicenseRepository = new LicenseRepository(app)

  object rep {
    lazy val brand: models.repository.cm.brand.Repositories = new models.repository.cm.brand.Repositories(app)
    lazy val event: models.repository.cm.event.Repositories = new models.repository.cm.event.Repositories(app)
  }

}
