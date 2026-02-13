package scaladexcli

class TemplateSuite extends munit.FunSuite:

  test("all template names are unique") {
    val names = Templates.all.map(_.name)
    assertEquals(names.distinct.size, names.size)
  }

  test("get returns matching template") {
    val tpl = Templates.get("cats-effect")
    assert(tpl.isDefined)
    assertEquals(tpl.get.name, "cats-effect")
  }

  test("get is case-insensitive") {
    val tpl = Templates.get("CATS-EFFECT")
    assert(tpl.isDefined)
    assertEquals(tpl.get.name, "cats-effect")
  }

  test("get returns None for unknown template") {
    assertEquals(Templates.get("nonexistent"), None)
  }

  test("all templates have non-empty content") {
    Templates.all.foreach { t =>
      assert(t.content.nonEmpty, s"Template '${t.name}' has empty content")
    }
  }

  test("all templates have non-empty filename") {
    Templates.all.foreach { t =>
      assert(t.filename.nonEmpty, s"Template '${t.name}' has empty filename")
    }
  }

  test("scala-cli templates contain using directives") {
    val scalaTemplates = Templates.all.filter(_.filename.endsWith(".scala"))
    scalaTemplates.foreach { t =>
      assert(
        t.content.contains("//> using"),
        s"Template '${t.name}' missing //> using directive"
      )
    }
  }
