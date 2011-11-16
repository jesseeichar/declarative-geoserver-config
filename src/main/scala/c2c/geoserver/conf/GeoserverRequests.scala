package c2c.geoserver.conf
import java.net.URL
import scalax.io.Resource
import org.apache.http.auth.UsernamePasswordCredentials
import GeoserverJson._
import net.liftweb.json._

object GeoserverRequests {
  implicit val formats = net.liftweb.json.DefaultFormats

  def parseErrorHandler(response: String): PartialFunction[Throwable, Nothing] = {
    case e =>
      println("Unable to parse: \n" + response)
      throw e
  }
  def workspaces(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = {
    val get = Requests.get("workspaces.json")
    val response = Requests.executeOne(get)
    val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
    try { (parse(jsonString) \ "workspaces" \ "workspace").extract[List[WorkspaceRawRef]].map(_.toRef) }
    catch parseErrorHandler(jsonString)
  }
    def namespaces(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = {
    val get = Requests.get("namespaces.json")
    val response = Requests.executeOne(get)
    val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
    try { (parse(jsonString) \ "namespaces" \ "namespace").extract[List[NamespaceRawRef]].map(_.toRef) }
    catch parseErrorHandler(jsonString)
  }
  
  def styles(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = {
    val get = Requests.get("styles.json")
    val response = Requests.executeOne(get)
    val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
    try { (parse(jsonString) \ "styles" \ "style").extract[List[StyleRawRef]].map(_.toRef) }
    catch parseErrorHandler(jsonString)
  }
  def layers(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = {
    val get = Requests.get("layers.json")
    val response = Requests.executeOne(get)
    val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
    try { (parse(jsonString) \ "layers" \ "layer").extract[List[LayerRawRef]].map(_.toRef) }
    catch parseErrorHandler(jsonString)
  }
  def layergroups(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = {
    val get = Requests.get("layergroups.json")
    val response = Requests.executeOne(get)
    val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
    try { (parse(jsonString) \ "layerGroups" \ "layerGroup").extract[List[LayerGroupRawRef]].map(_.toRef) }
    catch parseErrorHandler(jsonString)
  }
}

/* --  The following are extra classes needed to parse geoserver rest responses --*/
object GeoserverJson {
  import GeoserverRequests.parseErrorHandler
  protected implicit val formats = net.liftweb.json.DefaultFormats
  trait ModelElem[P,T <: ModelElem[P,_]] {
    private var _parent:P = _
    private[GeoserverJson] def setParent(parent:P):T = {
      _parent = parent
      this.asInstanceOf[T]
    }
    def parent = _parent
  }
  abstract class Ref[P,T <: ModelElem[P,T]](pathToElement: String)(implicit baseURL: URL, credentials: UsernamePasswordCredentials, m: Manifest[T]) {
    self:P =>
    def href: String

    lazy val resolved: Option[T] = {
      val response = Requests.executeOne(Requests.get(href))
      val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
      if (jsonString.trim().isEmpty()) None
      else {
        try Some((parse(jsonString) \ pathToElement).extract[T].setParent(this))
        catch parseErrorHandler(compact(render(parse(jsonString) \ pathToElement)))
      }
    }
  }
  case class NamespaceRawRef(name: String, href: String) {
    def toRef(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = new NamespaceRef(name, href)
  }
  class NamespaceRef(val name: String, val href: String)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) extends Ref[NamespaceRef,Namespace]("namespace")
  case class Namespace(prefix:String, uri:String, featureTypes:String) extends ModelElem[NamespaceRef,Namespace]

  case class WorkspaceRawRef(name: String, href: String) {
    def toRef(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = new WorkspaceRef(name, href)
  }
  class WorkspaceRef(val name: String, val href: String)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) extends Ref[WorkspaceRef, Workspace]("workspace") {
    lazy val datastores = {
      val option = resolved.map { r =>
        val get = Requests.get(r.dataStores)
        val response = Requests.executeOne(get)
        val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
        try { (parse(jsonString) \ "dataStores" \ "dataStore").extract[List[DatastoreRawRef]].map(_.toRef) }
        catch parseErrorHandler(jsonString)
      }
      option.getOrElse(Nil)
    }
    lazy val coverageStores = {
      val option = resolved.map { r =>
        val get = Requests.get(r.coverageStores)
        val response = Requests.executeOne(get)
        val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
        try { (parse(jsonString) \ "coverageStores" \ "coverageStore").extract[List[CoverageStoreRawRef]].map(_.toRef) }
        catch parseErrorHandler(jsonString)
      }
      option.getOrElse(Nil)
    }
  }
  case class Workspace(name: String, dataStores: String, coverageStores: String) extends ModelElem[WorkspaceRef, Workspace]

  case class DatastoreRawRef(name: String, href: String) {
    def toRef(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = new DatastoreRef(name, href)
  }
  class DatastoreRef(val name: String, val href: String)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) extends Ref[DatastoreRef, Datastore]("dataStore") {
    lazy val featureTypes = {
      val option = resolved.map { r =>
        val get = Requests.get(r.featureTypes)
        val response = Requests.executeOne(get)
        val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
        try { (parse(jsonString) \ "featureTypes" \ "featureType").extract[List[FeatureTypeRawRef]].map(_.toRef) }
        catch parseErrorHandler(jsonString)
      }
      option.getOrElse(Nil)
    }
  }
  private def toParams(connectionParameters: JObject) = {
    def geoserverParams(connectionParameters: JObject) = {
      val entries = 
        (connectionParameters \ "entry").extract[List[Map[String,String]]].map { map =>
          map("@key") -> map("$")
        }
      entries.toMap
    }
    
    if((connectionParameters \ "entry" \ "@key" \ classOf[JField]).nonEmpty) geoserverParams(connectionParameters)
    else Map[String,String]()
  }
  case class Datastore(name: String, description:Option[String], enabled: Boolean, connectionParameters: JObject, __default: Boolean, featureTypes: String) extends ModelElem[DatastoreRef, Datastore] {
    lazy val params:Map[String,String] = toParams(connectionParameters) 
  }

  case class FeatureTypeRawRef(name: String, href: String) {
    def toRef(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = new FeatureTypeRef(name, href)
  }
  class FeatureTypeRef(val name: String, val href: String)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) extends Ref[FeatureTypeRef, FeatureType]("featureType")
  case class FeatureType(
    name: String, nativeName: String, namespace: FeatureTypeNamespace, title: Option[String], `abstract`: Option[String],
    keywords: Keywords, nativeCRS: Option[String], latLonBoundingBox: BBox, projectionPolicy: String,
    enabled: Boolean, metadata: Option[JObject], store: FeatureTypeRawRef, attributes: Attributes,
    maxFeatures: Int, numDecimals: Int) extends ModelElem[FeatureTypeRef, FeatureType]

  case class Attributes(attribute: JValue) {
    def attributes = attribute match {
      case array: JArray => array.extract[List[Attribute]]
      case obj: JObject => List(obj.extract[Attribute])
      case _ => throw new AssertionError(attribute + " was expected to be either an JArray or JObject")
    }
  }
  case class Attribute(name: String, minOccurs: Int, maxOccurs: Int, nillable: Boolean, binding: String)

  case class BBox(minx: Double, miny: Double, maxx: Double, maxy: Double, crs: String)
  case class FeatureTypeNamespace(name: String, href: String)
  case class Keywords(strings: List[String])

  case class CoverageStoreRawRef(name: String, href: String) {
    def toRef(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = new CoverageStoreRef(name, href)
  }
  class CoverageStoreRef(val name: String, val href: String)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) extends Ref[CoverageStoreRef, CoverageStore]("coverageStore") {
    def coverages = {
      val option = resolved.map { r =>
        val get = Requests.get(r.coverages)
        val response = Requests.executeOne(get)
        val jsonString = Resource.fromInputStream(response.getEntity().getContent()).slurpString()
        try { (parse(jsonString) \ "coverages" \ "coverage").extract[List[CoverageRawRef]].map(_.toRef) }
        catch parseErrorHandler(jsonString)
      }
      option.getOrElse(Nil)
    }
  }
  case class CoverageStore(name: String, description:Option[String], `type`: String, enabled: Boolean, workspace: WorkspaceRawRef, __default: Boolean, url: String, coverages: String)
  	extends ModelElem[CoverageStoreRef, CoverageStore]

  case class CoverageRawRef(name: String, href: String) {
    def toRef(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = new CoverageRef(name, href)
  }
  class CoverageRef(val name: String, val href: String)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) extends Ref[CoverageRef, Coverage]("coverage")
  case class Coverage(
    name: String, nativeName: String, namespace: FeatureTypeNamespace, title: String, description: String,
    keywords: Keywords, /*nativeCRS: Option[String],nativeBoundingBox:NativeBBox*/ latLonBoundingBox: BBox,
    enabled: Boolean, metadata: JObject, store: FeatureTypeRawRef, grid: Grid, supportedFormats: Formats, interpolationMethods: InterpolationMethods,
    defaultInterpolationMethod: String, dimensions: Dimensions, requestSRS: SRSList, responseSRS: SRSList) extends ModelElem[CoverageRef, Coverage]

  case class Grid(`@dimension`: String, range: HiLowRange, transform: Transform, crs: String) { val dimension = `@dimension` }
  case class HiLowRange(low: String, high: String)
  case class Transform(scaleX: String, scaleY: String, shearX: String, shearY: String, translateX: String, translateY: String)
  case class Formats(string: List[String])
  case class InterpolationMethods(string: List[String])
  case class Dimensions(coverageDimension: JValue) {
    def coverageDimensions = coverageDimension match {
      case array: JArray => array.extract[List[CoverageDimension]]
      case obj: JObject => List(obj.extract[CoverageDimension])
      case _ => throw new AssertionError(coverageDimension + " was expected to be either an JArray or JObject")
    }
  }
  case class CoverageDimension(name: String, description: String, range: MinMaxRange)
  case class MinMaxRange(min: String, max: String)
  case class SRSList(string: List[String])

  case class StyleRawRef(name: String, href: String) {
    def toRef(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = new StyleRef(name, href)
  }
  class StyleRef(val name: String, val href: String)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) extends Ref[StyleRef, Style]("style")
  case class Style(name: String, sldVersion: Version, filename: String) extends ModelElem[StyleRef, Style]
  case class Version(version: String)

  case class LayerRawRef(name: String, href: String) {
    def toRef(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = new LayerRef(name, href)
  }
  class LayerRef(val name: String, val href: String)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) extends Ref[LayerRef, Layer]("layer")
  case class Layer(name: String, path: Option[String], `type`: String, defaultStyle: StyleRawRef, styles: Styles, resource: JObject, enabled: Boolean, metadata: Option[JObject], attribution: Attribution)
  	extends ModelElem[LayerRef, Layer]
  case class Attribution(title: Option[String], href: Option[String], logoURL: Option[String], logoType: Option[String], logoWidth: Option[Int], logoHeight: Option[Int])
  case class Styles(style: List[StyleRawRef])

  case class LayerGroupRawRef(name: String, href: String) {
    def toRef(implicit baseURL: URL, credentials: UsernamePasswordCredentials) = new LayerGroupRef(name, href)
  }
  class LayerGroupRef(val name: String, val href: String)(implicit baseURL: URL, credentials: UsernamePasswordCredentials) extends Ref[LayerGroupRef, LayerGroup]("layerGroup")
  case class LayerGroup(name:String, layers:Layers, style:Styles, bounds:Option[BBox]) extends ModelElem[LayerGroupRef, LayerGroup]
  case class Layers(layer:List[LayerRawRef])
}