package c2c.geoserver.conf
package version21x

import c2c.geoserver.{ conf => rconf }
import java.net.URI
import scalax.file.Path
import org.apache.http.auth.UsernamePasswordCredentials
import java.net.URL
import scalax.io.Resource

class ConfigParamExtractor(implicit baseURL: URL, credentials: UsernamePasswordCredentials) {
  val filePrefix = "file://"
  def urlToPath(url: String) = url match {
    case shpUrl if shpUrl startsWith filePrefix => shpUrl drop filePrefix.size
    case _ => url
  }
  def extractConfig() = {
    import GeoserverRequests._
    val uriMapping = GeoserverRequests.namespaces.flatMap { _.resolved.map(ns => ns.prefix -> ns.uri) }.toMap
    val ws = GeoserverRequests.workspaces map { ws => Workspace.create(uriMapping, ws)}
    val styles = GeoserverRequests.styles map { style => Style.create(style)}
    Configuration(workspaces = ws, styles = styles)
  }

  object Style {
    def create(ref:GeoserverJson.StyleRef) = {
      val sld = ref.href.dropRight("json".size)+"sld"
      rconf.Style(ref.name, filename=Some(ref.resolved.get.filename),url = Some(sld))
    }
  }
  object Workspace {
    def create(uriMapping:Map[String,String], ws:GeoserverJson.WorkspaceRef):rconf.Workspace = {
      rconf.Workspace(
          name = ws.name,
          uri = uriMapping.get(ws.name),
          stores = stores(ws))
    }
  def stores(ws: GeoserverJson.WorkspaceRef): List[Store] = {
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
  }
  object Shp {
    def unapply(ds: GeoserverJson.Datastore): Option[rconf.Shp] = {
      val params = ds.params
      params.get("url").filter(_.endsWith(".shp")).map { shpUrl =>
        val path = urlToPath(shpUrl)
        rconf.Shp(
            name = Some(ds.name), 
            description = ds.description, 
            path = path, 
            charset = params.get("charset"),
            layers = ds.parent.featureTypes.flatMap(_.resolved) map {case VectorLayer(layer) => layer})
      }

    }
  }
  object Raster {
    def unapply(ds: GeoserverJson.CoverageStore): Option[rconf.Raster] = {
      ds.`type` match {
        case t if t == className[rconf.GeoTIFF] => 
          Some(rconf.GeoTIFF(
              name = Some(ds.name), 
              description = ds.description, 
              path = urlToPath(ds.url))) 
        case t if t == className[rconf.Gtopo30] => 
          Some(rconf.Gtopo30(
              name = Some(ds.name), 
              description = ds.description, 
              path = urlToPath(ds.url))) 
        case t if t == className[rconf.ECW] => 
          Some(rconf.ECW(
              name = Some(ds.name), 
              description = ds.description, 
              path = urlToPath(ds.url))) 
        case t if t == className[rconf.ImageMosaic] => 
          Some(rconf.ImageMosaic(
              name = Some(ds.name), 
              description = ds.description, 
              path = urlToPath(ds.url))) 
        case t if t == className[rconf.WorldImage] => 
          Some(rconf.WorldImage(
              name = Some(ds.name), 
              description = ds.description, 
              path = urlToPath(ds.url))) 
        case t if t == className[rconf.ArcGrid] => 
          Some(rconf.ArcGrid(
              name = Some(ds.name), 
              description = ds.description, 
              path = urlToPath(ds.url))) 
        case t if t == className[rconf.WMS] => 
          Some(rconf.WMS(
              name = Some(ds.name), 
              description = ds.description, 
              path = urlToPath(ds.url))) 
        case t if t == className[rconf.JP2ECW] => 
          Some(rconf.JP2ECW(
              name = Some(ds.name), 
              description = ds.description, 
              path = urlToPath(ds.url))) 
      }
    }
  }
  object ShpDir {
    def unapply(ds: GeoserverJson.Datastore): Option[rconf.ShpDir] = {
      val params = ds.params
      params.get("url").filter(url => url.startsWith(filePrefix) && Path(new URI(url).getPath()).isDirectory).map { shpDir =>
        rconf.ShpDir(
            name = Some(ds.name), 
            description = ds.description, 
            path = urlToPath(shpDir), 
            charset = params.get("charset"),
            layers = ds.parent.featureTypes.flatMap(_.resolved) map {case VectorLayer(layer) => layer})
      }

    }
  }
  object Postgis {
    def unapply(ds: GeoserverJson.Datastore): Option[rconf.Postgis] = {
      import Keys.Postgis._
      val params = ds.params
      params.get(dbtype).filter(_ == dbtypeValue).map { _ =>
        rconf.Postgis(
          name = Some(ds.name),
          description = ds.description,
          host = params(host),
          port = params.get(port).map(_.toInt),
          database = params(database),
          username = params(user),
          password = Some(params(pass)),
          schema = params.get(schema),
          timeout = params.get(timeout).map(_.toInt),
          validateConnections = params.get(validateConnections).map(_.toBoolean),
          maxConnections = params.get(maxConnections).map(_.toInt),
          minConnections = params.get(minConnections).map(_.toInt),
          looseBBox = params.get(looseBBox).map(_.toBoolean),
          exposePrimaryKeys = params.get(exposePrimaryKeys).map(_.toBoolean),
          fetchSize = params.get(fetchSize).map(_.toInt),
          maxPreparedStatements = params.get(maxPreparedStatements).map(_.toInt),
          preparedStatements = params.get(preparedStatements).map(_.toBoolean),
          estimateExtents = params.get(estimateExtents).map(_.toBoolean),
          layers = ds.parent.featureTypes.flatMap(_.resolved) map {case VectorLayer(layer) => layer})
      }
    }
  }
  
  object VectorLayer {
    def unapply(featureType:GeoserverJson.FeatureType):Option[rconf.VectorLayer] = {
      import featureType.{nativeBoundingBox => bbox, latLonBoundingBox => llbbox}
      Some(rconf.VectorLayer(
          name = Some(featureType.name),
          nativeName = Some(featureType.nativeName),
          title = featureType.title,
          `abstract` = featureType.`abstract`,
          srs = featureType.srs,
          bbox = List(bbox.minx.toDouble, bbox.miny.toDouble, bbox.maxx.toDouble, bbox.maxy.toDouble),
          llbbox = List(llbbox.minx.toDouble, llbbox.miny.toDouble, llbbox.maxx.toDouble, llbbox.maxy.toDouble)))
    }
  }
}