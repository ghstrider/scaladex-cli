package scaladexcli

import upickle.default.*
import scala.sys.process.*

class ApiException(message: String) extends Exception(message)

object ScaladexApi:

  private val baseUrl = "https://index.scala-lang.org/api"

  private def httpGet(url: String): String =
    try Seq("curl", "-sfL", "--connect-timeout", "10", "-H", "Accept: application/json", url).!!.trim
    catch
      case e: RuntimeException =>
        throw new ApiException(
          s"Network request failed. Check your internet connection.\n  url: $url"
        )

  private def parseJson[T: Reader](json: String, context: String): T =
    try read[T](json)
    catch
      case e: Exception =>
        throw new ApiException(s"Failed to parse $context response: ${e.getMessage}")

  def search(
      query: String,
      target: String = "JVM",
      scalaVersion: String = "3"
  ): List[SearchResult] =
    val encoded = java.net.URLEncoder.encode(query, "UTF-8")
    val url = s"$baseUrl/search?q=$encoded&target=$target&scalaVersion=$scalaVersion"
    val json = httpGet(url)
    parseJson[List[SearchResult]](json, "search")

  def project(org: String, repo: String): ProjectDetails =
    val url = s"$baseUrl/project?organization=$org&repository=$repo"
    val json = httpGet(url)
    parseJson[ProjectDetails](json, s"project $org/$repo")

  def releases(org: String, repo: String, count: Int = 5): List[GitHubRelease] =
    val url = s"https://api.github.com/repos/$org/$repo/releases?per_page=$count"
    try
      val json = httpGet(url)
      parseJson[List[GitHubRelease]](json, "releases")
    catch
      case _: Exception => List.empty
