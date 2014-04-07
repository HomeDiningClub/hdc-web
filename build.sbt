import sbt._
import Keys._
import play.Project._

name := "hdc-web"

version := "1.0"

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
  "org.springframework" % "spring-context" % "3.2.4.RELEASE",
  "org.springframework.data" % "spring-data-neo4j" % "2.3.1.RELEASE",
  "org.springframework.data" % "spring-data-neo4j-rest" % "2.3.1.RELEASE" excludeAll(ExclusionRule(organization = "org.neo4j", name="neo4j")),
  "org.neo4j" % "neo4j" % "1.9.3" excludeAll(ExclusionRule(organization = "org.neo4j", name="neo4j-kernel")),
  "org.neo4j" % "neo4j-kernel" % "1.9.3" % "test" classifier "tests" classifier "",
  "org.neo4j" % "neo4j-rest-graphdb" % "1.9.RC2",
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