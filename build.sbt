organization := "io.bibimbap"

name := "bibimbap-bootstrap"

version := "0.1.0"

mainClass in (Compile, run) := Some("io.bibimbap.bootstrap.Main")

// Note that this project is not supposed to use any Scala code. We keep
// the versions in sync just for the show.

scalaVersion := "2.10.0-RC1"

scalacOptions += "-deprecation"

scalacOptions += "-feature"

scalacOptions += "-unchecked"
