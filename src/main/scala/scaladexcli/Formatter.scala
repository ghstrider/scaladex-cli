package scaladexcli

enum BuildTool:
  case SBT, Mill, ScalaCLI, Ammonite

object BuildTool:
  def fromString(s: String): Option[BuildTool] =
    s.toLowerCase match
      case "sbt"              => Some(BuildTool.SBT)
      case "mill"             => Some(BuildTool.Mill)
      case "scalacli" | "cli" => Some(BuildTool.ScalaCLI)
      case "amm" | "ammonite" => Some(BuildTool.Ammonite)
      case _                  => None

  val all: String = BuildTool.values.map(_.toString.toLowerCase).mkString(", ")

object Formatter:

  def detectBuildTool(filePath: String): BuildTool =
    if filePath.endsWith(".sbt") then BuildTool.SBT
    else if filePath.endsWith(".sc") then BuildTool.Ammonite
    else BuildTool.ScalaCLI

  def formatDep(
      buildTool: BuildTool,
      groupId: String,
      artifact: String,
      version: String
  ): String =
    buildTool match
      case BuildTool.SBT =>
        s""""$groupId" %% "$artifact" % "$version""""
      case BuildTool.Mill =>
        s"""ivy"$groupId::$artifact:$version""""
      case BuildTool.ScalaCLI =>
        s"""//> using dep "$groupId::$artifact:$version""""
      case BuildTool.Ammonite =>
        s"""import $$ivy.`$groupId::$artifact:$version`"""

  def insertDepIntoFile(filePath: String, depLine: String): Either[String, String] =
    val file = new java.io.File(filePath)
    if !file.exists() then return Left(s"File not found: $filePath")
    if !file.canRead() then return Left(s"Cannot read file: $filePath")
    if !file.canWrite() then return Left(s"Cannot write to file: $filePath")
    try
      val source = scala.io.Source.fromFile(filePath)
      val lines =
        try source.getLines().toVector
        finally source.close()
      val result = insertDepIntoLines(lines, depLine)
      val writer = new java.io.PrintWriter(filePath)
      try writer.write(result)
      finally writer.close()
      Right(result)
    catch
      case e: Exception => Left(s"Failed to update file: ${e.getMessage}")

  def insertDepIntoLines(lines: Vector[String], depLine: String): String =
    if lines.isEmpty then depLine + "\n"
    else
      val lastUsingIdx = lines.lastIndexWhere(_.startsWith("//> using"))
      if lastUsingIdx >= 0 then
        val (before, after) = lines.splitAt(lastUsingIdx + 1)
        (before :+ depLine :++ after).mkString("\n") + "\n"
      else
        (depLine +: lines).mkString("\n") + "\n"
