package c2c.geoserver.conf.version21x

object Keys {
  val dataStore = "dataStore"
  val coverageStore = "coverageStore"
  object Store {
    val name = "name"
    val description = "description"
    val connectionParameters = "connectionParameters"
    val url = "url"
    val charset = "charset"
    val enabled = "enabled"
    val `type` = "type"
  }
  object Postgis {
    val dbtype = "dbtype"
    val dbtypeValue = "postgis"
    val host = "host"
    val port = "port"
    val database = "database"
    val schema = "schema"
    val user = "user"
    val pass = "passwd"
    val timeout = "Connection timeout"
    val validateConnections = "validate connections"
    val maxConnections = "max connections"
    val minConnections = "min connections"
    val looseBBox = "Loose bbox"
    val fetchSize = "fetch size"
    val exposePrimaryKeys = "Expose primary keys"
    val maxPreparedStatements = "Max open prepared statements"
    val preparedStatements = "preparedStatements"
    val estimateExtents = "Estimated extends"
  }
}