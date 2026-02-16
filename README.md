# scaladex-cli

You know some Scala. You want to try a library. But first you need to figure out the groupId, the artifact name, the latest version, whether it's `%%` or `%%%`, and which build tool syntax to use. By the time you've got the dependency string, you've lost your flow.

scaladex-cli fixes that. Search for a library, pick it from a list, and the dependency string lands in your clipboard (or straight into your file). No browser, no copy-pasting from Maven Central.

## Install

### Homebrew (recommended)

```bash
brew tap ghstrider/tap
brew install scaladex-cli
```

### Shell script

```bash
curl -fsSL https://raw.githubusercontent.com/ghstrider/scaladex-cli/main/install.sh | sh
```

Works on macOS (Apple Silicon, Intel) and Linux x86_64.

## Quick start

**Search for a library and copy the dep to clipboard:**

```
scaladex-cli search cats-core
```

Arrow keys to pick the project, artifacts, and version. The result gets copied to your clipboard automatically.

**Add a dep directly into a scala-cli file:**

```
scaladex-cli add -f app.scala cats-effect
```

Same interactive flow, but the `//> using dep` line gets inserted at the top of your file.

**Don't want to start from scratch? Use a template:**

```
scaladex-cli template cats-effect
```

This creates `app.scala` with the dep and a hello world you can run immediately:

```
scala-cli run app.scala
```

## All commands

| Command | What it does |
|---------|-------------|
| `search <query>` | Interactive search, copies dep to clipboard |
| `add <query>` | Same as search. Use `-f file.scala` to inject into a file |
| `dep <org/repo>` | Print dep string (non-interactive, for scripting) |
| `versions <org/repo>` | List available versions |
| `info <org/repo>` | Show groupId, artifacts, and versions |
| `changelog <org/repo>` | Show recent GitHub release notes |
| `template <name>` | Generate a starter file (`--list` to see all) |
| `completions <shell>` | Shell completions for bash/zsh/fish |
| `version` | Print version |

## Flags

These work on `search`, `add`, and `dep`:

- `-b, --build-tool <tool>` — Output format: `sbt`, `mill`, `scalacli`, `ammonite`
- `-s, --scala <version>` — Scala version to target (default: `3`)
- `-t, --target <target>` — Platform target (default: `JVM`)
- `-v, --verbose` — Show what's happening under the hood

## Templates

```
scaladex-cli template --list
```

Available: `hello`, `cats-effect`, `http4s`, `zio`, `fs2`, `scalatest`, `munit`, `os-lib`, `tapir`

Each one creates a ready-to-run scala-cli file. Just `scala-cli run <file>`.

## Shell completions

```bash
# zsh
scaladex-cli completions zsh > ~/.zfunc/_scaladex-cli

# bash
scaladex-cli completions bash >> ~/.bashrc

# fish
scaladex-cli completions fish > ~/.config/fish/completions/scaladex-cli.fish
```

## How it works

It's a Scala Native binary — no JVM startup, instant response. It talks to the [Scaladex API](https://index.scala-lang.org) for library search and the GitHub API for changelogs. Terminal UI uses raw mode with ANSI escape codes for the arrow-key selection.

## License

Apache 2.0
