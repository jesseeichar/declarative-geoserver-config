package c2c.geoserver.conf
import java.net.URL
import Requests._
import org.apache.http.auth.UsernamePasswordCredentials

class GeoserverConfigurator(username: String, password: String, geoserverRestUrl: String) {
  implicit val baseURL = new URL(geoserverRestUrl)
  implicit val credentials = new UsernamePasswordCredentials(username, password)

  val requestBuilder = new RestRequestBuilder
  def configure(conf: Configuration) = {
    val requests = conf.workspaces.flatMap(requestBuilder.toRequest(""))
    executeMany(requests)
  }

  def readConfiguration: Configuration = null

  def clearConfig() = {
    import GeoserverRequests._
    val lgReq = layergroups map {lg => delete(lg.href)} 
    val layerReq = layers map {l => delete(l.href)} 
    val wsReq = workspaces flatMap { workspace => 
      val dsReq = workspace.datastores flatMap {ds => ds.featureTypes.map(ft => delete(ft.href)) :+ delete(ds.href)}
      val csReq = workspace.coverageStores flatMap {cs => cs.coverages.map(c => delete(c.href)) :+ delete(cs.href)}
      dsReq ++ csReq :+ delete(workspace.href) 
    }
    
    val styleReq = styles map {s => delete(s.href)}
    
    executeMany(lgReq ++ layerReq ++ wsReq ++ styleReq)
  }
}