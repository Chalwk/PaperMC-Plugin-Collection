# PaperMC-Plugin-Collection

[![Website](https://img.shields.io/badge/website-chalwk.github.io%2FPaperMC--Plugin--Collection-blue)](https://chalwk.github.io/PaperMC-Plugin-Collection/)
[![Build](https://github.com/Chalwk/PaperMC-Plugin-Collection/actions/workflows/build.yml/badge.svg)](https://github.com/Chalwk/PaperMC-Plugin-Collection/actions/workflows/build.yml)
[![Security Policy](https://img.shields.io/badge/security-policy-blue)](SECURITY.md)
[![License: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE)

A collection of plugins for **PaperMC** (and compatible forks such as Spigot and Bukkit). Every plugin is a complete, standalone
JAR - no shared library, no dependency chain between plugins.

Full documentation for each plugin lives on the [website](https://chalwk.github.io/PaperMC-Plugin-Collection/).

---

## Index

### Gameplay

| Plugin                                                | Docs                                                                                         | What it does                                                                                                        |
| ----------------------------------------------------- | -------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| [`VacuLoot`](papermc_plugins/vaculoot/)               | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/vaculoot/)        | Toggleable item and XP magnet with tiered ranges, optional Vault economy cost, world allow-list, and item blacklist |
| [`GameModeManager`](papermc_plugins/gamemodemanager/) | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/gamemodemanager/) | Separate inventories and player state per gamemode, with world-change preservation                                  |
| [`NoctiView`](papermc_plugins/noctiview/)             | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/noctiview/)       | Per-world night vision toggle with configurable particles, sound feedback, and admin world controls                 |

### Administration

| Plugin                                          | Docs                                                                                      | What it does                                                                                      |
| ----------------------------------------------- | ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| [`BigBrother`](papermc_plugins/bigbrother/)     | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/bigbrother/)   | Command, sign, anvil, book, and portal spies with per-player toggles and filters                  |
| [`AdminChat`](papermc_plugins/adminchat/)       | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/adminchat/)    | Multi-channel staff chat with per-channel permissions, formatting, sounds, and visibility toggles |
| [`AutoMessages`](papermc_plugins/automessages/) | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/automessages/) | Scheduled broadcasts with legacy colors and JSON click/hover components                           |

---

## Requirements

- **Java 21** or newer - required by Paper 1.21+
- **PaperMC**, **Purpur**, or **Spigot** server
  - Minecraft **1.21+**; check individual plugin docs for exact version support
- **Gradle 8.x** to build from source (or use the included `./gradlew` wrapper)
- No external dependencies unless a plugin says otherwise in its docs

---

## Building

Clone the repository and build all plugins:

```bash
git clone https://github.com/Chalwk/PaperMC-Plugin-Collection.git
cd PaperMC-Plugin-Collection
./gradlew build
```

Each plugin produces its own JAR in its `<plugin>/build/libs/` directory.
Copy the JAR you want into your server's `plugins/` folder and restart.

To build a single plugin:

```bash
./gradlew :vaculoot:build
```

Replace `vaculoot` with the project name of the plugin you want (see
`settings.gradle` or the table above for names).

To build a specific plugin at a specific version (matches the release workflow):

```bash
./gradlew :vaculoot:build -Pversion=1.2.3
```

---

## Releasing

Releases are tag-driven. To cut a release for a single plugin:

```bash
git tag vaculoot-v1.2.3
git push origin vaculoot-v1.2.3
```

The `Release` workflow builds only that plugin at the tagged version and
publishes a GitHub Release with the corresponding JAR attached
(e.g. `vaculoot-1.2.3.jar`). Tags that don't match `<plugin>-v<semver>` are
rejected with an error.

---

## Installing

1. Build from source (above), or download a release JAR from the
   [Releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases) page.
2. Drop the JAR into your server's `plugins/` directory.
3. Restart the server (or use a plugin manager that supports hot-loading).
4. Edit the generated config in `plugins/<PluginName>/config.yml` as needed.
5. Run `/reload confirm` or restart to apply config changes.

Every plugin generates its own config on first run with sensible defaults.

---

## Conventions

Plugins in this collection try to follow a few house rules:

- **Java 21+**, Paper API preferred. If a plugin genuinely needs a
  platform-specific API (Spigot, Bukkit), it says so in its docs.
- **No cross-plugin dependencies.** Every plugin is standalone. If two plugins
  need to talk, they do it via events or a soft-depend, never a hard compile
  dependency.
- **`/pluginname help` always works.** Every command has a help subcommand,
  tab completion, and permission nodes.
- **Permissions are granular.** No `pluginname.*` wildcards that silently grant
  everything. Each action has its own node.
- **Configs are versioned.** Config files include a `config-version` key so
  migrations are possible without guessing.
- **No hardcoded messages.** Every user-facing string is in `config.yml` or a
  `messages.yml`.
- **Destructive actions are opt-in**, not default. Nothing deletes blocks,
  items, or player data without an explicit confirmation flag.
- **MIT licensed.** Take what's useful.

---

## Layout

```
papermc_plugins/
├── adminchat/          AdminChat
├── automessages/       AutoMessages
├── bigbrother/         BigBrother
├── gamemodemanager/    GameModeManager
├── noctiview/          NoctiView
└── vaculoot/           VacuLoot

_mcplugins/             Jekyll collection: one doc page per plugin
_includes/              Jekyll partials (head, header, footer)
_layouts/               Jekyll layouts (default, page, plugin)
assets/css/             Jekyll site stylesheet

.github/                Issue templates, PR template, CI workflows
build.gradle            Root build script
settings.gradle         Project includes
gradle/                 Wrapper + version catalog
gradlew, gradlew.bat    Gradle wrapper scripts
```

---

## Contributing

Bugs, ideas, and pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md)
for how to report a bug, suggest a plugin, or open a PR.

---

## Security

**Do not open a public issue for security problems.** See [SECURITY.md](SECURITY.md)
for the private reporting channels, scope, and expected timelines.

---

## License

MIT - see [LICENSE](LICENSE).

Copyright (c) 2026 Jericho Crosby (Chalwk)