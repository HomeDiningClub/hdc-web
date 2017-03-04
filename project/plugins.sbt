logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.12")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

//addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.1")

//addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.1.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.1.1")

//addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "1.0.0")

// Scalr & Rez
addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")