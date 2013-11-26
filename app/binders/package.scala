/*
 * Happy Melly Teller
 * Copyright (C) 2013, Happy Melly http://www.happymelly.com
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

package binders

import play.api.mvc.{ QueryStringBindable, PathBindable }
import org.joda.money.{ Money, CurrencyUnit }
import scala.util.Try
import org.joda.money.format.{ MoneyAmountStyle, MoneyFormatterBuilder }

object `package` {

  // CurrencyUnit

  private def currencyLookup(code: String) = Try {
    CurrencyUnit.of(code.toUpperCase)
  }.toOption.toRight("Not a known currency code: " + code)

  implicit def currencyUnitPathBindable: PathBindable[CurrencyUnit] = new PathBindable[CurrencyUnit] {
    def bind(key: String, value: String): Either[String, CurrencyUnit] = currencyLookup(value)

    def unbind(key: String, value: CurrencyUnit): String = value.getCurrencyCode
  }

  implicit def currencyUnitQueryStringBindable: QueryStringBindable[CurrencyUnit] = new QueryStringBindable[CurrencyUnit] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, CurrencyUnit]] = {
      params.get(key).flatMap(_.headOption).map(currencyLookup)
    }

    def unbind(key: String, value: CurrencyUnit): String = value.getCurrencyCode
  }

  // Money

  private def parseMoney(input: String) = Try {
    moneyFormatter.parseMoney(input.trim.toUpperCase)
  }.toOption.toRight("Not a valid monetary amount: " + input)

  private val moneyFormatter = new MoneyFormatterBuilder().appendCurrencyCode().appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter

  implicit def moneyPathBindable: PathBindable[Money] = new PathBindable[Money] {
    def bind(key: String, value: String): Either[String, Money] = parseMoney(value)

    def unbind(key: String, value: Money): String = moneyFormatter.print(value)
  }

  implicit def moneyQueryStringBindable = new QueryStringBindable[Money] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Money]] = {
      params.get(key).flatMap(_.headOption).map(parseMoney)
    }

    def unbind(key: String, value: Money): String = moneyFormatter.print(value)
  }

}
