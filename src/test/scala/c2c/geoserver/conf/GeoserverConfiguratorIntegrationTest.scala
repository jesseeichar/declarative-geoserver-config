package c2c.geoserver.conf

import org.specs2.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.net.URL
import scalax.io.Resource
import net.liftweb.json._
import org.apache.http.auth.UsernamePasswordCredentials

@RunWith(classOf[JUnitRunner])
class GeoserverConfiguratorIntegrationTest extends Specification {
  def is =
    sequential ^
      "configuring a geoserver with a workspace should create a new workspace and namespace" ! workspaceConfig ^
      "running clear config should remove all workspaces" ! clear

  lazy val username = Option(System.getProperty("geoserver.username")) getOrElse "admin"
  lazy val password = Option(System.getProperty("geoserver.password")) getOrElse "geoserver"
  implicit def url = new URL(Option(System.getProperty("geoserver.url")) getOrElse "http://localhost:8080/geoserver/rest")
  implicit def credentials = new UsernamePasswordCredentials(username, password)
  lazy val configurator = {
    new GeoserverConfigurator(username, password, url.toExternalForm())
  }

  def workspaceConfig = pending
  def clear = {
    configurator.clearConfig()
    GeoserverRequests.workspaces must beEmpty
  }
}