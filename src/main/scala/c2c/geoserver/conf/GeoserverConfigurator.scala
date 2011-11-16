package c2c.geoserver.conf
import java.net.URL
import Requests._
import org.apache.http.auth.UsernamePasswordCredentials

class GeoserverConfigurator(username: String, password: String, geoserverRestUrl: String, geoserverVersion:String) {
  implicit val baseURL = new URL(geoserverRestUrl)
  implicit val credentials = new UsernamePasswordCredentials(username, password)

  val (configParamExtractor, requestBuilder) = geoserverVersion match {
    case ver if ver startsWith "2.1" => (version21x.ConfigParamExtractor, (new version21x.RestRequestBuilder).toRequest("")_)
    case ver => throw new IllegalArgumentException(ver+" is not a supported Geoserver version")
  }
  def configure(conf: Configuration) = {
    println("Configuring Geoserver: "+geoserverRestUrl+" with configuration:\n"+JsonParser.serializeConfiguration(conf))
    val requests = conf.styles.flatMap(requestBuilder) ++ conf.workspaces.flatMap(requestBuilder)
    executeMany(requests)
  }

  def readConfiguration: Configuration = {
    import GeoserverRequests._
    val uriMapping = namespaces.flatMap { _.resolved.map(ns => ns.prefix -> ns) }.toMap
    val ws =
      workspaces map { ws =>
        new Workspace(
          name = ws.name,
          uri = uriMapping.get(ws.name).map(_.uri),
          stores = stores(ws))
      }
    Configuration(workspaces = ws)
  }

  def stores(ws: GeoserverJson.WorkspaceRef): List[Store] = {
    import configParamExtractor._
    for{
      storeRef <- ws.datastores ++ ws.coverageStores
      ds <- storeRef.resolved
    } yield {
      ds match {
        case Shp(shp) => shp
        case ShpDir(dir) => dir 
        case Postgis(postgis) => postgis 
        case Raster(raster) => raster 
        // NOT FINISHED
      }
    }
  }
  def clearConfig() = {
    import GeoserverRequests._
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