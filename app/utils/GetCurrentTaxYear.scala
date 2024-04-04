package utils

import uk.gov.hmrc.time.TaxYear

import java.time.{LocalDate, ZoneId}

object GetCurrentTaxYear {

  def getCurrentTaxYear: String = {
    val currentDate = LocalDate.now(ZoneId.of("Europe/London"))
    val taxYear = TaxYear(currentDate.getYear)
    if (currentDate isBefore taxYear.starts) {
      val previousTaxYear = taxYear.previous
      s"${previousTaxYear.startYear}/${taxYear.startYear.toString.takeRight(2)}"
    } else {
      s"${taxYear.startYear}/${(taxYear.startYear + 1).toString.takeRight(2)}"
    }
  }

  def getTaxYear: Int = {
      TaxYear.current.currentYear
  }

}
