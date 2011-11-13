package c2c.geoserver.conf
package version21x

import org.apache.http.client.methods.HttpRequestBase
import Requests._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Xml.toXml
import java.net.URL
import org.apache.http.auth.UsernamePasswordCredentials
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
    case layer: Layer => layerToRequest(path, layer)
  }

  def configurationToRequest(path:String, config: Configuration) = {
    config.styles.flatMap(toRequest(path)) ++
      config.workspaces.flatMap(toRequest(path)) ++
      config.layergroups.flatMap(toRequest(path))
  }
  def workspaceToRequest(path:String, ws: Workspace) = {
    val updatedPath = "workspaces/"+ws.name
    val requests =
      post("workspaces", { "workspace" -> { "name" -> ws.name } }) +:
        ws.stores.flatMap(toRequest(updatedPath))
        
    if (ws.uri.isDefined) {
      val namespace = post("namespaces", { "namespace" -> { ("name" -> ws.name) ~ ("href" -> ws.uri) } })
      namespace +: requests
    } else {
      requests
    }
  }
  def shpDirToRequest(path:String, dir: ShpDir): Seq[HttpRequestBase] = {
    val filePath = "file://"+dir.path
    val datastoreJson:JObject = (dataStore -> {
      (Store.name -> dir.realName) ~
      (Store.description -> dir.description) ~
      (Store.connectionParameters -> {
        (Store.url -> filePath) ~ 
        (Store.charset -> dir.charset)})})

    List(post(path+"/datastores",datastoreJson))
  }
  def styleToRequest(path:String, style: Style): Seq[HttpRequestBase] = null
  def layerGroupToRequest(path:String, layerGroup: LayerGroup): Seq[HttpRequestBase] = null
  def shpToRequest(path:String, shp: Shp) = {
    val filePath = "file://"+shp.path
    val datastoreJson:JObject = (dataStore -> {
      (Store.name -> shp.realName) ~
      (Store.description -> shp.description) ~
      (Store.connectionParameters -> {
        (Store.url -> filePath) ~ 
        (Store.charset -> shp.charset)})})

    List(post(path+"/datastores",datastoreJson))
  }
  def postgisToRequest(path:String, postgis: Postgis): Seq[HttpRequestBase] = {
    import c2c.geoserver.conf.Postgis._
    val datastoreJson:JObject =
      (dataStore -> {
        (Store.name -> postgis.realName) ~
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

    List(post(path+"/datastores",datastoreJson))
  }
  /*def databaseToRequest(path:String, database: Database): Seq[HttpRequestBase] = null
  def tableToRequest(path:String, table: Table): Seq[HttpRequestBase] = null
  def createTableToRequest(path:String, table: CreateTable): Seq[HttpRequestBase] = null*/
  def rasterToRequest(path: String, raster: Raster): Seq[HttpRequestBase] = {
    val filePath = "file://" + raster.path
    val `type` = raster.getClass.getSimpleName
    
    val datastoreJson: JObject = (coverageStore -> {
      (Store.name -> raster.realName) ~
      (Store.description -> raster.description) ~
      (Store.enabled -> raster.enabled.getOrElse(true).toString) ~
      (Store.`type` -> `type`) ~
      (Store.url -> filePath)})
    List(post(path + "/coveragestores", datastoreJson))
  }

  def layerToRequest(path:String, layer: Layer): Seq[HttpRequestBase] = null
}