organization := "com.distribuit"

name := "datos"

version := "0.1"

scalaVersion := "2.11.0"


libraryDependencies ++= Seq(
  "com.datos" % "vfs" % "1.0",
  "com.typesafe" % "config" % "1.3.0",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "com.typesafe.akka" %% "akka-cluster" % "2.4.14",
  "com.typesafe.akka" %% "akka-cluster-metrics" % "2.4.14",
  "com.typesafe.akka" %% "akka-remote" % "2.4.14",
  "com.typesafe.akka" %% "akka-stream" % "2.4.14",
  "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.14",
  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-core" % "10.0.0",
  "com.typesafe.play" % "play-json_2.11" % "2.5.4",
  "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "org.slf4j" % "slf4j-log4j12" % "1.7.2",
  "org.slf4j" % "slf4j-api" % "1.7.2",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.2",
  "org.apache.httpcomponents" % "httpclient" % "4.5.2",
  "org.apache.commons" % "commons-email" % "1.2",
  "org.apache.hadoop" % "hadoop-client" % "2.6.0",
  "org.apache.commons" % "commons-compress" % "1.12",
  "com.github.scala-incubator.io" % "scala-io-file_2.11" % "0.4.3",
  "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.1",
  "com.softwaremill" %% "akka-http-session" % "0.1.4-2.0-M1"
)

resolvers += Resolver.mavenLocal
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
assemblyJarName in assembly := s"datos-${version.value}-assembly.jar"

assemblyMergeStrategy in assembly := {

  case x if x.startsWith("org/apache/hadoop/yarn") => MergeStrategy.first
  case x if x.startsWith("org/apache/spark/unused") => MergeStrategy.first
  case x if x.startsWith("org/apache/commons") => MergeStrategy.first
  case x if x.startsWith("com/google/common/base") => MergeStrategy.first
  case x if x.startsWith("org/joda/time") => MergeStrategy.first
  case x if x.startsWith("javax") => MergeStrategy.first
  case x if x.startsWith("org/apache") => MergeStrategy.first
  case x if x.startsWith("org/mortbay") => MergeStrategy.first
  case x if x.startsWith("org/slf4j/impl") => MergeStrategy.first
  case x => (assemblyMergeStrategy in assembly).value(x)
}

unmanagedResources in Compile += file(".") / "conf" / "log4j.properties"

logBuffered in Test := false

scapegoatVersion := "1.3.0"
