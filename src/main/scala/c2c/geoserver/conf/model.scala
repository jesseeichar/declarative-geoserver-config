package c2c.geoserver.conf
import scalax.io.Codec
import scalax.file.Path

trait JsonElem
sealed trait ConfigElem extends JsonElem
case class Configuration(
    workspaces:List[Workspace]=Nil,
    styles:List[Style]=Nil,
    layergroups:List[LayerGroup]=Nil) extends ConfigElem {
  val workspaceMap = workspaces.map {(ws:Workspace) => ws.name -> ws}.toMap
}

case class Workspace(name:String, uri:Option[String]=None, stores:List[Store]) extends ConfigElem


sealed trait Store extends ConfigElem
/**
 * @Path path to the shpfile, NOT a URL
 */
case class Shp(name:Option[String]=None, path:String, charset:Option[String]=Some("UTF-8")) extends Store {
  def realName = name getOrElse Path.fromString(path).simpleName
}
case class ShpDir(name:Option[String]=None, path:String, configureAll:Option[Boolean]=None, charset:Option[String]=Some("UTF-8")) extends Store {
  def realName = name getOrElse Path.fromString(path).simpleName
}
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
    name:Option[String],
    description:Option[String],
    host:String, 
    port:Option[Int]=Some(defaultPort), 
    database:String, 
    username:String, 
    password:Option[String],
    schema:Option[String]=Some(defaultSchema),
    timeout:Option[Int]=Some(defaultTimeout),
    validateConnections:Option[Boolean] = Some(defaultValidateConnections),
    maxConnections:Option[Int] = Some(defaultMaxConnections),
    minConnections:Option[Int] = Some(defaultMinConnections),
    looseBBox:Option[Boolean] = Some(defaultLooseBBox),
    exposePrimaryKeys:Option[Boolean] = Some(defaultExposePrimaryKeys),
//    primaryKeyTable:Option[String] = None,
    fetchSize:Option[Int] = Some(defaultFetchSize),
    maxPreparedStatements:Option[Int]=Some(defaultMaxPreparedStatements),
    preparedStatements:Option[Boolean]=Some(defaultPreparedStatements),
    estimateExtents:Option[Boolean]=Some(defaultEstimatedExtents)) extends Store {
  lazy val realName = name getOrElse host
  lazy val realPass = password orElse Option(System.getProperty("postgis.geoserver."+host+"."+database+"."+schema+".password")) getOrElse {
    println("No password defined for Postgis: "+host+"."+database+"."+schema)
    readLine("Enter password:" ); 
  }
}

case class ConnectionParam(key:String, value:String)
case class Database(name:String, params:List[ConnectionParam], tables:List[Table]=Nil, createTables:List[CreateTable]=Nil) extends Store
case class Table(name:String) extends ConfigElem
case class Attribute(name:String, binding:String)
case class CreateTable(name:String, nativeName:String, title:String, srs:String, attributes:List[Attribute]) extends ConfigElem

case class Raster(name:String, `type`:String, enabled:Option[Boolean], url:String, layers:List[Layer]=Nil) extends Store
case class Layer(name:String) extends ConfigElem

case class LayerGroup(name:String, srs:String, bounds:String, layers:List[String]=Nil) extends ConfigElem
case class Style(name:String,path:String) extends ConfigElem
