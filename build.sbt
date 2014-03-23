import play.Project._

name := "hdc-web"

version := "1.0"

resolvers ++= Seq(
  //Resolver.url("Local Ivy Repository 1", url("file:///" + baseDirectory.value + "/local_ivy/repository/"))(Resolver.ivyStylePatterns),
  Resolver.file("Local Ivy Repository", file(baseDirectory.value + "/local_ivy/repository/"))(Resolver.ivyStylePatterns),
  //"Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "neo4j-public-repository" at "http://m2.neo4j.org/content/groups/public"
)

libraryDependencies ++= Seq(
  "com.wingnest.play2" %% "play21-frames-neo4j-plugin" % "1.2"
)

playScalaSettings