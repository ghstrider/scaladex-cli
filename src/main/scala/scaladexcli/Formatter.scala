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
