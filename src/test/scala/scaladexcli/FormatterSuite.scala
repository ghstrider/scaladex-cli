package scaladexcli

class FormatterSuite extends munit.FunSuite:

  val groupId = "org.typelevel"
  val artifact = "cats-core"
  val version = "2.10.0"

  test("formats SBT dependency") {
    val result = Formatter.formatDep(BuildTool.SBT, groupId, artifact, version)
    assertEquals(result, """"org.typelevel" %% "cats-core" % "2.10.0"""")
  }

  test("formats Mill dependency") {
    val result = Formatter.formatDep(BuildTool.Mill, groupId, artifact, version)
    assertEquals(result, """ivy"org.typelevel::cats-core:2.10.0"""")
  }

  test("formats ScalaCLI dependency") {
    val result = Formatter.formatDep(BuildTool.ScalaCLI, groupId, artifact, version)
    assertEquals(result, """//> using dep "org.typelevel::cats-core:2.10.0"""")
  }

  test("formats Ammonite dependency") {
    val result = Formatter.formatDep(BuildTool.Ammonite, groupId, artifact, version)
    assertEquals(result, """import $ivy.`org.typelevel::cats-core:2.10.0`""")
  }

  test("handles artifact with dots in groupId") {
    val result =
      Formatter.formatDep(BuildTool.ScalaCLI, "com.softwaremill.sttp", "core", "3.9.0")
    assertEquals(result, """//> using dep "com.softwaremill.sttp::core:3.9.0"""")
  }

  test("handles pre-release versions") {
    val result = Formatter.formatDep(BuildTool.SBT, groupId, artifact, "2.10.0-RC1")
    assertEquals(result, """"org.typelevel" %% "cats-core" % "2.10.0-RC1"""")
  }
