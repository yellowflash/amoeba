name := "amoeba"

version := "0.1"

scalaOrganization := "org.typelevel"

scalaVersion := "2.12.4-bin-typelevel-4"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)