package c2c.geoserver.conf

trait JsonElem
sealed trait ConfigElem extends JsonElem
case class Configuration(
    workspaces:List[Workspace]=Nil,
    styles:List[Style]=Nil,
    layergroups:List[LayerGroup]=Nil) extends ConfigElem

case class Workspace(name:String, uri:Option[String], default:Option[Boolean]=Some(false), stores:List[Store]) extends ConfigElem

sealed trait Store extends ConfigElem
case class Shp(path:String) extends Store
case class ShpDir(path:String, configureAll:Option[Boolean]) extends Store

case class ConnectionParam(key:String, value:String)
case class Database(name:String, params:List[ConnectionParam], tables:List[Table]=Nil, createTables:List[CreateTable]=Nil) extends Store
case class Table(name:String) extends ConfigElem
case class Attribute(name:String, binding:String)
case class CreateTable(name:String, nativeName:String, title:String, srs:String, attributes:List[Attribute]) extends ConfigElem

case class Raster(name:String, `type`:String, enabled:Option[Boolean], url:String, layers:List[Layer]=Nil) extends Store
case class Layer(name:String) extends ConfigElem

case class LayerGroup(name:String, srs:String, bounds:String, layers:List[String]=Nil) extends ConfigElem
case class Style(name:String,path:String) extends ConfigElem
