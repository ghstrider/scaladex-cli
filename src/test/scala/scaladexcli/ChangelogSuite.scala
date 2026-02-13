package scaladexcli

class ChangelogSuite extends munit.FunSuite:

  test("fetches releases for typelevel/cats") {
    val releases = ScaladexApi.releases("typelevel", "cats", 3)
    assert(releases.nonEmpty, "Expected at least one release")
    assert(releases.size <= 3)
    releases.foreach { r =>
      assert(r.tag_name.nonEmpty, "tag_name should not be empty")
      assert(r.published_at.nonEmpty, "published_at should not be empty")
    }
  }

  test("returns empty list for nonexistent repo") {
    val releases = ScaladexApi.releases("nonexistent-org-xyz", "no-such-repo", 5)
    assertEquals(releases, List.empty)
  }

  test("respects count parameter") {
    val releases = ScaladexApi.releases("typelevel", "cats", 2)
    assert(releases.size <= 2)
  }
