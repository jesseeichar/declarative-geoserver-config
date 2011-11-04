package c2c.geoserver.conf

import org.specs2._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JsonParserTest extends Specification {
  def is =
    "JsonParser can parse a json specification" ! parseJson ^
      "JsonParser can serialize a configuration to json" ! serialize

  def parseJson = {
    val conf = """
	{
    "styles": [
    {
        "name": "roads",
        "path": "/var/sig/sld/roads.sld"
    }],
    "layergroups": [
    {
        "name": "background",
        "srs": "EPSG:4326",
        "bounds": "0,0,30,30",
        "layers": ["roads", "regions"]
    }],
    "workspaces": [
    {
        "name": "default",
        "uri": "http://camptocamp.com/default"
        "default": true,
        "stores": []
    }]
}"""
    val model = JsonParser.parseConfiguration(conf)
    (model.layergroups must haveSize(1)) and
      (model.styles must haveSize(1)) and
      (model.workspaces must haveSize(1))
  }

  def serialize = {
    val config:Configuration =
      Configuration(
        workspaces = List(
          Workspace(
            name = "edit",
            uri = Some("http://camptocamp.com/edit"),
            stores = List(
              Shp(path = "/shp/roads.shp"),
              ShpDir(path = "/shp/countries.shp", configureAll = Some(false))))))

    val json = JsonParser.serialize(config)
    import net.liftweb.json.{parse,DefaultFormats,JString}
    implicit val parseFormats = DefaultFormats // Brings in default date formats etc
    val parsed = parse(json)
    val workspaceName = (parsed \\ "name").extract[String]
    val stores = (parsed \\ "stores" \ "path" \\ classOf[JString])
    
    val validJson = (workspaceName must_== "edit") and 
    	(stores must contain("/shp/roads.shp", "/shp/countries.shp"))
    	
    val parsedConfig = JsonParser.parseConfiguration(json)
    
    validJson and 
    	(parsedConfig.workspaces must haveSize(1)) and
    	(parsedConfig.workspaces(0).stores must haveSize(2)) and
    	(parsedConfig.workspaces(0).stores.find(_.isInstanceOf[Shp]) must beSome) and
		(parsedConfig.workspaces(0).stores.find(_.isInstanceOf[ShpDir]) must beSome)
  }
}