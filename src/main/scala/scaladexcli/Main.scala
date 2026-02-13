package scaladexcli

import cats.syntax.all.*
import com.monovore.decline.*

object Log:
  var verbose: Boolean = false
  def info(msg: String): Unit =
    if verbose then System.err.println(s"\u001b[2m> $msg\u001b[0m")

object Commands:
  def handleApiError(e: ApiException): Nothing =
    System.err.println(s"Error: ${e.getMessage}")
    sys.exit(1)

  def interactiveSearch(
      query: String,
      btOpt: Option[BuildTool],
      sv: String,
      tgt: String,
      fileOpt: Option[String]
  ): Unit =
    Log.info(s"Searching Scaladex for '$query' (scala=$sv, target=$tgt)...")
    val results =
      try ScaladexApi.search(query, tgt, sv)
      catch case e: ApiException => handleApiError(e)
    Log.info(s"Found ${results.size} result(s)")
    if results.isEmpty then
      System.err.println(s"No results found for '$query'")
      sys.exit(1)

    val chosen = Interactive.choose(
      "Select a project:",
      results,
      r => s"${r.organization}/${r.repository}"
    )

    val org = chosen.organization
    val repo = chosen.repository
    Log.info(s"Fetching project details for $org/$repo...")
    val details =
      try ScaladexApi.project(org, repo)
      catch case e: ApiException => handleApiError(e)
    Log.info(
      s"groupId: ${details.groupId}, latest: ${details.version}, ${details.artifacts.size} artifact(s)"
    )

    val artifacts = Interactive.chooseMultiple(
      "Select artifact(s):",
      details.artifacts,
      identity
    )

    val version = Interactive.choose(
      "Select version:",
      details.versions.take(10),
      identity
    )

    val bt = btOpt
      .orElse(fileOpt.map(Formatter.detectBuildTool))
      .getOrElse(BuildTool.ScalaCLI)
    Log.info(s"Using build tool: $bt")

    val depLines =
      artifacts.map(a => Formatter.formatDep(bt, details.groupId, a, version))

    fileOpt match
      case Some(filePath) if bt == BuildTool.ScalaCLI =>
        Log.info(s"Inserting into $filePath...")
        var failed = false
        depLines.foreach { dep =>
          Formatter.insertDepIntoFile(filePath, dep) match
            case Left(err) =>
              System.err.println(s"Error: $err")
              failed = true
            case Right(_) => ()
        }
        if !failed then
          println(s"Added to $filePath:")
          depLines.foreach(println)
      case Some(filePath) =>
        println(s"Add to your build file ($filePath):")
        depLines.foreach(println)
      case None =>
        depLines.foreach(println)

    val clipText = depLines.mkString("\n")
    Log.info("Copying to clipboard...")
    if Clipboard.copy(clipText) then println("(copied to clipboard)")
    else Log.info("Clipboard not available")

