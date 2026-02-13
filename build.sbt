enablePlugins(ScalaNativePlugin)

name := "scaladex-cli"
version := "0.1.0"
organization := "dev.ankush"

scalaVersion := "3.3.4"

libraryDependencies ++= Seq(
  "com.monovore" %%% "decline" % "2.6.0",
  "com.lihaoyi" %%% "upickle" % "4.4.2",
  "org.scalameta" %%% "munit" % "1.2.2" % Test
)

testFrameworks += new TestFramework("munit.Framework")

nativeConfig ~= {
  _.withLTO(scala.scalanative.build.LTO.none)
    .withMode(scala.scalanative.build.Mode.releaseFast)
    .withGC(scala.scalanative.build.GC.commix)
}
