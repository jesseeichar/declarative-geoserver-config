package c2c.geoserver.conf
import java.net.URL
import Requests._
import org.apache.http.auth.UsernamePasswordCredentials

class GeoserverConfigurator(username: String, password: String, geoserverRestUrl: String) {
  implicit val baseURL = new URL(geoserverRestUrl)
  implicit val credentials = new UsernamePasswordCredentials(username, password)

  val requestBuilder = new RestRequestBuilder(new URL(geoserverRestUrl))
  def configure(conf: Configuration) = {
    val requests = conf.workspaces.map(requestBuilder.toRequest)
  }

  def readConfiguration: Configuration = null

  def clearConfig() = {
    val workspaces = GeoserverRequests.workspaces
    for { 
      workspace <- workspaces
//      namespace <- 
    } yield {
      val s = GeoserverRequests.datastores(workspace)
      println(s)
    }
  /*  workspaces.foreach { ws =>
      val deleteResponse = executeOne(delete("workspace."+ws.name))
      val statusCode = deleteResponse.getStatusLine().getStatusCode()
      if(statusCode != 200) {
        println("Unable to delete workspace: "+ws.name+": "+deleteResponse.getStatusLine().getReasonPhrase());
      }
    }*/
  }
}