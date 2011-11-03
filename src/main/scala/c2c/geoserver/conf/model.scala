package c2c.geoserver.conf

case class Configuration(
    workspaces:List[Workspace]=Nil,
    styles:List[Style]=Nil,
    layergroups:List[LayerGroup]=Nil)
    
case class Workspace(name:String, uri:String, default:Option[Boolean]=Some(false), stores:List[Store])

abstract class Store
case class Shp(path:String) extends Store
case class ShpDir(path:String, configureAll:Option[Boolean]) extends Store

case class ConnectionParam(key:String, value:String)
case class Database(name:String, params:List[ConnectionParam], tables:List[Table]=Nil, createTables:List[CreateTable]=Nil) extends Store
case class Table(name:String)
case class Attribute(name:String, binding:String)
case class CreateTable(name:String, nativeName:String, title:String, srs:String, attributes:List[Attribute])

case class Raster(name:String, `type`:String, enabled:Option[Boolean], url:String, layers:List[Layer]=Nil) extends Store
case class Layer(name:String)

case class LayerGroup(name:String, srs:String, bounds:String, layers:List[String]=Nil)
case class Style(name:String,path:String)