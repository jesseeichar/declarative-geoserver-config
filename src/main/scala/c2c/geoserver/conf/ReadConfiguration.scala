package c2c.geoserver.conf

import net.liftweb.json._

object ReadConfiguration extends App {
	val configurator = new GeoserverConfigurator("snegre", "pyrenees", "http://ids.pigma.org/geoserver/rest", "2.1.1")
	val config = configurator.readConfiguration
	println(JsonParser.serializeConfiguration(config))
}