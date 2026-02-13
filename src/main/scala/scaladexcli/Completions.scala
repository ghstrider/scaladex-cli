package scaladexcli

object Completions:

  val bash: String =
    """|_scaladex_cli() {
       |  local cur commands
       |  COMPREPLY=()
       |  cur="${COMP_WORDS[COMP_CWORD]}"
       |  commands="search dep versions add info template completions"
       |
       |  if [ "$COMP_CWORD" -eq 1 ]; then
       |    COMPREPLY=( $(compgen -W "$commands" -- "$cur") )
       |    return 0
       |  fi
       |
       |  case "${COMP_WORDS[1]}" in
       |    search|dep|add)
       |      local opts="--build-tool --scala --target"
       |      COMPREPLY=( $(compgen -W "$opts" -- "$cur") )
       |      ;;
       |    versions|info)
       |      ;;
       |    template)
       |      local templates="hello cats-effect http4s zio fs2 scalatest munit os-lib tapir"
       |      COMPREPLY=( $(compgen -W "$templates --list" -- "$cur") )
       |      ;;
       |    completions)
       |      COMPREPLY=( $(compgen -W "bash zsh fish" -- "$cur") )
       |      ;;
       |  esac
       |}
       |complete -F _scaladex_cli scaladex-cli""".stripMargin

  val zsh: String =
    """|#compdef scaladex-cli
       |
       |_scaladex-cli() {
       |  local -a commands
       |  commands=(
       |    'search:Search Scaladex for libraries'
       |    'dep:Get dependency declaration for a library'
       |    'versions:List available versions for a library'
       |    'add:Interactively add a dependency to a file'
       |    'info:Show details about a library'
       |    'template:Generate a starter file from a template'
       |    'completions:Generate shell completion scripts'
       |  )
       |
       |  _arguments -C \
       |    '1:command:->cmd' \
       |    '*::arg:->args'
       |
       |  case $state in
       |    cmd)
       |      _describe 'command' commands
       |      ;;
       |    args)
       |      case $words[1] in
       |        search|dep|add)
       |          _arguments \
       |            '(-b --build-tool)'{-b,--build-tool}'[Build tool format]:tool:(sbt mill scalacli ammonite)' \
       |            '(-s --scala)'{-s,--scala}'[Scala version]:version:' \
       |            '(-t --target)'{-t,--target}'[Platform target]:target:' \
       |            '*:query:'
       |          ;;
       |        template)
       |          _arguments \
       |            '(-l --list)'{-l,--list}'[List available templates]' \
       |            '1:template:(hello cats-effect http4s zio fs2 scalatest munit os-lib tapir)' \
       |            '2:file:_files'
       |          ;;
       |        completions)
       |          _arguments '1:shell:(bash zsh fish)'
       |          ;;
       |        versions|info)
       |          _arguments '*:org/repo:'
       |          ;;
       |      esac
       |      ;;
       |  esac
       |}
       |
       |_scaladex-cli""".stripMargin

  val fish: String =
    """|complete -c scaladex-cli -n '__fish_use_subcommand' -a search -d 'Search Scaladex for libraries'
       |complete -c scaladex-cli -n '__fish_use_subcommand' -a dep -d 'Get dependency declaration'
       |complete -c scaladex-cli -n '__fish_use_subcommand' -a versions -d 'List available versions'
       |complete -c scaladex-cli -n '__fish_use_subcommand' -a add -d 'Interactively add a dependency'
       |complete -c scaladex-cli -n '__fish_use_subcommand' -a info -d 'Show details about a library'
       |complete -c scaladex-cli -n '__fish_use_subcommand' -a template -d 'Generate a starter file'
       |complete -c scaladex-cli -n '__fish_use_subcommand' -a completions -d 'Generate shell completions'
       |complete -c scaladex-cli -n '__fish_seen_subcommand_from search dep add' -s b -l build-tool -d 'Build tool format' -a 'sbt mill scalacli ammonite'
       |complete -c scaladex-cli -n '__fish_seen_subcommand_from search dep add' -s s -l scala -d 'Scala version'
       |complete -c scaladex-cli -n '__fish_seen_subcommand_from search dep add' -s t -l target -d 'Platform target'
       |complete -c scaladex-cli -n '__fish_seen_subcommand_from template' -a 'hello cats-effect http4s zio fs2 scalatest munit os-lib tapir' -d 'Template name'
       |complete -c scaladex-cli -n '__fish_seen_subcommand_from template' -s l -l list -d 'List templates'
       |complete -c scaladex-cli -n '__fish_seen_subcommand_from completions' -a 'bash zsh fish'""".stripMargin
