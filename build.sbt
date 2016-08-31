
name := """hdc-web"""

version := "2.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.8"

logLevel := Level.Error // Change this if you need to debug library dependencies problems (Info / Warn)

//ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

resolvers ++= Seq(
  //Resolver.file("Local Ivy Repository", file(baseDirectory.value + "/local_ivy/repository/"))(Resolver.ivyStylePatterns),
  "Spring releases" at "http://repo.springsource.org/release",
  "Spring milestones" at "http://repo.spring.io/milestone",
  "Spring Data snapshot" at "http://repo.springsource.org/snapshot/",
  "Spring Snapshots" at "http://maven.springframework.org/snapshot", // spring-guice
  "Neo4j" at "http://m2.neo4j.org/content/repositories/releases/",
  "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local",
  "tuxburner.github.io" at "http://tuxburner.github.io/repo", // Play neo4j plugin
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases", // play mailer
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.mavenLocal
)

libraryDependencies ++= Seq(
  cache,
  javaCore,
  specs2 % Test,
  "javax.inject" % "javax.inject" % "1" withSources(),
  "javax.validation" % "validation-api" % "1.1.0.Final" withSources(),
  "asm" % "asm" % "3.3.1",
  // Plugin for Neo4j
  //"com.github.tuxBurner" %% "play-neo4jplugin" % "1.5.0" withSources() excludeAll(
  //  ExclusionRule(organization = "org.springframework"),
  //  ExclusionRule(organization = "org.springframework.data"),
  //  ExclusionRule(organization = "org.neo4j"),
  //  ExclusionRule(organization = "com.typesafe.play")),
  // Spring data
  "com.sun.jersey" % "jersey-core" % "1.19" withSources(),
  "org.springframework" % "spring-context" % "4.1.9.RELEASE" withSources(),
  "org.springframework.data" % "spring-data-neo4j" % "3.4.2.RELEASE" excludeAll(ExclusionRule(organization = "org.neo4j", name="neo4j"), ExclusionRule(organization = "org.springframework", name = "spring-context")) withSources(),
  "org.springframework.data" % "spring-data-neo4j-rest" % "3.4.2.RELEASE" excludeAll(ExclusionRule(organization = "org.neo4j", name="neo4j"), ExclusionRule(organization = "org.springframework", name = "spring-context")) withSources(),
  // Spring guice
  "org.springframework.guice" % "spring-guice" % "1.0.0.BUILD-SNAPSHOT" withSources(),
  // Neo4j
  "org.neo4j" % "neo4j" % "2.3.1" withSources(),
  "org.neo4j" % "neo4j-backup" % "2.3.1" withSources(),
  //"org.springframework" % "spring-context" % "4.1.0.RELEASE",
  //"org.springframework.data" % "spring-data-neo4j" % "3.4.2.RELEASE",
  //"org.springframework.data" % "spring-data-neo4j-rest" % "3.4.2.RELEASE" excludeAll(ExclusionRule(organization = "org.neo4j", name="neo4j")),
  // Neo4J
  //"org.neo4j" % "neo4j" % "2.3.0-M02", //excludeAll(ExclusionRule(organization = "org.neo4j", name="neo4j-kernel")),
  //"org.neo4j" % "neo4j-kernel" % "2.1.4" % "test" classifier "tests" classifier "",
  //"org.neo4j" % "neo4j-backup" % "2.3.0-M02",
  //"ws.securesocial" %% "securesocial" % "3.0-M4" withSources(),
  // SecureSocial
  "ws.securesocial" %% "securesocial" % "master-SNAPSHOT" withSources(),
  "com.typesafe.play" %% "play-mailer" % "3.0.1" withSources(),
  "net.codingwell" %% "scala-guice" % "4.0.0" withSources(),
  // For imagesRezise components
  "commons-io" % "commons-io" % "2.4" withSources(),
  "commons-codec" % "commons-codec" % "1.9" withSources(),
  //"javax.enterprise" % "cdi-api" % "1.0-SP4",
  //"com.adrianhurt" %% "play-bootstrap3" % "0.4.4"
  "com.adrianhurt" %% "play-bootstrap" % "1.1-P24-B3" excludeAll(ExclusionRule(organization = "org.webjars", name="jquery"), ExclusionRule(organization = "org.webjars", name="bootstrap")) withSources()
  //"com.typesafe.play" %% "play-plugins-util" % "2.4.0",
  //"nl.rhinofly" %% "play-s3" % "3.3.4",
  //"se.digiplant" %% "play-scalr" % "1.1.2",
  //"se.digiplant" %% "play-res" % "1.1.1",
  //"com.sksamuel.scrimage" %% "scrimage-core" % "1.4.1",
  //"com.sksamuel.scrimage" %% "scrimage-filters" % "1.4.1" (for filter and effects)
  //"com.sksamuel.scrimage" %% "scrimage-canvas" % "1.4.1" (for image manipulation)
  //"org.julienrf" %% "play-jsmessages" % "1.6.2"
)

// Import more bindings for routes
// This makes support for unknown types in routing
play.sbt.routes.RoutesKeys.routesImport ++= Seq(
  "customUtils.Binders._"
)

// This is imports for all templates
TwirlKeys.templateImports ++= Seq(
  "play.twirl.api._",
  "models.viewmodels._",
  "models.formdata._"
)

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  //"-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

// Run Grunt tasks
//PlayKeys.playRunHooks += Grunt(baseDirectory.value)
//baseDirectory.map(base => GruntRunner(base / "app" / "assets"))

// Enabled Less rendering
includeFilter in (Assets, LessKeys.less) := "*.less"

LessKeys.compress in Assets := true

// Don't reload app when editing public static resources
watchSources := (watchSources.value
  --- baseDirectory.value / "app/assets" ** "*"
  --- baseDirectory.value / "public"     ** "*").get

// For debugging when getting weird route compiler errors
//sourcePositionMappers := Nil

// This is for adding extra features
//pipelineStages := Seq(rjs, digest, gzip)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator



