package models

import play.api.libs.json.{Format, Json}

case class PensionContributions(
                                 pensionContributionsPercentage: Double
                               )

object PensionContributions {

  implicit val format:Format[PensionContributions] = Json.format[PensionContributions]
  val pensionPercentage = "pensionPercentage"

}
