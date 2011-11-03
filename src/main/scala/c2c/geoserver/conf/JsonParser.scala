package c2c.geoserver.conf
import scalax.file._
import net.liftweb.json._
import Serialization.{read, write}

object JsonParser {
//  implicit val parseFormats = DefaultFormats // Brings in default date formats etc
  implicit val serializationFormats = Serialization.formats(
      ShortTypeHints(List(classOf[Shp], classOf[ShpDir], classOf[Database], classOf[Raster]))
      )
  def parse(jsonString: String) = {
    read[Configuration](jsonString)
  }
  def serialize(config:Configuration):String = write[Configuration](config)
}