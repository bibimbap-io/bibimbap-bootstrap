organization := "io.bibimbap"

name := "bibimbap-bootstrap"

version := "0.1.0"

scalaVersion := "2.10.0-M6"

scalacOptions += "-deprecation"

scalacOptions += "-feature"

scalacOptions += "-unchecked"

mainClass in (Compile, run) := Some("io.bibimbap.bootstrap.Main")
