package c2c.geoserver.conf
package version21x

import org.apache.http.client.methods.HttpRequestBase
import Requests._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Xml.toXml
import java.net.URL
import org.apache.http.auth.UsernamePasswordCredentials
import scalax.io.Input
import scalax.file.Path
import Keys._

class RestRequestBuilder(implicit baseURL: URL, credentials: UsernamePasswordCredentials) {

  def toRequest(path:String)(elem: ConfigElem): Seq[HttpRequestBase] = elem match {
    case c: Configuration => configurationToRequest(path, c)
    case ws: Workspace => workspaceToRequest(path, ws)
    case style: Style => styleToRequest(path, style)
    case layerGroup: LayerGroup => layerGroupToRequest(path, layerGroup)
    case shp: Shp => shpToRequest(path, shp)
    case dir: ShpDir => shpDirToRequest(path, dir)
    case postgis: Postgis => postgisToRequest(path, postgis)
/*    case database: Database => databaseToRequest(path, database)
    case table: Table => tableToRequest(path, table)
    case table: CreateTable => createTableToRequest(path, table)*/
    case raster: Raster => rasterToRequest(path, raster)
    case layer: VectorLayer => vectorLayerToRequest(path, layer)
    case layer: RasterLayer => rasterLayerToRequest(path, layer)
  }

