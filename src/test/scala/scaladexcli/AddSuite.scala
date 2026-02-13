package scaladexcli

class AddSuite extends munit.FunSuite:

  // -- detectBuildTool tests --

  test("detects SBT from .sbt extension") {
    assertEquals(Formatter.detectBuildTool("build.sbt"), BuildTool.SBT)
  }

  test("detects Ammonite from .sc extension") {
    assertEquals(Formatter.detectBuildTool("script.sc"), BuildTool.Ammonite)
  }

  test("detects ScalaCLI from .scala extension") {
    assertEquals(Formatter.detectBuildTool("Main.scala"), BuildTool.ScalaCLI)
  }

  test("defaults to ScalaCLI for unknown extension") {
    assertEquals(Formatter.detectBuildTool("README.md"), BuildTool.ScalaCLI)
  }

  test("detects ScalaCLI for full path") {
    assertEquals(Formatter.detectBuildTool("/tmp/project/app.scala"), BuildTool.ScalaCLI)
  }

  // -- insertDepIntoLines tests --

  val depLine = """//> using dep "org.typelevel::cats-core:2.10.0""""

  test("inserts dep into empty file") {
    val result = Formatter.insertDepIntoLines(Vector.empty, depLine)
    assertEquals(result, depLine + "\n")
  }

  test("prepends dep when no using directives exist") {
    val lines = Vector(
      "object Main:",
      "  def main(args: Array[String]): Unit = ()"
    )
    val result = Formatter.insertDepIntoLines(lines, depLine)
    val expected = Vector(depLine, "object Main:", "  def main(args: Array[String]): Unit = ()").mkString("\n") + "\n"
    assertEquals(result, expected)
  }

  test("inserts after last existing using directive") {
    val lines = Vector(
      """//> using scala "3.3.4"""",
      """//> using dep "com.lihaoyi::os-lib:0.9.1"""",
      "",
      "object Main:",
      "  def main(args: Array[String]): Unit = ()"
    )
    val result = Formatter.insertDepIntoLines(lines, depLine)
    val expected = Vector(
      """//> using scala "3.3.4"""",
      """//> using dep "com.lihaoyi::os-lib:0.9.1"""",
      depLine,
      "",
      "object Main:",
      "  def main(args: Array[String]): Unit = ()"
    ).mkString("\n") + "\n"
    assertEquals(result, expected)
  }

  test("inserts after single using directive") {
    val lines = Vector(
      """//> using scala "3.3.4"""",
      "",
      "println(42)"
    )
    val result = Formatter.insertDepIntoLines(lines, depLine)
    val expected = Vector(
      """//> using scala "3.3.4"""",
      depLine,
      "",
      "println(42)"
    ).mkString("\n") + "\n"
    assertEquals(result, expected)
  }

  // -- insertDepIntoFile error handling --

  test("insertDepIntoFile returns Left for non-existent file") {
    val result = Formatter.insertDepIntoFile("/tmp/no_such_file_12345.scala", depLine)
    assert(result.isLeft)
    assert(result.left.get.contains("File not found"))
  }

  test("insertDepIntoFile succeeds on valid file") {
    val tmp = java.io.File.createTempFile("scaladex-test-", ".scala")
    tmp.deleteOnExit()
    val writer = new java.io.PrintWriter(tmp)
    writer.write("//> using scala \"3\"\n\nprintln(42)\n")
    writer.close()

    val result = Formatter.insertDepIntoFile(tmp.getAbsolutePath, depLine)
    assert(result.isRight)
    assert(result.right.get.contains(depLine))
  }
