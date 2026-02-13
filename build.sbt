enablePlugins(ScalaNativePlugin)

name := "scaladex-cli"
version := "0.1.0"
organization := "dev.ankush"

scalaVersion := "3.3.4"

libraryDependencies ++= Seq(
  "com.monovore" %%% "decline" % "2.4.1",
  "com.lihaoyi" %%% "upickle" % "4.0.2"
)

nativeConfig ~= {
  _.withLTO(scala.scalanative.build.LTO.thin)
    .withMode(scala.scalanative.build.Mode.releaseFast)
    .withGC(scala.scalanative.build.GC.commix)
}
