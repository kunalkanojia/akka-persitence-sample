name := """akka-persistence-spike"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.11"

resolvers += "Eventuate Releases" at "https://dl.bintray.com/rbmhtechnology/maven"
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

javaOptions in Test += "-Dconfig.resource=test.conf"
//Heroku
herokuAppName in Compile := "play-akka-persistence"
herokuJdkVersion in Compile := "1.8"


val akkaVersion = "2.5.8"

libraryDependencies ++= Seq(
  ws,

  //Akka Persistence
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.80-RC2",
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % "0.80-RC2" % Test,

//  "org.iq80.leveldb" % "leveldb" % "0.7",
//  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

  //JSON
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "org.json4s" %% "json4s-ext" % "3.3.0",

  //UI
  "org.webjars" %% "webjars-play" % "2.5.0-4",
  filters,
  "org.webjars" % "bootstrap" % "3.3.7-1" exclude("org.webjars", "jquery"),
  "org.webjars" % "jquery" % "3.2.1",
  "com.adrianhurt" %% "play-bootstrap" % "1.2-P25-B3",

  //TEST
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,

  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
)

routesGenerator := InjectedRoutesGenerator
