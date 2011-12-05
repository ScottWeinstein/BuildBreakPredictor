name := "class BuildBreakPredictor"

version := "1.0.0.0-SNAPSHOT"

organization := "org.SW"

scalaVersion := "2.9.1"

resolvers ++= Seq(
	"scala-tools-releases" at "http://scala-tools.org/repo-releases/",
	"Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
)

libraryDependencies ++= Seq(
  "net.databinder"  %% "dispatch-http"  % "0.8.6",
  "net.liftweb"  %% "lift-json"  % "2.4-M5",
  "org.scala-tools.time" %% "time" % "0.5",
  "org.scalala" %% "scalala" % "1.0.0.RC2-SNAPSHOT",
  "org.scalaz" %% "scalaz-core" % "6.0.3",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.2.0",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.2.0",
  "junit" % "junit" % "4.10" % "test",
  "org.scala-tools.testing"  %% "specs"  % "1.6.9"  % "test",
  "org.scalatest"  %% "scalatest"  % "1.6.1"  % "test",
  "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test"
)


