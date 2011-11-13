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

@RunWith(classOf[JUnitRunner])
class GeoserverConfiguratorIntegrationTest extends IntegrationSpec {
  def is =
    sequential ^
    "running clear config should remove all workspaces" ! clear ^
      "configuring a geoserver with a workspace should create a new workspace and namespace" ! workspaceConfig //^

  lazy val configurator = {
    new GeoserverConfigurator(username, password, url.toExternalForm(), "2.1.x")
  }

  val shpPath = Path.fromString(classOf[GeoserverConfigurator].getResource("SpecSampleShp.shp").getFile)
  val tifPath = Path.fromString(classOf[GeoserverConfigurator].getResource("SpecSampleTif.tif").getFile)
  val arcgridPath = Path.fromString(classOf[GeoserverConfigurator].getResource("SpecSampleArcgrid.asc").getFile)
  val worldImagePath = Path.fromString(classOf[GeoserverConfigurator].getResource("SpecSampleWorldImage.png").getFile)
  /* TODO: json todos
   *  add description, use memory mapped buffers, Create spatial index if missing/outdated, Cache and reuse memory maps
   *  for Shp and ShpDir*/
  val config = JsonParser.parseConfiguration("""
{
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
		    "port": 5555,
		    "timeout": 10,
		    "schema": "someSchema",
		  	"username": "www-data",
		  	"password": "www-data",
		    "maxConnections": 3,
		    "minConnections": 2,
		    "fetchSize": 100,
		    "looseBBox": true,
		    "validateConnections": true,
		    "preparedStatements": true,
		    "maxPreparedStatements": 10,
		    "exposePrimaryKeys": true,
		    "estimateExtents": false,
		    "database": "geocat"},
      {
		    "jsonClass": "GeoTIFF",
		    "name": "SpecGeoTIFF",
		  	"enabled": true,
		  	"description": "a tif file for specs test",
		  	"path": "%s"},
      {
		    "jsonClass": "Arcgrid",
		    "name": "SpecArcgrid",
		  	"enabled": true,
		  	"description": "a Arcgrid file for specs test",
		  	"path": "%s"},
      {
		    "jsonClass": "WorldImage",
		    "name": "SpecWorldImage",
		  	"enabled": true,
		  	"description": "a WorldImage file for specs test",
		  	"path": "%s"}]
    }]
}
      """.format(shpPath.path, shpPath.parent.get.path, tifPath.path, arcgridPath.path, worldImagePath.path))

  def workspaceConfig = {
    configurator.configure(config)
    println(config.workspaces(0).stores(2).asInstanceOf[Postgis].estimateExtents)
    val readConfig = configurator.readConfiguration
    val stores = readConfig.workspaces.collect {
      case ws if config.workspaceMap.contains(ws.name) => ws.stores must haveTheSameElementsAs(config.workspaceMap(ws.name).stores)
    }
    (stores must haveSize(config.workspaces.size)) and
      (stores.foldLeft(success: Result) { (acc, next) => acc and next })
  }
  def clear = {
    configurator.clearConfig()
    GeoserverRequests.workspaces must beEmpty
  }
}