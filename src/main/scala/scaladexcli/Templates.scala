package scaladexcli

case class Template(name: String, description: String, filename: String, content: String)

object Templates:

  val all: List[Template] = List(
    Template(
      "hello",
      "Plain Scala hello world",
      "hello.scala",
      """|//> using scala "3"
         |
         |@main def hello(): Unit =
         |  println("Hello, World!")
         |""".stripMargin
    ),
    Template(
      "cats-effect",
      "Cats Effect IOApp",
      "app.scala",
      """|//> using scala "3"
         |//> using dep "org.typelevel::cats-effect:3.5.7"
         |
         |import cats.effect.*
         |
         |object Main extends IOApp.Simple:
         |  def run: IO[Unit] =
         |    IO.println("Hello, Cats Effect!")
         |""".stripMargin
    ),
    Template(
      "http4s",
      "Http4s server with Ember",
      "server.scala",
      """|//> using scala "3"
         |//> using dep "org.http4s::http4s-ember-server:0.23.30"
         |//> using dep "org.http4s::http4s-dsl:0.23.30"
         |
         |import cats.effect.*
         |import org.http4s.*
         |import org.http4s.dsl.io.*
         |import org.http4s.ember.server.EmberServerBuilder
         |import com.comcast.ip4s.*
         |
         |object Server extends IOApp.Simple:
         |  val routes = HttpRoutes.of[IO]:
         |    case GET -> Root => Ok("Hello, Http4s!")
         |    case GET -> Root / "greet" / name => Ok(s"Hello, $name!")
         |
         |  def run: IO[Unit] =
         |    EmberServerBuilder.default[IO]
         |      .withHost(host"0.0.0.0")
         |      .withPort(port"8080")
         |      .withHttpApp(routes.orNotFound)
         |      .build
         |      .useForever
         |""".stripMargin
    ),
    Template(
      "zio",
      "ZIO application",
      "app.scala",
      """|//> using scala "3"
         |//> using dep "dev.zio::zio:2.1.14"
         |
         |import zio.*
         |
         |object Main extends ZIOAppDefault:
         |  def run =
         |    Console.printLine("Hello, ZIO!")
         |""".stripMargin
    ),
    Template(
      "fs2",
      "FS2 streaming example",
      "stream.scala",
      """|//> using scala "3"
         |//> using dep "co.fs2::fs2-core:3.11.0"
         |//> using dep "org.typelevel::cats-effect:3.5.7"
         |
         |import cats.effect.*
         |import fs2.Stream
         |
         |object Main extends IOApp.Simple:
         |  def run: IO[Unit] =
         |    Stream
         |      .iterate(1)(_ + 1)
         |      .take(10)
         |      .evalMap(n => IO.println(s"Item $n"))
         |      .compile
         |      .drain
         |""".stripMargin
    ),
    Template(
      "scalatest",
      "ScalaTest spec",
      "test.scala",
      """|//> using scala "3"
         |//> using dep "org.scalatest::scalatest:3.2.19"
         |//> using test.dep "org.scalatestplus::scalacheck-1-18:3.2.19.0"
         |
         |import org.scalatest.flatspec.AnyFlatSpec
         |import org.scalatest.matchers.should.Matchers
         |
         |class ExampleSpec extends AnyFlatSpec with Matchers:
         |  "A string" should "have length" in {
         |    "hello".length shouldBe 5
         |  }
         |
         |  it should "concatenate" in {
         |    "hello" ++ " world" shouldBe "hello world"
         |  }
         |""".stripMargin
    ),
    Template(
      "munit",
      "MUnit test suite",
      "test.scala",
      """|//> using scala "3"
         |//> using dep "org.scalameta::munit:1.1.0"
         |
         |class ExampleSuite extends munit.FunSuite:
         |  test("addition") {
         |    assertEquals(1 + 1, 2)
         |  }
         |
         |  test("string length") {
         |    assertEquals("hello".length, 5)
         |  }
         |""".stripMargin
    ),
    Template(
      "os-lib",
      "OS-Lib file operations",
      "files.scala",
      """|//> using scala "3"
         |//> using dep "com.lihaoyi::os-lib:0.11.4"
         |
         |@main def files(): Unit =
         |  val cwd = os.pwd
         |  println(s"Current directory: $cwd")
         |
         |  val items = os.list(cwd)
         |  println(s"Found ${items.size} items:")
         |  items.foreach(p => println(s"  ${p.last}"))
         |""".stripMargin
    ),
    Template(
      "tapir",
      "Tapir HTTP API with documentation",
      "api.scala",
      """|//> using scala "3"
         |//> using dep "com.softwaremill.sttp.tapir::tapir-netty-server-cats:1.11.12"
         |//> using dep "com.softwaremill.sttp.tapir::tapir-swagger-ui-bundle:1.11.12"
         |
         |import cats.effect.*
         |import sttp.tapir.*
         |import sttp.tapir.server.netty.cats.NettyCatsServer
         |import sttp.tapir.swagger.bundle.SwaggerInterpreter
         |
         |object Main extends IOApp.Simple:
         |  val hello = endpoint.get
         |    .in("greet" / path[String]("name"))
         |    .out(stringBody)
         |    .serverLogicSuccess[IO](name => IO.pure(s"Hello, $name!"))
         |
         |  val docs = SwaggerInterpreter().fromEndpoints[IO](List(hello.endpoint), "My API", "1.0")
         |
         |  def run: IO[Unit] =
         |    NettyCatsServer.io()
         |      .use: server =>
         |        server
         |          .port(8080)
         |          .addEndpoint(hello)
         |          .addEndpoints(docs)
         |          .start()
         |          .flatMap(_ => IO.println("Server at http://localhost:8080/docs") *> IO.never)
         |""".stripMargin
    )
  )

  val names: String = all.map(_.name).mkString(", ")

  def get(name: String): Option[Template] =
    all.find(_.name.equalsIgnoreCase(name))
