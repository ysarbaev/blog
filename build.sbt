name := "akka-http-blog"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.4.1"
  val akkaStreamV = "2.0.1"
  val scalikeV = "2.3.4"
  val h2V = "1.4.191"
  val scalaTestV = "2.2.6"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamV,

    "org.scalikejdbc" %% "scalikejdbc" % scalikeV,
    "org.scalikejdbc" %% "scalikejdbc-config"  % scalikeV,

    "com.h2database" % "h2" % h2V,

    "org.scalatest" % "scalatest_2.11" % scalaTestV % "test"
  )
}



