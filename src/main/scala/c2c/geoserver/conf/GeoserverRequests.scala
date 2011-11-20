package c2c.geoserver.conf
import java.net.URL
import org.apache.http.auth.UsernamePasswordCredentials

trait GeoserverRequests {
  def workspaces(implicit baseURL: URL, credentials: UsernamePasswordCredentials): List[GeoserverJson.WorkspaceRef]
  def namespaces(implicit baseURL: URL, credentials: UsernamePasswordCredentials): List[GeoserverJson.NamespaceRef]
  def styles(implicit baseURL: URL, credentials: UsernamePasswordCredentials): List[GeoserverJson.StyleRef]
  def layers(implicit baseURL: URL, credentials: UsernamePasswordCredentials): List[GeoserverJson.LayerRef]
  def layergroups(implicit baseURL: URL, credentials: UsernamePasswordCredentials): List[GeoserverJson.LayerGroupRef]
}

object GeoserverJson {
  trait ModelElem {
    def name: String
    def href: String
  }
  trait WorkspaceRef extends ModelElem {
    def coverageStores: List[CoverageStoreRef]
    def datastores: List[DataStoreRef]
  }
  trait NamespaceRef extends ModelElem
  trait StyleRef extends ModelElem
  trait LayerRef extends ModelElem
  trait LayerGroupRef extends ModelElem

  trait CoverageStoreRef extends ModelElem {
    def coverages: List[CoverageRef]
  }
  trait DataStoreRef extends ModelElem {
    def featureTypes: List[FeatureTypeRef]
  }
  trait CoverageRef extends ModelElem
  trait FeatureTypeRef extends ModelElem
}