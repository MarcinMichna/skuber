
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

val akkaVersion = "2.6.15"

val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.15.4"
val specs2 = "org.specs2" %% "specs2-core" % "4.11.0"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.9"
val mockito = "org.mockito" % "mockito-core" % "3.11.0"
val akkaStreamTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion

val snakeYaml =  "org.yaml" % "snakeyaml" % "1.29"
val commonsIO = "commons-io" % "commons-io" % "2.9.0"
val commonsCodec = "commons-codec" % "commons-codec" % "1.15"
val bouncyCastleFips = "org.bouncycastle" % "bc-fips" % "1.0.2"
val bouncyCastlePkixFips = "org.bouncycastle" % "bcpkix-fips" % "1.0.2"

// the client API request/response handing uses Akka Http
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.2.4"
val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion

// Skuber uses akka logging, so the examples config uses the akka slf4j logger with logback backend
val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
val logback = "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime

// the Json formatters are based on Play Json
val playJson = "com.typesafe.play" %% "play-json" % "2.9.2"

// Need Java 8 or later as the java.time package is used to represent K8S timestamps
scalacOptions += "-target:jvm-1.8"

scalacOptions in Test ++= Seq("-Yrangepos")

ThisBuild / version := "2.6.3"

sonatypeProfileName := "io.skuber"

publishMavenStyle in ThisBuild := true

licenses in ThisBuild := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage in ThisBuild := Some(url("https://github.com/doriordan"))

scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/doriordan/skuber"),
    "scm:git@github.com:doriordan/skuber.git"
  )
)

developers in ThisBuild := List(Developer(id="doriordan", name="David ORiordan", email="doriordan@gmail.com", url=url("https://github.com/doriordan")))

lazy val commonSettings = Seq(
  organization := "io.skuber",
  crossScalaVersions := Seq("2.12.13", "2.13.6"),
  scalaVersion := "2.13.6",
  publishTo :=  sonatypePublishToBundle.value,
  pomIncludeRepository := { _ => false },
  Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat
)

lazy val skuberSettings = Seq(
  name := "skuber",
  libraryDependencies ++= Seq(
    akkaHttp, akkaStream, playJson, snakeYaml, commonsIO, commonsCodec, bouncyCastleFips, bouncyCastlePkixFips,
    scalaCheck % Test, specs2 % Test, mockito % Test, akkaStreamTestKit % Test,
    scalaTest % Test
  ).map(_.exclude("commons-logging", "commons-logging"))
)

lazy val examplesSettings = Seq(
  name := "skuber-examples",
  libraryDependencies ++= Seq(akka, akkaSlf4j, logback)
)

// by default run the guestbook example when executing a fat examples JAR
lazy val examplesAssemblySettings = Seq(
  mainClass in assembly := Some("skuber.examples.guestbook.Guestbook")
)

publishArtifact in root := false

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(skuber, examples)

lazy val skuber= (project in file("client"))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    skuberSettings,
    Defaults.itSettings,
    libraryDependencies += scalaTest % "it"
  )

lazy val examples = (project in file("examples"))
  .settings(commonSettings: _*)
  .settings(examplesSettings: _*)
  .settings(examplesAssemblySettings: _*)
  .dependsOn(skuber)
