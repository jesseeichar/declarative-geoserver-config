package c2c.geoserver.conf

import c2c.geoserver.{ conf => rconf }
import java.net.URI
import scalax.file.Path

object ConfigParamExtractor {
  val filePrefix = "file://"
  object Shp {
    def unapply(ds: GeoserverJson.Datastore): Option[rconf.Shp] = {
      val params = ds.params
      params.get("url").filter(_.endsWith(".shp")).map { shpUrl =>
        val path = shpUrl match {
          case shpUrl if shpUrl startsWith filePrefix => shpUrl drop filePrefix.size
          case _ => shpUrl
        }
        rconf.Shp(name=Some(ds.name),   path = path, charset = params.get("charset"))
      }

    }
  }
  object ShpDir {
    def unapply(ds: GeoserverJson.Datastore): Option[rconf.ShpDir] = {
      val params = ds.params
      params.get("url").filter(url => url.startsWith(filePrefix) && Path(new URI(url).getPath()).isDirectory).map { shpDir =>
        val path = shpDir match {
          case shpUrl if shpUrl startsWith filePrefix => shpUrl drop filePrefix.size
          case _ => shpDir
        }
        
        rconf.ShpDir(name=Some(ds.name), path = path, charset = params.get("charset"))
      }

    }
  }
}