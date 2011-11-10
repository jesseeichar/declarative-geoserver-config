package c2c.geoserver.conf
import java.net.URL
import Requests._
import org.apache.http.auth.UsernamePasswordCredentials

class GeoserverConfigurator(username: String, password: String, geoserverRestUrl: String, geoserverVersion:String) {
  implicit val baseURL = new URL(geoserverRestUrl)
  implicit val credentials = new UsernamePasswordCredentials(username, password)

  val requestBuilder = geoserverVersion match {
    case ver if ver startsWith "2.1" => new RestRequestBuilder21
    case ver => throw new IllegalArgumentException(ver+" is not a supported Geoserver version")
  }
  def configure(conf: Configuration) = {
    val requests = conf.workspaces.flatMap(requestBuilder.toRequest(""))
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
          stores = dataStores(ws) ++ coverageStores(ws))
      }
    Configuration(workspaces = ws)
  }

  def dataStores(ws: GeoserverJson.WorkspaceRef): List[Store] = {
    import ConfigParamExtractor._
    for{
      ds <- ws.datastores
      resolved <- ds.resolved
    } yield {
      resolved match {
        case Shp(shp) => shp
        case ShpDir(shp) => shp // NOT FINISHED
      }
    }
  }
  def coverageStores(ws: GeoserverJson.WorkspaceRef): List[Store] = {
Nil // NOT FINISHED
  }
  def clearConfig() = {
    import GeoserverRequests._
    val lgReq = layergroups map { lg => delete(lg.href) }
    val layerReq = layers map { l => delete(l.href) }
    val wsReq = workspaces map { workspace =>
      /*val dsReq = workspace.datastores flatMap { ds => ds.featureTypes.map(ft => delete(ft.href)) :+ delete(ds.href) }
      val csReq = workspace.coverageStores flatMap { cs => cs.coverages.map(c => delete(c.href)) :+ delete(cs.href) }
      dsReq ++ csReq :+*/ 
      delete(workspace.href,"recurse" -> true)
    }

    val styleReq = styles map { s => delete(s.href) }

    executeMany(lgReq ++ layerReq ++ wsReq ++ styleReq)
  }
}