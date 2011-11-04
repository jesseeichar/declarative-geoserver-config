package c2c.geoserver.conf
import java.net.URL
import scalax.io.Resource
import org.apache.http.auth.UsernamePasswordCredentials

object GeoserverRequests {
  def workspaces(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = {
    val get = Requests.get("workspaces.json")
    val response = Requests.executeOne(get)
    val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
    JsonParser.parse[GeoserverWorkspacesResponse](jsonString).workspaces.workspace
  }

  def datastores(workspace: WorkspaceRef)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = {
    val get = Requests.get("workspaces/" + workspace.name + "/datastores.json")
    val response = Requests.executeOne(get)
    val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
    JsonParser.parse[GeoserverDatastoresResponse](jsonString).datastores.datastore
  }

  def featuretypes(workspace: WorkspaceRef, datastore: DatastoreRef)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = {
    val get = Requests.get("workspaces/" + workspace.name + "/datastores/" + datastore.name + "/featuretypes.json")
    val response = Requests.executeOne(get)
    val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
    JsonParser.parse[GeoserverFeatureTypesResponse](jsonString).featureTypes.featureType
  }

  def coveragestores(workspace: WorkspaceRef)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = {
    val get = Requests.get("workspaces/" + workspace.name + "/coveragestores.json")
    val response = Requests.executeOne(get)
    val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
    JsonParser.parse[GeoserverCoveragesStoreResponse](jsonString).coverageStores.coverageStore
  }

  def coverages(workspace: WorkspaceRef, coverageStore: CoverageStoreRef)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = {
    val get = Requests.get("workspaces/" + workspace.name + "/coveragestores/" + coverageStore.name + "/coverages.json")
    val response = Requests.executeOne(get)
    val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
    JsonParser.parse[GeoserverCoveragesResponse](jsonString).coverages.coverage
  }
}

/* --  The following are extra classes needed to parse geoserver rest responses --*/
case class GeoserverWorkspacesResponse(workspaces: Workspaces) extends JsonElem
case class Workspaces(workspace: List[WorkspaceRef])
case class WorkspaceRef(name: String, href: String)

case class GeoserverDatastoresResponse(datastores: Datastores) extends JsonElem
case class Datastores(datastore: List[DatastoreRef])
case class DatastoreRef(name: String, href: String)

case class GeoserverFeatureTypesResponse(featureTypes: FeatureTypes) extends JsonElem
case class FeatureTypes(featureType: List[FeatureTypeRef])
case class FeatureTypeRef(name: String, href: String)

case class GeoserverCoveragesStoreResponse(coverageStores: CoverageStores) extends JsonElem
case class CoverageStores(coverageStore: List[CoverageStoreRef])
case class CoverageStoreRef(name: String, href: String)

case class GeoserverCoveragesResponse(coverages: Coverages) extends JsonElem
case class Coverages(coverage: List[CoverageRef])
case class CoverageRef(name: String, href: String)
