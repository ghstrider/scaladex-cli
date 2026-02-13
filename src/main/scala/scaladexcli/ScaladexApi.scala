package scaladexcli

import upickle.default.*
import java.net.{URL, HttpURLConnection}
import java.io.{BufferedReader, InputStreamReader}

object ScaladexApi:

  private val baseUrl = "https://index.scala-lang.org/api"

  private def httpGet(url: String): String =
    val conn = URL(url).openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("GET")
    conn.setRequestProperty("Accept", "application/json")
    conn.setConnectTimeout(10000)
    conn.setReadTimeout(10000)
    try
      val reader = BufferedReader(InputStreamReader(conn.getInputStream))
      val sb = StringBuilder()
      var line = reader.readLine()
      while line != null do
        sb.append(line)
        line = reader.readLine()
      reader.close()
      sb.toString
    finally conn.disconnect()

  def search(
      query: String,
      target: String = "JVM",
      scalaVersion: String = "3"
  ): List[SearchResult] =
    val encoded = java.net.URLEncoder.encode(query, "UTF-8")
    val url =
      s"$baseUrl/search?q=$encoded&target=$target&scalaVersion=$scalaVersion"
    val json = httpGet(url)
    read[List[SearchResult]](json)

  def project(org: String, repo: String): ProjectDetails =
    val url = s"$baseUrl/project?organization=$org&repository=$repo"
    val json = httpGet(url)
    read[ProjectDetails](json)
