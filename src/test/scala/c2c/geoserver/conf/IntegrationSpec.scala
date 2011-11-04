package c2c.geoserver.conf
import org.specs2.Specification
import java.net.URL
import org.apache.http.auth.UsernamePasswordCredentials

trait IntegrationSpec extends Specification {
  lazy val username = Option(System.getProperty("geoserver.username")) getOrElse "admin"
  lazy val password = Option(System.getProperty("geoserver.password")) getOrElse "geoserver"
  implicit def url = new URL(Option(System.getProperty("geoserver.url")) getOrElse "http://localhost:8080/geoserver/rest")
  implicit def credentials = new UsernamePasswordCredentials(username, password)

}