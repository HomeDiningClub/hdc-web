import sbt._
import Keys._
import play.Project._

name := "hdc-web"

version := "1.0"

resolvers ++= Seq(
  Resolver.file("Local Ivy Repository", file(baseDirectory.value + "/local_ivy/repository/"))(Resolver.ivyStylePatterns),
  "neo4j-public-repository" at "http://m2.neo4j.org/content/groups/public"
)

libraryDependencies ++= Seq(
  javaCore,
  "com.wingnest.play2" %% "play21-frames-neo4j-plugin" % "1.2"
)

playScalaSettings

play.Keys.lessEntryPoints <<= baseDirectory { base =>
  (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") +++
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "responsive.less") +++
    (base / "app" / "assets" / "stylesheets" * "*.less")
}