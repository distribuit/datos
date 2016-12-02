organization := "com.distribuit"

name := "datos"

version := "0.1"

scalaVersion := "2.11.0"


libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.1.0",
  "org.apache.hadoop" % "hadoop-client" % "2.6.0",
  "org.apache.commons" % "commons-compress" % "1.12",
  "org.apache.commons" % "commons-vfs2" % "2.1",
  "com.github.scala-incubator.io" % "scala-io-file_2.11" % "0.4.3",
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "com.typesafe.akka" %% "akka-cluster" % "2.4.14",
  "com.typesafe.akka" %% "akka-cluster-metrics" % "2.4.14",
  "com.typesafe.akka" %% "akka-remote" % "2.4.14",
  "com.typesafe.akka" %% "akka-stream" % "2.4.14",
  "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.14",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-core" % "10.0.0",
  "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.1",
  "com.typesafe.play" % "play-json_2.11" % "2.5.4"
)

resolvers += Resolver.mavenLocal

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
