package c2c.geoserver.conf
package version21x

import c2c.geoserver.{ conf => rconf }
import java.net.URI
import scalax.file.Path

object ConfigParamExtractor {
  val filePrefix = "file://"
  def urlToPath(url: String) = url match {
    case shpUrl if shpUrl startsWith filePrefix => shpUrl drop filePrefix.size
    case _ => url
  }
  object Shp {
    def unapply(ds: GeoserverJson.Datastore): Option[rconf.Shp] = {
      val params = ds.params
      params.get("url").filter(_.endsWith(".shp")).map { shpUrl =>
        val path = urlToPath(shpUrl)
        rconf.Shp(name = Some(ds.name), description = ds.description, path = path, charset = params.get("charset"))
      }

    }
  }
  object Raster {
    private def name[M](implicit m: Manifest[M]) = {
      val name = m.erasure.getSimpleName()
      if(name.endsWith("$")) name.dropRight(1)
      else name
    } 
    def unapply(ds: GeoserverJson.CoverageStore): Option[rconf.Raster] = {
      ds.`type` match {
        case t if t == name[rconf.GeoTIFF] => 
          Some(rconf.GeoTIFF(name = Some(ds.name), description = ds.description, path = urlToPath(ds.url))) 
        case t if t == name[rconf.Gtopo30] => 
          Some(rconf.Gtopo30(name = Some(ds.name), description = ds.description, path = urlToPath(ds.url))) 
        case t if t == name[rconf.ECW] => 
          Some(rconf.ECW(name = Some(ds.name), description = ds.description, path = urlToPath(ds.url))) 
        case t if t == name[rconf.ImageMosaic] => 
          Some(rconf.ImageMosaic(name = Some(ds.name), description = ds.description, path = urlToPath(ds.url))) 
        case t if t == name[rconf.WorldImage] => 
          Some(rconf.WorldImage(name = Some(ds.name), description = ds.description, path = urlToPath(ds.url))) 
        case t if t == name[rconf.Arcgrid] => 
          Some(rconf.Arcgrid(name = Some(ds.name), description = ds.description, path = urlToPath(ds.url))) 
        case t if t == name[rconf.WMS] => 
          Some(rconf.WMS(name = Some(ds.name), description = ds.description, path = urlToPath(ds.url))) 
      }
      
    }
  }
  object GeoTIFF {
	  def unapply(ds: GeoserverJson.CoverageStore): Option[rconf.GeoTIFF] = {
			  Some(rconf.GeoTIFF(name = Some(ds.name), description = ds.description, path = urlToPath(ds.url)))
	  }
  }
  object ShpDir {
    def unapply(ds: GeoserverJson.Datastore): Option[rconf.ShpDir] = {
      val params = ds.params
      params.get("url").filter(url => url.startsWith(filePrefix) && Path(new URI(url).getPath()).isDirectory).map { shpDir =>
        rconf.ShpDir(name = Some(ds.name), description = ds.description, path = urlToPath(shpDir), charset = params.get("charset"))
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
          estimateExtents = params.get(estimateExtents).map(_.toBoolean))
      }

    }
  }
}