package c2c.geoserver.conf

import org.specs2.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.net.URL
import scalax.io.Resource
import net.liftweb.json._
import org.apache.http.auth.UsernamePasswordCredentials
import scalax.file.Path
import org.specs2.execute.Result
import scala.xml.XML

@RunWith(classOf[JUnitRunner])
class GeoserverConfiguratorIntegrationTest extends IntegrationSpec {
  def is =
    sequential ^
    "running clear config should remove all workspaces" ! clear ^
      "configuring a geoserver with a workspace should create a new workspace and namespace" ! workspaceConfig //^

  lazy val configurator = {
    new GeoserverConfigurator(username, password, url.toExternalForm(), "2.1.x")
  }

  val gridSld = Path.fromString(classOf[GeoserverConfigurator].getResource("grid.sld").getFile)
  val lineSld = Path.fromString(classOf[GeoserverConfigurator].getResource("line.sld").getFile)
  val shpPath = Path.fromString(classOf[GeoserverConfigurator].getResource("SpecSampleShp.shp").getFile)
  val tifPath = Path.fromString(classOf[GeoserverConfigurator].getResource("SpecSampleTif.tif").getFile)
  val ArcGridPath = Path.fromString(classOf[GeoserverConfigurator].getResource("SpecSampleArcGrid.asc").getFile)
  val worldImagePath = Path.fromString(classOf[GeoserverConfigurator].getResource("SpecSampleWorldImage.png").getFile)
  /* TODO: json todos
   *  add description, use memory mapped buffers, Create spatial index if missing/outdated, Cache and reuse memory maps
   *  for Shp and ShpDir*/
  val config = JsonParser.parseConfiguration("""
{
	"styles": [{
		"name": "specGrid"
		"path": "%s"},
	{
		"name": "specLine"
		"path": "%s"}],
    "workspaces": [{
        "name": "SpecWorkspace",
        "uri": "http://camptocamp.com/spec",
        "default": false,
        "stores": [{
            "jsonClass": "Shp",
		  	"description": "a shp file for specs test",
		  	"name": "SpecAddedShape",
		    "charset": "ISO-8859-1",
            "path": "%s"},
        {
            "jsonClass": "ShpDir",
		  	"name": "SpecShpDir",
		    "description": "a dir of shp files for specs test",
            "path": "%s"},
        {
		    "jsonClass": "Postgis",
		    "description": "Spec postgis description",
		    "name": "SpecPostgis",
		    "host": "localhost",
		    "port": 5432,
		    "timeout": 10,
		    "schema": "public",
		  	"username": "www-data",
		  	"password": "www-data",
		    "maxConnections": 3,
		    "minConnections": 2,
		    "fetchSize": 100,
		    "looseBBox": true,
		    "validateConnections": true,
		    "preparedStatements": false,
		    "maxPreparedStatements": 10,
		    "exposePrimaryKeys": true,
		    "estimateExtents": true,
		    "database": "geocat",
		    "layers": [{
		  			"name": "switzerland",
		  			"nativeName": "countries",
		  			"title": "the title",
		  			"abstract": "the abstract",
		  			"srs": "EPSG:21781",
		  			"bbox": [485000, 75000, 833000, 296000],
		  			"llbbox": [5.9, 45.79, 10.5, 47.8]}]},
      {
		    "jsonClass": "GeoTIFF",
		    "name": "SpecGeoTIFF",
		  	"enabled": true,
		  	"description": "a tif file for specs test",
		  	"path": "%s"}]
    }]
}
      """.format(gridSld.path, lineSld.path, shpPath.path, shpPath.parent.get.path, 
    		  	 tifPath.path))

  def workspaceConfig = {
    configurator.configure(config)
    val readConfig = configurator.readConfiguration
    val stores = readConfig.workspaces.collect {
      case ws if config.workspaceMap.contains(ws.name) => 
        (ws.stores must haveTheSameElementsAs(config.workspaceMap(ws.name).stores)) and
        	(ws.uri must_== config.workspaceMap(ws.name).uri)
    }
    
    println(readConfig.styleMap)
    val styles = config.styleMap.collect {
      case (name,style) if readConfig.styleMap contains name => 
	      val readSld = readConfig.styleMap.get(name).flatMap(_.resource).get.slurpString
	      val expectedSld = style.resource.get.slurpString
	      XML.loadString(readSld) must beEqualToIgnoringSpace (XML.loadString(expectedSld))
    }

    
    (stores must haveSize(config.workspaces.size)) and
    (styles must haveSize(config.styles.size)) and
      (styles.foldLeft(success: Result) { (acc, next) => acc and next }) and
      (stores.foldLeft(success: Result) { (acc, next) => acc and next })
  }
  def clear = {
    configurator.clearConfig()
    configurator.geoserverRequests.workspaces must beEmpty
  }
}