object Main
    extends CommandApp(
      name = "scaladex-cli",
      header = "Search Scaladex and generate dependency declarations for Scala projects",
      main = {
        val verboseOpt = Opts
          .flag("verbose", short = "v", help = "Show what is happening")
          .orFalse

        val buildToolOpt = Opts
          .option[String](
            long = "build-tool",
            short = "b",
            help = s"Build tool format: ${BuildTool.all}",
            metavar = "tool"
          )
          .mapValidated { s =>
            BuildTool
              .fromString(s)
              .toValidNel(s"Unknown build tool '$s'. Expected one of: ${BuildTool.all}")
          }
          .orNone

        val scalaVersionOpt = Opts
          .option[String](
            long = "scala",
            short = "s",
            help = "Scala version to target (default: 3)",
            metavar = "version"
          )
          .withDefault("3")

        val targetOpt = Opts
          .option[String](
            long = "target",
            short = "t",
            help = "Platform target (default: JVM)",
            metavar = "target"
          )
          .withDefault("JVM")

        val searchCmd = Opts.subcommand("search", "Search and copy dependency to clipboard") {
          val query = Opts.argument[String]("query")
          (query, buildToolOpt, scalaVersionOpt, targetOpt, verboseOpt).mapN { (q, btOpt, sv, tgt, v) =>
            Log.verbose = v
            Commands.interactiveSearch(q, btOpt, sv, tgt, fileOpt = None)
          }
        }

        val depCmd = Opts.subcommand("dep", "Get dependency declaration for a library") {
          val orgRepo = Opts.argument[String]("org/repo")
          (orgRepo, buildToolOpt, verboseOpt).mapN { (or, btOpt, v) =>
            Log.verbose = v
            val bt = btOpt.getOrElse(BuildTool.ScalaCLI)
            val parts = or.split("/")
            if parts.length != 2 then
              System.err.println("Expected format: org/repo (e.g. typelevel/cats-core)")
              sys.exit(1)
            val (org, repo) = (parts(0), parts(1))
            Log.info(s"Fetching project details for $org/$repo...")
            val details =
              try ScaladexApi.project(org, repo)
              catch case e: ApiException => Commands.handleApiError(e)
            Log.info(s"Using build tool: $bt, groupId: ${details.groupId}, version: ${details.version}")
            details.artifacts.foreach { artifact =>
              println(Formatter.formatDep(bt, details.groupId, artifact, details.version))
            }
          }
        }

        val versionsCmd =
          Opts.subcommand("versions", "List available versions for a library") {
            val orgRepo = Opts.argument[String]("org/repo")
            (orgRepo, verboseOpt).mapN { (or, v) =>
              Log.verbose = v
              val parts = or.split("/")
              if parts.length != 2 then
                System.err.println("Expected format: org/repo (e.g. typelevel/cats-core)")
                sys.exit(1)
              val (org, repo) = (parts(0), parts(1))
              Log.info(s"Fetching versions for $org/$repo...")
              val details =
                try ScaladexApi.project(org, repo)
                catch case e: ApiException => Commands.handleApiError(e)
              Log.info(s"Found ${details.versions.size} version(s)")
              println(s"${org}/${repo} — latest: ${details.version}")
              details.versions.foreach(v2 => println(s"  $v2"))
            }
          }

        val addCmd = Opts.subcommand("add", "Add a dependency to a Scala file") {
          val query = Opts.argument[String]("query")
          val fileOpt = Opts
            .option[String](
              long = "file",
              short = "f",
              help = "Scala file to insert the dependency into",
              metavar = "file"
            )
            .orNone
          (query, fileOpt, buildToolOpt, scalaVersionOpt, targetOpt, verboseOpt).mapN {
            (q, fOpt, btOpt, sv, tgt, v) =>
              Log.verbose = v
              Commands.interactiveSearch(q, btOpt, sv, tgt, fileOpt = fOpt)
          }
        }

        val infoCmd = Opts.subcommand("info", "Show details about a library") {
          val orgRepo = Opts.argument[String]("org/repo")
          (orgRepo, verboseOpt).mapN { (or, v) =>
            Log.verbose = v
            val parts = or.split("/")
            if parts.length != 2 then
              System.err.println("Expected format: org/repo (e.g. typelevel/cats)")
              sys.exit(1)
            val (org, repo) = (parts(0), parts(1))
            Log.info(s"Fetching project details for $org/$repo...")
            val details =
              try ScaladexApi.project(org, repo)
              catch case e: ApiException => Commands.handleApiError(e)
            Log.info(s"Received ${details.artifacts.size} artifact(s), ${details.versions.size} version(s)")

            println(s"$org/$repo")
            println(s"  groupId: ${details.groupId}")
            println(s"  latest:  ${details.version}")
            println(s"  artifacts (${details.artifacts.size}):")
            details.artifacts.foreach(a => println(s"    - $a"))
            println(s"  versions:")
            details.versions.take(10).foreach(v2 => println(s"    - $v2"))
            if details.versions.size > 10 then
              println(s"    ... and ${details.versions.size - 10} more")
          }
        }

        val changelogCmd =
          Opts.subcommand("changelog", "Show recent release notes from GitHub") {
            val orgRepo = Opts.argument[String]("org/repo")
            val countOpt = Opts
              .option[Int](
                long = "count",
                short = "n",
                help = "Number of releases to show (default: 5)",
                metavar = "n"
              )
              .withDefault(5)
            (orgRepo, countOpt, verboseOpt).mapN { (or, count, v) =>
              Log.verbose = v
              val parts = or.split("/")
              if parts.length != 2 then
                System.err.println("Expected format: org/repo (e.g. typelevel/cats)")
                sys.exit(1)
              val (org, repo) = (parts(0), parts(1))
              Log.info(s"Fetching releases from github.com/$org/$repo...")
              val releases = ScaladexApi.releases(org, repo, count)
              if releases.isEmpty then
                println(s"No releases found for $org/$repo")
              else
                println(s"$org/$repo — recent releases:\n")
                releases.foreach { r =>
                  val date = r.published_at.take(10)
                  val title = if r.name.nonEmpty then r.name else r.tag_name
                  println(s"  \u001b[1m${r.tag_name}\u001b[0m  $date")
                  if title != r.tag_name then println(s"  $title")
                  if r.body.nonEmpty then
                    val lines = r.body.linesIterator.take(8).toList
                    lines.foreach(l => println(s"    $l"))
                    if r.body.linesIterator.size > 8 then
                      println(s"    ...")
                  println()
                }
            }
          }

        val templateCmd =
          Opts.subcommand("template", "Generate a starter file from a template") {
            val listFlag = Opts.flag("list", short = "l", help = "List available templates").orFalse
            val name = Opts.argument[String]("name").orNone
            val file = Opts.argument[String]("file").orNone
            (listFlag, name, file, verboseOpt).mapN { (showList, nameOpt, fileOpt, v) =>
              Log.verbose = v
              if showList || nameOpt.isEmpty then
                println("Available templates:")
                Templates.all.foreach { t =>
                  println(s"  ${t.name.padTo(14, ' ')} ${t.description}")
                }
                println()
                println("Usage: scaladex-cli template <name> [file]")
              else
                val tplName = nameOpt.get
                Templates.get(tplName) match
                  case None =>
                    System.err.println(s"Unknown template '$tplName'. Available: ${Templates.names}")
                    sys.exit(1)
                  case Some(tpl) =>
                    val outPath = fileOpt.getOrElse(tpl.filename)
                    Log.info(s"Using template: ${tpl.name} (${tpl.description})")
                    val outFile = new java.io.File(outPath)
                    if outFile.exists() then
                      System.err.println(s"File already exists: $outPath")
                      sys.exit(1)
                    Log.info(s"Writing to $outPath...")
                    val writer = new java.io.PrintWriter(outPath)
                    try writer.write(tpl.content)
                    finally writer.close()
                    println(s"Created $outPath")
                    println(s"Run with: scala-cli run $outPath")
            }
          }

        val completionsCmd =
          Opts.subcommand("completions", "Generate shell completion scripts") {
            val shell = Opts.argument[String]("shell")
            shell.map { sh =>
              sh.toLowerCase match
                case "bash" => println(Completions.bash)
                case "zsh"  => println(Completions.zsh)
                case "fish" => println(Completions.fish)
                case _ =>
                  System.err.println(s"Unknown shell '$sh'. Expected: bash, zsh, or fish")
                  sys.exit(1)
            }
          }

        val versionCmd = Opts.subcommand("version", "Print version") {
          Opts.unit.map(_ => println("scaladex-cli 0.1.0"))
        }

        searchCmd orElse depCmd orElse versionsCmd orElse addCmd orElse infoCmd orElse changelogCmd orElse templateCmd orElse completionsCmd orElse versionCmd
      }
    )
