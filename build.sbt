import play.Project._

name := "hdc-web"

version := "1.0"

//mainClass := Some("controllers.Application.index")

resolvers ++= Seq(
  //Resolver.file("Local Ivy Repository", file(baseDirectory.value + "/local_ivy/repository/"))(Resolver.ivyStylePatterns),
 "Spring releases" at "http://repo.springsource.org/release",
 "Spring Data snapshot" at "http://repo.springsource.org/snapshot/",
 "Neo4j" at "http://m2.neo4j.org/content/repositories/releases/",
 "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local",
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
  "com.sun.jersey" % "jersey-core" % "1.9",
  "com.typesafe" %% "play-plugins-util" % "2.2.0",
  "com.typesafe" %% "play-plugins-mailer" % "2.2.0",
  "ws.securesocial" %% "securesocial" % "2.1.3",
  "nl.rhinofly" %% "play-s3" % "3.3.4"
)

playScalaSettings