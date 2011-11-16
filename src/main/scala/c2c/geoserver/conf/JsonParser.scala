package c2c.geoserver.conf
import scalax.file._
import net.liftweb.json._
import Serialization.{read, write}

object JsonParser {
  private[this] implicit val serializationFormats = Serialization.formats(
      ShortTypeHints(List(classOf[Shp], classOf[ShpDir], classOf[Postgis], 
          classOf[GeoTIFF], classOf[ImageMosaic], classOf[ECW], classOf[Gtopo30], 
          classOf[Gtopo30], classOf[WMS], classOf[ArcGrid], classOf[WorldImage],
          classOf[JP2ECW])))
      
  def parseConfiguration(jsonString: String) = parse[Configuration](jsonString)
  def parse[T <: JsonElem : Manifest](jsonString: String) = read[T](jsonString)
  
  def serializeConfiguration(config:Configuration):String = serialize(config)
  def serialize[T <: JsonElem: Manifest](config: T): String = 
		  pretty(render(Extraction.decompose(config)(serializationFormats)))
}