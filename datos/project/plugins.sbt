logLevel := Level.Warn
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"


//Sbt Plugins
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.1")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.0")
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.4")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")