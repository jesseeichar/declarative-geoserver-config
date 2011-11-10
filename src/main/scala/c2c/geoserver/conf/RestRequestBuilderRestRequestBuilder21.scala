package c2c.geoserver.conf
import org.apache.http.client.methods.HttpRequestBase
import Requests._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Xml.toXml
import java.net.URL
import org.apache.http.auth.UsernamePasswordCredentials
import scalax.file.Path

class RestRequestBuilder21(implicit baseURL: URL, credentials: UsernamePasswordCredentials) {

  def toRequest(path:String)(elem: ConfigElem): Seq[HttpRequestBase] = elem match {
    case c: Configuration => configurationToRequest(path, c)
    case ws: Workspace => workspaceToRequest(path, ws)
    case style: Style => styleToRequest(path, style)
    case layerGroup: LayerGroup => layerGroupToRequest(path, layerGroup)
    case shp: Shp => shpToRequest(path, shp)
    case dir: ShpDir => shpDirToRequest(path, dir)
    case postgis: Postgis => postgisToRequest(path, postgis)
    case database: Database => databaseToRequest(path, database)
    case table: Table => tableToRequest(path, table)
    case table: CreateTable => createTableToRequest(path, table)
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
    val datastoreJson:JObject = ("dataStore" -> {
      ("name" -> dir.realName) ~
      ("connectionParameters" -> {
        ("url" -> filePath) ~ 
        ("charset" -> dir.charset)})})

    List(post(path+"/datastores",datastoreJson))
  }
  def styleToRequest(path:String, style: Style): Seq[HttpRequestBase] = null
  def layerGroupToRequest(path:String, layerGroup: LayerGroup): Seq[HttpRequestBase] = null
  def shpToRequest(path:String, shp: Shp) = {
    val filePath = "file://"+shp.path
    val datastoreJson:JObject = ("dataStore" -> {
      ("name" -> shp.realName) ~
      ("connectionParameters" -> {
        ("url" -> filePath) ~ 
        ("charset" -> shp.charset)})})

    List(post(path+"/datastores",datastoreJson))
  }
  def postgisToRequest(path:String, postgis: Postgis): Seq[HttpRequestBase] = {
    import Postgis._
    val datastoreJson:JObject =
      ("dataStore" -> {
        ("name" -> postgis.realName) ~
        ("description" -> postgis.description.getOrElse("")) ~
        ("connectionParameters" -> {
          ("dbtype" -> "postgis") ~
          ("host" -> postgis.host) ~
          ("port" -> postgis.port.getOrElse(defaultPort).toString) ~
          ("database" -> postgis.database) ~
          ("schema" -> postgis.schema.getOrElse(defaultSchema)) ~
          ("user" -> postgis.username) ~
          ("passwd" -> postgis.realPass) ~
          ("Connection timeout" -> postgis.timeout.getOrElse(defaultTimeout).toString()) ~
          ("validate connections" -> postgis.validateConnections.getOrElse(defaultValidateConnections).toString()) ~
          ("max connections" -> postgis.maxConnections.getOrElse(defaultMaxConnections).toString) ~
          ("min connections" -> postgis.minConnections.getOrElse(defaultMinConnections).toString) ~
          ("Loose bbox" -> postgis.looseBBox.getOrElse(defaultLooseBBox).toString()) ~
          ("fetch size" -> postgis.fetchSize.getOrElse(defaultFetchSize).toString()) ~
          ("Expose primary keys" -> postgis.exposePrimaryKeys.getOrElse(defaultExposePrimaryKeys).toString()) ~
          ("Max open prepared statements" -> postgis.maxPreparedStatements.getOrElse(defaultMaxPreparedStatements).toString()) ~
          ("preparedStatements" -> postgis.preparedStatements.getOrElse(defaultPreparedStatements).toString()) ~
          ("Estimated extends"-> postgis.estimateExtents.getOrElse(defaultEstimatedExtents).toString())
        })
      })

      println(pretty(render(datastoreJson)))
    List(post(path+"/datastores",datastoreJson))
  }
  def databaseToRequest(path:String, database: Database): Seq[HttpRequestBase] = null
  def tableToRequest(path:String, table: Table): Seq[HttpRequestBase] = null
  def createTableToRequest(path:String, table: CreateTable): Seq[HttpRequestBase] = null
  def rasterToRequest(path:String, raster: Raster): Seq[HttpRequestBase] = null
  def layerToRequest(path:String, layer: Layer): Seq[HttpRequestBase] = null
}