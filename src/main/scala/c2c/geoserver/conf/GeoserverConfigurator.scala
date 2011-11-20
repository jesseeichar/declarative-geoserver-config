package c2c.geoserver.conf
import java.net.URL
import Requests._
import org.apache.http.auth.UsernamePasswordCredentials

class GeoserverConfigurator(username: String, password: String, geoserverRestUrl: String, geoserverVersion:String) {
  implicit val baseURL = new URL(geoserverRestUrl)
  implicit val credentials = new UsernamePasswordCredentials(username, password)

  val (configParamExtractor, requestBuilder, geoserverRequests) = geoserverVersion match {
    case ver if ver startsWith "2.1" => 
      (new version21x.ConfigParamExtractor(), 
          (new version21x.RestRequestBuilder).toRequest("")_, 
          version21x.GeoserverRequests:GeoserverRequests)
    case ver => throw new IllegalArgumentException(ver+" is not a supported Geoserver version")
  }
  def configure(conf: Configuration) = {
    println("Configuring Geoserver: "+geoserverRestUrl+" with configuration:\n"+JsonParser.serializeConfiguration(conf))
    val requests = conf.styles.flatMap(requestBuilder) ++ conf.workspaces.flatMap(requestBuilder)
    executeMany(requests)
  }

  def readConfiguration: Configuration = configParamExtractor.extractConfig()

  def clearConfig() = {
    import geoserverRequests._
    val lgReq = layergroups map { lg => delete(lg.href) }
    val layerReq = layers map { l => delete(l.href) }
    val wsReq = workspaces flatMap { workspace =>
      val dsReq = workspace.datastores flatMap { ds => ds.featureTypes.map(ft => delete(ft.href)) :+ delete(ds.href) }
      val csReq = workspace.coverageStores flatMap { cs => cs.coverages.map(c => delete(c.href)) :+ delete(cs.href) }
      dsReq ++ csReq :+ delete(workspace.href,"recurse" -> true)
    }

    val styleReq = styles map { s => delete(s.href) }

    executeMany(lgReq ++ layerReq ++ wsReq ++ styleReq)
  }
}