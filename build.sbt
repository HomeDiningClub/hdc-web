import play.Project._

name := "hdc-web"

version := "1.0"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "neo4j-public-repository" at "http://m2.neo4j.org/content/groups/public"
)

libraryDependencies ++= Seq(
  "com.wingnest.play2" %% "play21-frames-neo4j-plugin" % "1.2"
)

playScalaSettings