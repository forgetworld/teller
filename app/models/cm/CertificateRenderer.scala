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

package models.cm

import java.io.ByteArrayOutputStream

import com.itextpdf.text._
import com.itextpdf.text.pdf.{BaseFont, ColumnText, PdfWriter}
import models.Person
import models.cm.event.Attendee
import org.joda.time.LocalDate
import play.api.i18n.Messages

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Contains methods for rendering certificates for different brands
  */
object CertificateRenderer {


  /**
    * Returns new generated LCM certificate
    *
    * @param event Event
    * @param attendee Attendee
    */
  def lcmCertificate(event: Event,
                     attendee: Attendee,
                     facilitators: Seq[Person],
                     img: com.itextpdf.text.Image)(implicit messages: Messages): Array[Byte] = {

    val document = new Document(PageSize.A4.rotate)
    val baseFont = BaseFont.createFont("reports/fonts/SourceSansPro-ExtraLight.ttf",
      BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
    val arialFont = FontFactory.getFont("Arial Bold",
      BaseFont.IDENTITY_H, true, 12, Font.BOLD)

    val output = new ByteArrayOutputStream()
    val writer = PdfWriter.getInstance(document, output)
    document.open()
    val cofacilitator = if (facilitators.length > 1) true else false
    img.setAbsolutePosition(28, 0)
    img.scalePercent(24)
    document.add(img)

    val font = new Font(baseFont, 40)
    font.setColor(0, 0, 0)

    val name = new Phrase(attendee.fullName, font)
    val title = new Phrase(event.title, font)
    val dateString = if (event.schedule.start == event.schedule.end) {
      event.schedule.end.toString("MMMM d yyyy")
    } else {
      event.schedule.start.toString("MMMM d, ") + event.schedule.end.toString("d yyyy")
    }
    val location = "%s in %s, %s".format(dateString, event.location.city,
      Messages("country." + event.location.countryCode))
    val locationPhrase = new Phrase(location, font)

    val canvas = writer.getDirectContent
    canvas.saveState()
    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, name, PageSize.A4.getHeight / 2, 410, 0)
    font.setSize(30)
    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, locationPhrase, PageSize.A4.getHeight / 2, 283, 0)
    font.setSize(18)
    val issued = LocalDate.now().toString("MMMM d, yyyy").toUpperCase
    val issueDate = new Phrase(issued, arialFont)

    if (cofacilitator) {
      ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, issueDate, 518, 90, 0)
      val firstFacilitator = facilitators.head
      val secondFacilitator = facilitators.last
      val firstName = new Phrase(firstFacilitator.fullName.toUpperCase, arialFont)
      val secondName = new Phrase(secondFacilitator.fullName.toUpperCase, arialFont)
      ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
        firstName, 400, 150, 0)
      ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
        secondName, 640, 150, 0)
      firstFacilitator.signatureId.foreach { signature =>
        renderSignature(signature, (90, 80), (350, 175), document)
      }
      secondFacilitator.signatureId.foreach { signature =>
        renderSignature(signature, (90, 80), (590, 175), document)
      }

    } else {
      ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, issueDate, 480, 90, 0)
      val facilitator = facilitators.head
      val name = new Phrase(facilitator.fullName.toUpperCase, arialFont)
      ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, name, 480, 150, 0)
      facilitator.signatureId.foreach { signature =>
        renderSignature(signature, (115, 100), (410, 170), document)
      }
    }
    canvas.restoreState
    document.close()

    output.toByteArray
  }


  /**
    * Returns new generated M30 certificate for the given participant
    *
    * @param handledDate The date participant's evaluation was approved
    * @param event Event
    * @param attendee Attendee
    */
  def m30Certificate(handledDate: Option[LocalDate],
                     id: String,
                     event: Event,
                     attendee: Attendee,
                     facilitators: Seq[Person],
                     img: com.itextpdf.text.Image)(implicit messages: Messages): Array[Byte] = {

    val document = new Document(PageSize.A4.rotate)
    val baseFont = BaseFont.createFont("reports/fonts/DejaVuSerif.ttf",
      BaseFont.IDENTITY_H, BaseFont.EMBEDDED)

    val output = new ByteArrayOutputStream()
    val writer = PdfWriter.getInstance(document, output)
    document.open()
    val cofacilitator = if (facilitators.length > 1) true else false
    img.setAbsolutePosition(7, 2)
    img.scalePercent(48)
    document.add(img)

    val font = new Font(baseFont, 20)
    font.setColor(0, 181, 228)

    val name = new Phrase(attendee.fullName, font)
    val title = new Phrase(event.title, font)
    val dateString = if (event.schedule.start == event.schedule.end) {
      event.schedule.end.toString("d MMMM yyyy")
    } else {
      event.schedule.start.toString("d + ") + event.schedule.end.toString("d MMMM yyyy")
    }
    val eventDate = new Phrase(dateString, font)
    val location = new Phrase(event.location.city + ", " +
      Messages("country." + event.location.countryCode), font)
    val date = new Phrase(handledDate.map(_.toString("dd MMMM yyyy")).getOrElse(""), font)
    val certificateIdBlock = new Phrase(id, font)

    val canvas = writer.getDirectContent
    canvas.saveState()
    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, name, PageSize.A4.getHeight / 2, 415, 0)
    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, title, PageSize.A4.getHeight / 2, 330, 0)
    font.setSize(12)
    font.setColor(150, 150, 150)
    ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT, certificateIdBlock, 680, 450, 0)
    font.setColor(0, 0, 0)
    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, eventDate, 207, 290, 0)
    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, location, 208, 235, 0)
    ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, date, 208, 183, 0)

    if (cofacilitator) {
      val firstFacilitator = facilitators.head
      val secondFacilitator = facilitators.last
      val firstName = new Phrase(firstFacilitator.fullName, font)
      val secondName = new Phrase(secondFacilitator.fullName, font)
      ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, firstName, 650, 290, 0)
      ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, secondName, 650, 185, 0)
      firstFacilitator.signatureId.foreach { signature =>
        renderSignature(signature, (100, 95), (595, 300), document)
      }
      secondFacilitator.signatureId.foreach { signature =>
        renderSignature(signature, (100, 95), (595, 180), document)
      }

    } else {
      val facilitator = facilitators.head
      val name = new Phrase(facilitator.fullName, font)
      ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, name, 650, 185, 0)
      facilitator.signatureId.foreach { signature =>
        renderSignature(signature, (155, 100), (570, 205), document)
      }
    }
    canvas.restoreState
    document.close()

    output.toByteArray
  }

  protected def renderSignature(id: String, scale: (Int, Int), position: (Int, Int), document: Document) = {
    val imageData = Await.result(Person.signature(id).uploadToCache(), 5 seconds)
    try {
      val signature = com.itextpdf.text.Image.getInstance(imageData, true)
      signature.scaleToFit(scale._1, scale._2)
      signature.setAbsolutePosition(position._1, position._2)
      document.add(signature)
    } catch {
      case _: Throwable => Unit
    }
  }
}
