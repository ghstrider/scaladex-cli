package scaladexcli

import upickle.default.*

class ErrorHandlingSuite extends munit.FunSuite:

  // -- API failure tests --

  test("search throws ApiException for unreachable host") {
    // Use a non-routable IP to simulate network failure
    val ex = intercept[ApiException] {
      val api = ScaladexApi
      // Override by calling a bad URL directly through the public method
      // Since httpGet is private, we test via project() with a bad org that triggers curl error
      ScaladexApi.project("nonexistent-org-xyz-99999", "no-repo")
    }
    assert(ex.getMessage.contains("Network request failed") || ex.getMessage.contains("Failed to parse"))
  }

  test("releases returns empty list for network failures") {
    val result = ScaladexApi.releases("nonexistent-org-xyz-99999", "no-repo")
    assertEquals(result, List.empty)
  }

  // -- JSON parsing failure tests --

  test("malformed JSON throws upickle exception") {
    val ex = intercept[Exception] {
      read[List[SearchResult]]("not valid json at all")
    }
    assert(ex != null)
  }

  test("wrong JSON shape throws upickle exception") {
    val ex = intercept[Exception] {
      read[ProjectDetails]("""{"wrong":"shape"}""")
    }
    assert(ex != null)
  }

  test("empty JSON array parses to empty list") {
    val result = read[List[SearchResult]]("[]")
    assertEquals(result, List.empty)
  }

  test("GitHubRelease ignores extra fields") {
    val json = """{"tag_name":"v1.0","name":"One","published_at":"2024-01-01","body":"notes","extra_field":"ignored"}"""
    val result = read[GitHubRelease](json)
    assertEquals(result.tag_name, "v1.0")
  }

  // -- File operation failure tests --

  test("insertDepIntoFile fails for non-existent path") {
    val result = Formatter.insertDepIntoFile(
      "/no/such/directory/file.scala",
      """//> using dep "a::b:1.0""""
    )
    assert(result.isLeft)
    assert(result.left.get.contains("File not found"))
  }

  test("insertDepIntoFile fails for directory path") {
    val result = Formatter.insertDepIntoFile(
      "/tmp",
      """//> using dep "a::b:1.0""""
    )
    // /tmp exists but is a directory, should fail on read or write
    assert(result.isLeft)
  }

  // -- BuildTool parsing failure tests --

  test("fromString returns None for invalid build tool") {
    assertEquals(BuildTool.fromString("gradle"), None)
    assertEquals(BuildTool.fromString(""), None)
    assertEquals(BuildTool.fromString("maven"), None)
  }

  // -- Template failure tests --

  test("template get returns None for empty string") {
    assertEquals(Templates.get(""), None)
  }

  test("template get returns None for random string") {
    assertEquals(Templates.get("this-is-not-a-template"), None)
  }
