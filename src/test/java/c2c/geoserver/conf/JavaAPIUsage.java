package c2c.geoserver.conf;

/**
 * This class only has to compile.  It verifies that the api is useable from Java
 */
public class JavaAPIUsage {
	 @SuppressWarnings("unused")
	public static void method() {
		 Configuration conf = JsonParser.parseConfiguration("{}");
		 String json = JsonParser.serializeConfiguration(conf);
		 GeoserverConfigurator configurator = new GeoserverConfigurator("username", "password", "http://localhost:8080/geoserver/rest","2,1,x");
		 configurator.configure(conf);
		 Configuration newConfig = configurator.readConfiguration();
		 configurator.clearConfig();
	 }
}
