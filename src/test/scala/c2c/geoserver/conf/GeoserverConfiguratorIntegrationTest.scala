package c2c.geoserver.conf

import org.specs2.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.net.URL
import scalax.io.Resource
import net.liftweb.json._
import org.apache.http.auth.UsernamePasswordCredentials

@RunWith(classOf[JUnitRunner])
class GeoserverConfiguratorIntegrationTest extends IntegrationSpec {
  def is =
    sequential ^
      "configuring a geoserver with a workspace should create a new workspace and namespace" ! workspaceConfig /*^
      "running clear config should remove all workspaces" ! clear*/

  lazy val configurator = {
    new GeoserverConfigurator(username, password, url.toExternalForm())
  }

  val shpPath = classOf[GeoserverConfigurator].getResource("SpecSampleShp.shp")
  val config = Configuration(
    workspaces = List(
        Workspace(name = "SpecWorkspace", stores = List(
            Shp(shpPath.getFile)))))

  def workspaceConfig = {
    configurator.configure(config)
    GeoserverRequests.workspaces.map(_.name) must contain("SpecWorkspace") 
  }
  def clear = {
    configurator.clearConfig()
    GeoserverRequests.workspaces must beEmpty
  }
}