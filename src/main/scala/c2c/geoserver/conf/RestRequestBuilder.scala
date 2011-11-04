package c2c.geoserver.conf
import org.apache.http.client.methods.HttpRequestBase
import Requests._
//import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import java.net.URL

class RestRequestBuilder(geoserverRestUrL: URL) {
  implicit val baseURL = geoserverRestUrL

  def toRequest(elem: ConfigElem): Seq[HttpRequestBase] = elem match {
    case c: Configuration => configurationToRequest(c)
    case ws: Workspace => workspaceToRequest(ws)
    case style: Style => styleToRequest(style)
    case layerGroup: LayerGroup => layerGroupToRequest(layerGroup)
    case shp: Shp => shpToRequest(shp)
    case dir: ShpDir => shpDirToRequest(dir)
    case database: Database => databaseToRequest(database)
    case table: Table => tableToRequest(table)
    case table: CreateTable => createTableToRequest(table)
    case raster: Raster => rasterToRequest(raster)
    case layer: Layer => layerToRequest(layer)
  }

  def configurationToRequest(config: Configuration): Seq[HttpRequestBase] = {
    config.styles.flatMap(toRequest) ++
      config.workspaces.flatMap(toRequest) ++
      config.layergroups.flatMap(toRequest)
  }
  def workspaceToRequest(ws: Workspace): Seq[HttpRequestBase] = {
    val requests =
      post("workspaces", { "workspace" -> { "name" -> ws.name } }) +:
        ws.stores.flatMap(toRequest)
        
    if (ws.uri.isDefined) {
      val namespace = post("namespaces", { "namespace" -> { ("name" -> ws.name) ~ ("href" -> ws.uri) } })
      namespace +: requests
    } else {
      requests
    }
  }
  def styleToRequest(style: Style): Seq[HttpRequestBase] = null
  def layerGroupToRequest(layerGroup: LayerGroup): Seq[HttpRequestBase] = null
  def shpToRequest(shp: Shp): Seq[HttpRequestBase] = null
  def shpDirToRequest(dir: ShpDir): Seq[HttpRequestBase] = null
  def databaseToRequest(database: Database): Seq[HttpRequestBase] = null
  def tableToRequest(table: Table): Seq[HttpRequestBase] = null
  def createTableToRequest(table: CreateTable): Seq[HttpRequestBase] = null
  def rasterToRequest(raster: Raster): Seq[HttpRequestBase] = null
  def layerToRequest(layer: Layer): Seq[HttpRequestBase] = null
}