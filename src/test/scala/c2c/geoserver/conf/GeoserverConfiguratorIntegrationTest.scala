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
		  	"name": "SpecAddedShape",
		    "charset": "ISO-8859-1",
            "path": "%s"},
        {
            "jsonClass": "ShpDir",
		  	"name": "SpecShpDir",
            "path": "%s"},
        {
		    "jsonClass": "Postgis",
		    "description": "Spec postgis description",
		    "name": "SpecPostgis",
		    "host": "localhost",
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
		    "database": "geocat"}]
    }]
}
      """.format(shpPath.path, shpPath.parent.get.path))

  def workspaceConfig = {
    configurator.configure(config)
    val stores = configurator.readConfiguration.workspaces.collect {
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