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
import org.apache.http.entity.StringEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.auth.params.AuthPNames
import org.apache.http.client.methods.HttpDelete
import scala.xml.NodeSeq

object Requests {
  def executeOne(request: HttpRequestBase)(implicit credentials:UsernamePasswordCredentials) = executeMany(List(request)).head
  def executeMany(requests: Traversable[HttpRequestBase])(implicit credentials:UsernamePasswordCredentials) = {
    import AuthPolicy._

    val httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager())
    httpClient.getParams.setParameter(AuthPNames.TARGET_AUTH_PREF, BASIC :: DIGEST :: Nil asJava)
    httpClient.getCredentialsProvider.setCredentials(AuthScope.ANY, credentials)

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

    requests.map{r =>
      println("executing "+r.getMethod+" request: "+r.getURI())
      httpClient.execute(r, localContext)}
    
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
  def post(urlPath: String, json: JValue)(implicit baseURL: URL) = doCreatePost(urlPath, compact(render(json)), "text/json")
  def post(urlPath: String, xml: NodeSeq)(implicit baseURL: URL) = doCreatePost(urlPath, xml.toString, "application/xml")

  private def doCreatePost(urlPath: String, data: String, contentType: String)(implicit baseURL: URL): HttpPost = {
    val uri = createURI(urlPath)
    val post = new HttpPost(uri)
    post.setEntity(new StringEntity(data, "UTF-8"));
    post.setHeader("Content-type", contentType);
    post
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