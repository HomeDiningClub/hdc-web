import sbt._
import Keys._
import play.Project._

name := "hdc-web"

version := "1.0"

//mainClass := Some("controllers.Application.index")

resolvers ++= Seq(
  //Resolver.file("Local Ivy Repository", file(baseDirectory.value + "/local_ivy/repository/"))(Resolver.ivyStylePatterns),
 "Spring releases" at "http://repo.springsource.org/release",
 "Spring Data snapshot" at "http://repo.springsource.org/snapshot/",
 "Neo4j" at "http://m2.neo4j.org/content/repositories/releases/",
  Resolver.sonatypeRepo("releases")
)

libraryDependencies ++= Seq(
  javaCore,
  "javax.inject" % "javax.inject" % "1",
  "asm" % "asm" % "3.3.1",
  "org.springframework" % "spring-context" % "4.0.3.RELEASE",
  "org.springframework.data" % "spring-data-neo4j" % "3.0.1.RELEASE",
  "org.springframework.data" % "spring-data-neo4j-rest" % "3.0.1.RELEASE" excludeAll(ExclusionRule(organization = "org.neo4j", name="neo4j")),
  "org.neo4j" % "neo4j" % "2.0.1" excludeAll(ExclusionRule(organization = "org.neo4j", name="neo4j-kernel")),
  "org.neo4j" % "neo4j-kernel" % "2.0.1" % "test" classifier "tests" classifier "",
  "org.neo4j" % "neo4j-rest-graphdb" % "2.0.1",
  "com.typesafe" %% "play-plugins-util" % "2.2.0",
  "com.typesafe" %% "play-plugins-mailer" % "2.2.0",
  "ws.securesocial" %% "securesocial" % "2.1.3"
  //"com.wingnest.play2" %% "play21-frames-neo4j-plugin" % "1.2",
)

playScalaSettings


//play.Keys.lessEntryPoints <<= baseDirectory { base =>
//  (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") +++
//    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "responsive.less") +++
//    (base / "app" / "assets" / "stylesheets" * "*.less")
//}