package scaladexcli

import upickle.default.*

class ModelsSuite extends munit.FunSuite:

  test("SearchResult parses from JSON") {
    val json = """{"organization":"typelevel","repository":"cats","artifacts":["cats-core","cats-free"]}"""
    val result = read[SearchResult](json)
    assertEquals(result.organization, "typelevel")
    assertEquals(result.repository, "cats")
    assertEquals(result.artifacts, List("cats-core", "cats-free"))
  }

  test("SearchResult parses empty artifacts") {
    val json = """{"organization":"foo","repository":"bar","artifacts":[]}"""
    val result = read[SearchResult](json)
    assertEquals(result.artifacts, List.empty[String])
  }

  test("SearchResult list parses from JSON array") {
    val json = """[{"organization":"a","repository":"b","artifacts":["x"]},{"organization":"c","repository":"d","artifacts":["y","z"]}]"""
    val results = read[List[SearchResult]](json)
    assertEquals(results.length, 2)
    assertEquals(results(0).organization, "a")
    assertEquals(results(1).artifacts, List("y", "z"))
  }

  test("ProjectDetails parses from JSON") {
    val json = """{"artifacts":["cats-core","cats-free"],"versions":["2.10.0","2.9.0"],"groupId":"org.typelevel","version":"2.10.0"}"""
    val result = read[ProjectDetails](json)
    assertEquals(result.groupId, "org.typelevel")
    assertEquals(result.version, "2.10.0")
    assertEquals(result.artifacts, List("cats-core", "cats-free"))
    assertEquals(result.versions, List("2.10.0", "2.9.0"))
  }

  test("SearchResult roundtrips through JSON") {
    val original = SearchResult("org", "repo", List("a", "b"))
    val json = write(original)
    val parsed = read[SearchResult](json)
    assertEquals(parsed, original)
  }

  test("ProjectDetails roundtrips through JSON") {
    val original = ProjectDetails(List("core"), List("1.0.0", "0.9.0"), "com.example", "1.0.0")
    val json = write(original)
    val parsed = read[ProjectDetails](json)
    assertEquals(parsed, original)
  }

  test("GitHubRelease parses from JSON") {
    val json = """{"tag_name":"v2.13.0","name":"Cats 2.13.0","published_at":"2025-01-20T12:00:00Z","body":"## What's Changed\n* Fix bug"}"""
    val result = read[GitHubRelease](json)
    assertEquals(result.tag_name, "v2.13.0")
    assertEquals(result.name, "Cats 2.13.0")
    assertEquals(result.published_at, "2025-01-20T12:00:00Z")
    assert(result.body.contains("What's Changed"))
  }

  test("GitHubRelease parses empty body") {
    val json = """{"tag_name":"v1.0.0","name":"","published_at":"2024-01-01T00:00:00Z","body":""}"""
    val result = read[GitHubRelease](json)
    assertEquals(result.tag_name, "v1.0.0")
    assertEquals(result.name, "")
    assertEquals(result.body, "")
  }

  test("GitHubRelease roundtrips through JSON") {
    val original = GitHubRelease("v1.0.0", "Release 1.0", "2024-06-01T10:00:00Z", "Initial release")
    val json = write(original)
    val parsed = read[GitHubRelease](json)
    assertEquals(parsed, original)
  }

  test("GitHubRelease list parses from JSON array") {
    val json = """[{"tag_name":"v2.0","name":"Two","published_at":"2025-01-01T00:00:00Z","body":"notes"},{"tag_name":"v1.0","name":"One","published_at":"2024-01-01T00:00:00Z","body":"old"}]"""
    val results = read[List[GitHubRelease]](json)
    assertEquals(results.length, 2)
    assertEquals(results(0).tag_name, "v2.0")
    assertEquals(results(1).tag_name, "v1.0")
  }
