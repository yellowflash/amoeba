package amoeba

import amoeba.ConfigReader._
import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

sealed trait Co
case class First(_type: String, field: Int) extends Co
case class Second(_type: String, field: Int) extends Co

class ConfigReaderSpec extends FlatSpec {

  val config = ConfigFactory.parseString(
    """
      |string: "String",
      |integer: 12,
      |double: 13.0,
      |coprod: {
      |  _type: "First",
      |  field: 12,
      |}
    """.stripMargin)

  "ConfigReader" should "read optional" in {
    read[Option[String]](config, "string") should be (Some("String"))
    read[Option[String]](config, "non-existent") should be (None)
   }

  it should "read product type" in {
    val reader: ConfigReader[First] = deriveInstance
    read[First](config, "coprod") should be (First("First", 12))
  }

  it should "read coproduct types" in {
    val reader : ConfigReader[Co] = deriveInstance
    read[Co](config, "coprod") should be(First("First", 12))
  }
}
