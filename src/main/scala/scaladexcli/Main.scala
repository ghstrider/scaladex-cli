package scaladexcli

import com.monovore.decline.*

object Main
    extends CommandApp(
      name = "scaladex-cli",
      header = "Search Scaladex and generate dependency declarations for Scala projects",
      main = {
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
          .withDefault(BuildTool.ScalaCLI)

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

        val searchCmd = Opts.subcommand("search", "Search Scaladex for libraries") {
          val query = Opts.argument[String]("query")
          (query, scalaVersionOpt, targetOpt).mapN { (q, sv, tgt) =>
            val results = ScaladexApi.search(q, tgt, sv)
            if results.isEmpty then println(s"No results found for '$q'")
            else
              results.zipWithIndex.foreach { case (r, i) =>
                println(s"[${i + 1}] ${r.organization}/${r.repository}")
                println(s"    artifacts: ${r.artifacts.mkString(", ")}")
              }
          }
        }

        val depCmd = Opts.subcommand("dep", "Get dependency declaration for a library") {
          val orgRepo = Opts.argument[String]("org/repo")
          (orgRepo, buildToolOpt).mapN { (or, bt) =>
            val parts = or.split("/")
            if parts.length != 2 then
              System.err.println("Expected format: org/repo (e.g. typelevel/cats-core)")
              sys.exit(1)
            val (org, repo) = (parts(0), parts(1))
            val details = ScaladexApi.project(org, repo)
            details.artifacts.foreach { artifact =>
              println(Formatter.formatDep(bt, details.groupId, artifact, details.version))
            }
          }
        }

        val versionsCmd =
          Opts.subcommand("versions", "List available versions for a library") {
            val orgRepo = Opts.argument[String]("org/repo")
            orgRepo.map { or =>
              val parts = or.split("/")
              if parts.length != 2 then
                System.err.println("Expected format: org/repo (e.g. typelevel/cats-core)")
                sys.exit(1)
              val (org, repo) = (parts(0), parts(1))
              val details = ScaladexApi.project(org, repo)
              println(s"${org}/${repo} — latest: ${details.version}")
              details.versions.foreach(v => println(s"  $v"))
            }
          }

        searchCmd orElse depCmd orElse versionsCmd
      }
    )
