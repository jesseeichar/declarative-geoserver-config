package c2c.geoserver.conf

import collection.JavaConverters._
import org.apache.http.client.params.AuthPolicy
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.auth.params.AuthPNames
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.auth.AuthScope
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.client.protocol.ClientContext
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpHost
import org.apache.http.client.methods.HttpRequestBase
import net.liftweb.json._
import java.net.URL
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.auth.params.AuthPNames
import org.apache.http.client.methods.HttpDelete
import scala.xml.NodeSeq
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.methods.HttpPut
import org.apache.http.params.CoreConnectionPNames

object Requests {
  def executeOne(request: HttpRequestBase)(implicit credentials:UsernamePasswordCredentials) = executeMany(List(request)).head
  def executeMany(requests: Traversable[HttpRequestBase])(implicit credentials:UsernamePasswordCredentials) = {
    import AuthPolicy._
    import CoreConnectionPNames._
    val httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager())
    httpClient.getParams.setParameter(AuthPNames.TARGET_AUTH_PREF, BASIC :: DIGEST :: Nil asJava)
    httpClient.getCredentialsProvider.setCredentials(AuthScope.ANY, credentials)
    httpClient.getParams.setIntParameter(SO_TIMEOUT, 20000).
    					 setIntParameter(CONNECTION_TIMEOUT, 20000)
    
    
    httpClient.getConnectionManager().getSchemeRegistry().register(SSLUtilities.fakeSSLScheme(443))
    httpClient.getConnectionManager().getSchemeRegistry().register(SSLUtilities.fakeSSLScheme(8443))
    httpClient.getConnectionManager().asInstanceOf[ThreadSafeClientConnManager].setDefaultMaxPerRoute(10)
    httpClient.getConnectionManager().asInstanceOf[ThreadSafeClientConnManager].setMaxTotal(50)

    val localContext = new BasicHttpContext()
    // Create AuthCache instance
    val authCache = new BasicAuthCache()
    // Generate BASIC scheme object and add it to the local auth cache
    val basicAuth = new BasicScheme()
    requests.foreach(r => authCache.put(new HttpHost(r.getURI().getHost()), basicAuth))
    // Add AuthCache to the execution context
    localContext.setAttribute(ClientContext.AUTH_CACHE, authCache)

    requests.flatMap{r =>
      println("executing "+r.getMethod+" request: "+r.getURI())
      val result = util.control.Exception.catching(classOf[Exception]).either(
          httpClient.execute(r, localContext))
          
      result.fold(
          exception => {
        	  println("Request failed with exception "+exception)
        	  None
          },
          result => {
		      val code = result.getStatusLine().getStatusCode()
		      if(code >= 300) {
		        val reason = result.getStatusLine().getReasonPhrase()
		        println("unexpected result code: "+code+".  Reason:\n\t"+reason)
		      }
		      Some(result)})
      }
  }

  private def createURI(urlPath:String)(implicit baseURL: URL) = 
    if(urlPath.startsWith("http")) urlPath
    else (baseURL.toExternalForm() + "/" + urlPath) 
  private def createURIWithParams(urlPath:String, params: Seq[(String, Any)])(implicit baseURL: URL) = {
    
    val paramString = params.map { case (key, value) => key.trim + "=" + value.toString.trim }.mkString("?", "&", "").trim match {
      case "?" => ""
      case params => params
    }
    createURI(urlPath) + paramString
  }
  
  def post(urlPath: String, json: JValue)(implicit baseURL: URL): HttpPost = post(urlPath, compact(render(json)).getBytes("UTF-8"), "text/json")
  def post(urlPath: String, xml: NodeSeq)(implicit baseURL: URL): HttpPost = post(urlPath, xml.toString.getBytes("UTF-8"), "application/xml")
  def post(urlPath: String, data: Array[Byte], contentType: String)(implicit baseURL: URL): HttpPost = doCreateUploadRequest(new HttpPost(_:String), baseURL)(urlPath,data,contentType)
  
  def put(urlPath: String, json: JValue)(implicit baseURL: URL): HttpPut = put(urlPath, compact(render(json)).getBytes("UTF-8"), "text/json")
  def put(urlPath: String, xml: NodeSeq)(implicit baseURL: URL): HttpPut = put(urlPath, xml.toString.getBytes("UTF-8"), "application/xml")
  def put(urlPath: String, data: Array[Byte], contentType: String)(implicit baseURL: URL): HttpPut = doCreateUploadRequest(new HttpPut(_:String), baseURL)(urlPath,data,contentType)
  
  private def doCreateUploadRequest[R <: HttpEntityEnclosingRequestBase](factory: String => R, baseURL: URL)(urlPath: String, data: Array[Byte], contentType: String) = {
    val uri = createURI(urlPath)(baseURL)
    val request = factory(uri)
    request.setEntity(new ByteArrayEntity(data));
    request.setHeader("Content-type", contentType);
    request
    
  }

  def get(urlPath: String, params: (String, Any)*)(implicit baseURL: URL) = {
    val uri = createURIWithParams(urlPath,params)
    new HttpGet(uri)
  }
  
  def delete(urlPath:String, params: (String, Any)*)(implicit baseURL: URL) = {
    val uri = createURIWithParams(urlPath,params)
    new HttpDelete(uri)
  }
}