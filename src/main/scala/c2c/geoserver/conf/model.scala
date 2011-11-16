package c2c.geoserver.conf
import scalax.io.{Codec, Resource, Input}
import scalax.file.Path

trait JsonElem
sealed trait ConfigElem extends JsonElem
case class Configuration(
  workspaces: List[Workspace] = Nil,
  styles: List[Style] = Nil,
  layergroups: List[LayerGroup] = Nil) extends ConfigElem {
  val workspaceMap = workspaces.map { (ws: Workspace) => ws.name -> ws }.toMap
}

case class Workspace(name: String, uri: Option[String] = None, stores: List[Store]) extends ConfigElem

trait PathRealName {
  self: {def name:Option[String]; def path:String} =>
  
  val realName = name getOrElse Path.fromString(path).simpleName
}
sealed trait Store extends ConfigElem
/**
 * @Path path to the shpfile, NOT a URL
 */
case class Shp(name: Option[String] = None, description: Option[String] = None, path: String, charset: Option[String] = Some("UTF-8"), layers:List[VectorLayer] = Nil)
	extends Store with PathRealName

case class ShpDir(name: Option[String] = None, description: Option[String] = None, path: String, configureAll: Option[Boolean] = None, charset: Option[String] = Some("UTF-8"), layers:List[VectorLayer] = Nil)
	extends Store with PathRealName

object Postgis {
  val defaultPort = 5432
  val defaultSchema = "public"
  val defaultTimeout = 20
  val defaultValidateConnections = false
  val defaultMaxConnections = 10
  val defaultMinConnections = 1
  val defaultLooseBBox = true
  val defaultExposePrimaryKeys = true
  val defaultFetchSize = 1000
  val defaultMaxPreparedStatements = 50
  val defaultPreparedStatements = false
  val defaultEstimatedExtents = true
}
import Postgis._
case class Postgis(
  name: Option[String],
  description: Option[String],
  host: String,
  port: Option[Int] = Some(defaultPort),
  database: String,
  username: String,
  password: Option[String],
  schema: Option[String] = Some(defaultSchema),
  timeout: Option[Int] = Some(defaultTimeout),
  validateConnections: Option[Boolean] = Some(defaultValidateConnections),
  maxConnections: Option[Int] = Some(defaultMaxConnections),
  minConnections: Option[Int] = Some(defaultMinConnections),
  looseBBox: Option[Boolean] = Some(defaultLooseBBox),
  exposePrimaryKeys: Option[Boolean] = Some(defaultExposePrimaryKeys),
  //    primaryKeyTable:Option[String] = None,
  fetchSize: Option[Int] = Some(defaultFetchSize),
  maxPreparedStatements: Option[Int] = Some(defaultMaxPreparedStatements),
  preparedStatements: Option[Boolean] = Some(defaultPreparedStatements),
  estimateExtents: Option[Boolean] = Some(defaultEstimatedExtents),
  layers:List[VectorLayer] = Nil) extends Store {
  lazy val realName = name getOrElse host
  lazy val realPass = password orElse Option(System.getProperty("postgis.geoserver." + host + "." + database + "." + schema + ".password")) getOrElse {
    println("No password defined for Postgis: " + host + "." + database + "." + schema)
    readLine("Enter password:");
  }
}

case class ConnectionParam(key: String, value: String)
/*case class Database(name:String, params:List[ConnectionParam], tables:List[Table]=Nil, createTables:List[CreateTable]=Nil) extends Store
case class Table(name:String) extends ConfigElem
case class Attribute(name:String, binding:String)
case class CreateTable(name:String, nativeName:String, title:String, srs:String, attributes:List[Attribute]) extends ConfigElem*/
sealed trait Raster extends Store with PathRealName {
  def name: Option[String]
  def description: Option[String]
  def path: String
  def enabled: Option[Boolean]
  def layers: List[RasterLayer]
}
case class GeoTIFF(name: Option[String] = None, description: Option[String] = None, path: String, enabled: Option[Boolean] = Some(true), layers: List[RasterLayer] = Nil) extends Raster
case class ImageMosaic(name: Option[String] = None, description: Option[String] = None, path: String, enabled: Option[Boolean] = Some(true), layers: List[RasterLayer] = Nil) extends Raster
case class ECW(name: Option[String] = None, description: Option[String] = None, path: String, enabled: Option[Boolean] = Some(true), layers: List[RasterLayer] = Nil) extends Raster
case class JP2ECW(name: Option[String] = None, description: Option[String] = None, path: String, enabled: Option[Boolean] = Some(true), layers: List[RasterLayer] = Nil) extends Raster
case class WorldImage(name: Option[String] = None, description: Option[String] = None, path: String, enabled: Option[Boolean] = Some(true), layers: List[RasterLayer] = Nil) extends Raster
case class ArcGrid(name: Option[String] = None, description: Option[String] = None, path: String, enabled: Option[Boolean] = Some(true), layers: List[RasterLayer] = Nil) extends Raster
case class Gtopo30(name: Option[String] = None, description: Option[String] = None, path: String, enabled: Option[Boolean] = Some(true), layers: List[RasterLayer] = Nil) extends Raster
case class WMS(name: Option[String] = None, description: Option[String] = None, path: String, enabled: Option[Boolean] = Some(true), layers: List[RasterLayer] = Nil) extends Raster

case class VectorLayer(name: Option[String], nativeName: Option[String], srs:Option[String], bbox:List[Double]) extends ConfigElem
case class RasterLayer(name: Option[String], nativeName: Option[String]) extends ConfigElem

case class LayerGroup(name: String, srs: String, bounds: List[Double], layers: List[String] = Nil) extends ConfigElem
/**
 * Define a new style.  
 * 
 * @name the name/id of the style, will be used when defining layers and relating the layers to styles
 * @filename the name of the file where the sld will be saved.  
 * 			 The name is relative to:  data_dir/styles (data_dir is the geoserver data directory)
 * 			 by default it is name+".sld"
 * @url a url for loading the sld.  Only one of url or path should be used.  
 * @path the path to the file of the sld.  The contents of the file will be uploaded to the server
 */
case class Style(name: String, filename: Option[String], url: Option[String], path: Option[String]) extends ConfigElem {
  val resource:Option[Input] = url.map(u => Resource.fromURL(u):Input) orElse path.map(f => Resource.fromFile(f):Input)
}
