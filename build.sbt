name := "declarative-geoserver-config"

version := "0.1"

libraryDependencies ++= Seq(
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.2.0",
  "org.apache.httpcomponents" % "httpclient" % "4.1.2",
  "org.apache.httpcomponents" % "httpmime" % "4.1.2",
  "net.liftweb" %% "lift-json" % "2.4-M4",
  "com.github.scopt" %% "scopt" % "1.1.2",
  "junit" % "junit" % "4.8" % "test",
  "org.specs2" %% "specs2" % "1.6.1" % "test",
  "org.specs2" %% "specs2-scalaz-core" % "6.0.1" % "test")
