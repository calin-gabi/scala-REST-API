name := "rest-api"

version := "1.0.0"
scalaVersion := "2.12.1"

enablePlugins(JavaAppPackaging)

maintainer := "Gabriel Munteanu"

packageSummary := s"Akka 2.4.17 Server"

libraryDependencies ++= {
  val akkaV = "10.0.4"
  val scalaTestV = "3.0.1"
  val slickVersion = "3.2.0-M2"
  val circeV = "0.6.1"
  val slickJodaMapperVersion = "2.3.0"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaV,
    "ch.megard" %% "akka-http-cors" % "0.2.1",
    "de.heikoseeberger" %% "akka-http-circe" % "1.11.0",

    "com.typesafe.slick" %% "slick" % slickVersion,
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
    "org.flywaydb" % "flyway-core" % "3.2.1",

    "com.github.tototoshi" %% "slick-joda-mapper" % slickJodaMapperVersion,
    "joda-time" % "joda-time" % "2.7",
    "com.github.nscala-time" %% "nscala-time" % "2.16.0",
    "org.joda" % "joda-convert" % "1.7",

    "com.zaxxer" % "HikariCP" % "2.4.5",
    "org.slf4j" % "slf4j-nop" % "1.6.4",

    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-generic" % circeV,
    "io.circe" %% "circe-parser" % circeV,

    "com.github.t3hnar" %% "scala-bcrypt" % "3.0",

    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test",
    "ru.yandex.qatools.embed" % "postgresql-embedded" % "1.15" % "test",

    "com.google.api-client" % "google-api-client" % "1.22.0",
    "com.google.apis" % "google-api-services-oauth2" % "v2-rev129-1.22.0",

    "com.typesafe.akka" %% "akka-actor" % "2.4.17",
    // "com.typesafe.akka" %% "akka-cluster" % "2.4.17",
    "com.github.scopt" %% "scopt" % "3.6.0"
  )
}

Revolver.settings
enablePlugins(DockerPlugin)

//dockerExposedPorts := Seq(9000)
//dockerEntrypoint := Seq("bin/%s" format executableScriptName.value, "-Dconfig.resource=docker.conf")
