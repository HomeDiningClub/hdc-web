import sbt._
import Keys._
import play.Project._

name := "hdc-web"

version := "1.0"

resolvers ++= Seq(
  Resolver.file("Local Ivy Repository", file(baseDirectory.value + "/local_ivy/repository/"))(Resolver.ivyStylePatterns),
  "neo4j-public-repository" at "http://m2.neo4j.org/content/groups/public",
  Resolver.sonatypeRepo("releases")
)

libraryDependencies ++= Seq(
  javaCore,
  "com.wingnest.play2" %% "play21-frames-neo4j-plugin" % "1.2",
  "com.typesafe" %% "play-plugins-util" % "2.2.0",
  "com.typesafe" %% "play-plugins-mailer" % "2.2.0",
  "ws.securesocial" %% "securesocial" % "2.1.3"
)

playScalaSettings


//play.Keys.lessEntryPoints <<= baseDirectory { base =>
//  (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") +++
//    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "responsive.less") +++
//    (base / "app" / "assets" / "stylesheets" * "*.less")
//}