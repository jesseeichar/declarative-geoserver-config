package c2c.geoserver.conf

import net.liftweb.json._

object ReadConfiguration extends App {
  val url="localhost"
	val configurator = new GeoserverConfigurator("admin", "geoserver", "http://localhost:8180/geoserver/rest", "2.1.1")
	val config = configurator.readConfiguration
	println(JsonParser.serializeConfiguration(config))
}