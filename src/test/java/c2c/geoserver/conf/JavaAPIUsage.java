package c2c.geoserver.conf;

/**
 * This class only has to compile.  It verifies that the api is useable from Java
 */
public class JavaAPIUsage {
	 @SuppressWarnings("unused")
	public static void method() {
		 Configuration conf = JsonParser.parse("{}");
		 String json = JsonParser.serialize(conf);
		 GeoserverConfigurator configurator = new GeoserverConfigurator("http://localhost:8080/geoserver/rest");
		 configurator.configure(conf);
		 Configuration newConfig = configurator.readConfiguration();
		 configurator.clearConfig();
	 }
}