  def configurationToRequest(path:String, config: Configuration) = {
    config.styles.flatMap(toRequest(path)) ++
      config.workspaces.flatMap(toRequest(path)) ++
      config.layergroups.flatMap(toRequest(path))
  }
  def workspaceToRequest(path:String, ws: Workspace) = {
    val updatedPath = "workspaces/"+ws.name
    val requests = ws.stores.flatMap(toRequest(updatedPath))
        
    if (ws.uri.isDefined) {
      post("namespaces", { "namespace" -> { ("prefix" -> ws.name) ~ ("uri" -> ws.uri) } }) +: requests
    } else {
      post("workspaces", { "workspace" -> { "name" -> ws.name } }) +: requests
    }
  }
  def shpDirToRequest(path:String, dir: ShpDir): Seq[HttpRequestBase] = {
    val filePath = "file://"+dir.path
    val datastoreJson:JObject = (dataStore -> {
      (Common.name -> dir.realName) ~
      (Store.description -> dir.description) ~
      (Store.connectionParameters -> {
        (Store.url -> filePath) ~ 
        (Store.charset -> dir.charset)})})

    val updatedPath = path+"/datastores/"+dir.realName
    post(path+"/datastores",datastoreJson) +: dir.layers.flatMap(toRequest(updatedPath))
  }
  def styleToRequest(path:String, style: Style): Seq[HttpRequestBase] = {
    val styleJson:JObject = 
      (Keys.style -> {
        (Common.name -> style.name) ~
        (Style.filename -> style.filename.getOrElse(style.name+".sld"))})
    
    def postSLD(in:Input) = {
      put("styles/"+style.name, in.byteArray, "application/vnd.ogc.sld+xml")
    } 
    List(post("styles",styleJson)) ++ style.resource.map(postSLD)
    
  }
  def layerGroupToRequest(path:String, layerGroup: LayerGroup): Seq[HttpRequestBase] = null
  def shpToRequest(path:String, shp: Shp) = {
    val filePath = "file://"+shp.path
    val datastoreJson:JObject = (dataStore -> {
      (Common.name -> shp.realName) ~
      (Store.description -> shp.description) ~
      (Store.connectionParameters -> {
        (Store.url -> filePath) ~ 
        (Store.charset -> shp.charset)})})

    val updatedPath = path+"/datastores/"+shp.realName
    post(path+"/datastores",datastoreJson) +: shp.layers.flatMap(toRequest(updatedPath))

  }
  def postgisToRequest(path:String, postgis: Postgis): Seq[HttpRequestBase] = {
    import c2c.geoserver.conf.Postgis._
    val datastoreJson:JObject =
      (dataStore -> {
        (Common.name -> postgis.realName) ~
        (Store.description -> postgis.description.getOrElse("")) ~
        (Store.connectionParameters -> {
          (Postgis.dbtype -> Postgis.dbtypeValue) ~
          (Postgis.host -> postgis.host) ~
          (Postgis.port -> postgis.port.getOrElse(defaultPort).toString) ~
          (Postgis.database -> postgis.database) ~
          (Postgis.schema -> postgis.schema.getOrElse(defaultSchema)) ~
          (Postgis.user -> postgis.username) ~
          (Postgis.pass -> postgis.realPass) ~
          (Postgis.timeout -> postgis.timeout.getOrElse(defaultTimeout).toString()) ~
          (Postgis.validateConnections -> postgis.validateConnections.getOrElse(defaultValidateConnections).toString()) ~
          (Postgis.maxConnections -> postgis.maxConnections.getOrElse(defaultMaxConnections).toString) ~
          (Postgis.minConnections -> postgis.minConnections.getOrElse(defaultMinConnections).toString) ~
          (Postgis.looseBBox -> postgis.looseBBox.getOrElse(defaultLooseBBox).toString()) ~
          (Postgis.fetchSize -> postgis.fetchSize.getOrElse(defaultFetchSize).toString()) ~
          (Postgis.exposePrimaryKeys -> postgis.exposePrimaryKeys.getOrElse(defaultExposePrimaryKeys).toString()) ~
          (Postgis.maxPreparedStatements -> postgis.maxPreparedStatements.getOrElse(defaultMaxPreparedStatements).toString()) ~
          (Postgis.preparedStatements -> postgis.preparedStatements.getOrElse(defaultPreparedStatements).toString()) ~
          (Postgis.estimateExtents -> postgis.estimateExtents.getOrElse(defaultEstimatedExtents).toString())
        })
      })

    val updatedPath = path+"/datastores/"+postgis.realName
    post(path+"/datastores",datastoreJson) +: postgis.layers.flatMap(toRequest(updatedPath))

  }
  /*def databaseToRequest(path:String, database: Database): Seq[HttpRequestBase] = null
  def tableToRequest(path:String, table: Table): Seq[HttpRequestBase] = null
  def createTableToRequest(path:String, table: CreateTable): Seq[HttpRequestBase] = null*/
  def rasterToRequest(path: String, raster: Raster): Seq[HttpRequestBase] = {
    val filePath = "file://" + raster.path
    val `type` = toClassName(raster)
    
    val coveragestoreJson: JObject = (coverageStore -> {
      (Common.name -> raster.realName) ~
      (Store.description -> raster.description) ~
      (Store.enabled -> raster.enabled.getOrElse(true).toString) ~
      (Store.`type` -> `type`) ~
      (Store.url -> filePath)})
    val updatedPath = path+"/coveragestores/"+raster.realName
    post(path+"/coveragestores",coveragestoreJson) +: raster.layers.flatMap(toRequest(updatedPath))
  }

  private def encodeBounds(bbox:List[Double]) = if(bbox.nonEmpty) {
        Some({
          ("minx" -> bbox(0)) ~
          ("miny" -> bbox(1)) ~
          ("maxx" -> bbox(2)) ~
          ("maxy" -> bbox(3))
        })
      } else {
        None
      } 
  def vectorLayerToRequest(path:String, layer: VectorLayer): Seq[HttpRequestBase] = {

    val layerJson: JObject = (featureType -> {
      ("name" -> layer.realName) ~
      ("nativeName" -> layer.nativeName) ~
      ("title" -> layer.title) ~
      ("abstract" -> layer.`abstract`) ~
      ("nativeCRS" -> layer.srs) ~
      ("nativeBoundingBox" -> encodeBounds(layer.bbox))
    })
    
    println(pretty(render(layerJson)))
    
    List(post(path+"/featuretypes", layerJson))
  }
  def rasterLayerToRequest(path:String, layer: RasterLayer): Seq[HttpRequestBase] = null
}