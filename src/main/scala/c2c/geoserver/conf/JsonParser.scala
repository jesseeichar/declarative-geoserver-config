package c2c.geoserver.conf
import scalax.file._
import net.liftweb.json._
import Serialization.{read, write}

object JsonParser {
//  implicit val parseFormats = DefaultFormats // Brings in default date formats etc
  private[this] implicit val serializationFormats = Serialization.formats(
      ShortTypeHints(List(classOf[Shp], classOf[ShpDir], classOf[Database], classOf[Raster])))
      
  def parse[T <: JsonElem : Manifest](jsonString: String) = {
    read[T](jsonString)
  }
  def parseConfiguration(jsonString: String) = {
    read[Configuration](jsonString)
  }
  def serialize[T <: JsonElem: Manifest](config: T): String = write[T](config)
  def serializeConfiguration(config:Configuration):String = write[Configuration](config)
}