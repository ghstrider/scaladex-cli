package scaladexcli

class ScaladexApiSuite extends munit.FunSuite:

  // These tests hit the real Scaladex API — they verify end-to-end
  // integration and serve as living documentation of the API contract.
  // They require network access and curl to be installed.

  override val munitTimeout = scala.concurrent.duration.Duration(30, "s")

  test("search returns results for known library") {
    val results = ScaladexApi.search("cats-core")
    assert(results.nonEmpty, "expected at least one result for 'cats-core'")
    assert(
      results.exists(_.organization == "typelevel"),
      "expected typelevel in search results"
    )
  }

  test("search returns empty list for nonsense query") {
    val results = ScaladexApi.search("zzznonexistentlibrary999")
    assertEquals(results, List.empty[SearchResult])
  }

  test("project returns details for typelevel/cats") {
    val details = ScaladexApi.project("typelevel", "cats")
    assertEquals(details.groupId, "org.typelevel")
    assert(details.artifacts.contains("cats-core"), "expected cats-core artifact")
    assert(details.versions.nonEmpty, "expected at least one version")
    assert(details.version.nonEmpty, "expected a latest version string")
  }

  test("project returns artifacts list for http4s/http4s") {
    val details = ScaladexApi.project("http4s", "http4s")
    assert(details.artifacts.contains("http4s-core"), "expected http4s-core artifact")
    assert(details.artifacts.contains("http4s-circe"), "expected http4s-circe artifact")
  }

  test("search respects scalaVersion parameter") {
    val results3 = ScaladexApi.search("zio", scalaVersion = "3")
    val results2 = ScaladexApi.search("zio", scalaVersion = "2.13")
    // Both should return results for zio
    assert(results3.nonEmpty, "expected results for Scala 3")
    assert(results2.nonEmpty, "expected results for Scala 2.13")
  }
