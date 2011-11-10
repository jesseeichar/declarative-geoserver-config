package c2c.geoserver.conf

import org.specs2.Specification
import org.specs2.execute.Result
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import GeoserverJson._

@RunWith(classOf[JUnitRunner])
class GeoserverRequestsIntegrationTest extends IntegrationSpec {
  def is =
    "Geoserserver Response JSON parsing".title ^
      "A Geoserver workspaces request should parse out into several workspaces" ! workspacesRequest ^
      "A workspace can be resolved to load the information of that workspace" ! resolveWorkspaces ^
      "A Geoserver namespaces request should parse out into several namespaces" ! namespacesRequest ^
      "A namespace can be resolved to load the information of that namespace" ! resolveNamespaces ^
      "A Geoserver datastores request should parse out into several datastores" ! datastoresRequest ^
      "A datastore can be resolved to load the information of that datastore" ! resolveDatastores ^
      "A Geoserver featureTypes request should parse out into several featureTypes" ! featureTypesRequest ^
      "A featureTypes can be resolved to load the information of that featureTypes" ! resolveFeatureTypes ^
      "A Geoserver coverageStore request should parse out into several coverageStore" ! coverageStoreRequest ^ 
      "A coverageStore can be resolved to load the information of that coverageStore" ! resolveCoverageStore ^
      "A Geoserver style request should parse out into several styles" ! stylesRequest ^ 
      "A style can be resolved to load the information of that style" ! resolveStyle ^
      "A Geoserver layer request should parse out into several layers" ! layersRequest ^ 
      "A layer can be resolved to load the information of that layer" ! resolveLayer ^
      "A Geoserver layergroup request should parse out into several layergroups" ! layergroupsRequest ^ 
      "A layergroup can be resolved to load the information of that layergroup" ! resolveLayerGroups

  lazy val workspaces = GeoserverRequests.workspaces
  lazy val namespaces = GeoserverRequests.namespaces
  lazy val datastores = workspaces.flatMap{_.datastores}
  lazy val featuretypes = datastores.flatMap{_.featureTypes}
  lazy val coverageStores = workspaces.flatMap{_.coverageStores}
  lazy val coverages = coverageStores.flatMap{_.coverages}
  lazy val styles = GeoserverRequests.styles
  lazy val layers = GeoserverRequests.layers
  lazy val layergroups = GeoserverRequests.layergroups
  
  def workspacesRequest = workspaces must not beEmpty
  def resolveWorkspaces = forall[WorkspaceRef](workspaces,ws => ws.resolved.map(_.name) +" != "+ws.name, ws => ws.resolved.forall(_.name == ws.name))
  def namespacesRequest = namespaces must not beEmpty
  def resolveNamespaces = forall[NamespaceRef](namespaces,ns => ns.resolved.map(_.prefix) +" != "+ns.name, ns => ns.resolved.forall(_.prefix == ns.name))
  def datastoresRequest = datastores must not beEmpty
  def resolveDatastores = forall[DatastoreRef](datastores, ds => ds.resolved.map(_.name) +" != "+ds.name, ds => ds.resolved.forall(_.name == ds.name))
  def featureTypesRequest = featuretypes must not beEmpty
  def resolveFeatureTypes = forall[FeatureTypeRef](featuretypes, ft => ft.resolved.map(_.name) +" != "+ft.name, ft => ft.resolved.forall(_.name == ft.name))
  def coverageStoreRequest = coverageStores must not beEmpty
  def resolveCoverageStore = forall[CoverageRef](coverages, c => c.resolved.map(_.name) +" != "+c.name, c => c.resolved.forall(_.name == c.name))
  def stylesRequest = styles must not beEmpty
  def resolveStyle = forall[StyleRef](styles, s => s.resolved.map(_.name) +" != "+s.name, s => s.resolved.forall(_.name == s.name))
  def layersRequest = layers must not beEmpty
  def resolveLayer = forall[LayerRef](layers, s => s.resolved.map(_.name) +" != "+s.name, s => s.resolved.forall(_.name == s.name))
  def layergroupsRequest = layergroups must not beEmpty
  def resolveLayerGroups = forall[LayerGroupRef](layergroups, s => s.resolved.map(_.name) +" != "+s.name, s => s.resolved.forall(_.name == s.name))
 
  def forall[U](collection:Traversable[U], msg:U=>String, f:U=>Boolean) = {
    collection.foldLeft(success:Result) { case (acc, next) =>
      acc and (f(next) aka msg(next) must beTrue)
    }
  }

}