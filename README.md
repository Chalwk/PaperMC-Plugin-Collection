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

| Plugin                                                | Version                                                                                                                                         | Docs                                                                                         | Releases                                                                                         | What it does                                                                                                        |
| ----------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------- |
| [`VacuLoot`](papermc_plugins/vaculoot/)               | [![v1.0.0](https://img.shields.io/badge/v-1.0.0-blue)](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/vaculoot-v1.0.0)        | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/vaculoot/)        | [All releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=vaculoot-v)        | Toggleable item and XP magnet with tiered ranges, optional Vault economy cost, world allow-list, and item blacklist |
| [`GameModeManager`](papermc_plugins/gamemodemanager/) | [![v1.0.0](https://img.shields.io/badge/v-1.0.0-blue)](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/gamemodemanager-v1.0.0) | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/gamemodemanager/) | [All releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=gamemodemanager-v) | Separate inventories and player state per gamemode, with world-change preservation                                  |
| [`NoctiView`](papermc_plugins/noctiview/)             | [![v1.0.0](https://img.shields.io/badge/v-1.0.0-blue)](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/noctiview-v1.0.0)       | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/noctiview/)       | [All releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=noctiview-v)       | Per-world night vision toggle with configurable particles, sound feedback, and admin world controls                 |

### Administration

| Plugin                                          | Version                                                                                                                                      | Docs                                                                                      | Releases                                                                                      | What it does                                                                                      |
| ----------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| [`BigBrother`](papermc_plugins/bigbrother/)     | [![v1.0.1](https://img.shields.io/badge/v-1.0.1-blue)](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/bigbrother-v1.0.1)   | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/bigbrother/)   | [All releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=bigbrother-v)   | Command, sign, anvil, book, and portal spies with per-player toggles and filters                  |
| [`AdminChat`](papermc_plugins/adminchat/)       | [![v1.0.0](https://img.shields.io/badge/v-1.0.0-blue)](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/adminchat-v1.0.0)    | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/adminchat/)    | [All releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=adminchat-v)    | Multi-channel staff chat with per-channel permissions, formatting, sounds, and visibility toggles |
| [`AutoMessages`](papermc_plugins/automessages/) | [![v1.0.0](https://img.shields.io/badge/v-1.0.0-blue)](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/automessages-v1.0.0) | [Read the docs](https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/automessages/) | [All releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=automessages-v) | Scheduled broadcasts with legacy colors and JSON click/hover components                           |

---

## Releases

Each plugin is released independently, tagged as `<plugin>-v<version>`. The
`Release` workflow builds only the tagged plugin and attaches its JAR to the
GitHub Release.

To download the latest build of a specific plugin:

- **VacuLoot** - [latest](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/vaculoot-v1.0.0) · [all releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=vaculoot-v)
- **GameModeManager** - [latest](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/gamemodemanager-v1.0.0) · [all releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=gamemodemanager-v)
- **NoctiView** - [latest](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/noctiview-v1.0.0) · [all releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=noctiview-v)
- **BigBrother** - [latest](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/bigbrother-v1.0.1) · [all releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=bigbrother-v)
- **AdminChat** - [latest](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/adminchat-v1.0.0) · [all releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=adminchat-v)
- **AutoMessages** - [latest](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/tag/automessages-v1.0.0) · [all releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases?q=automessages-v)

Or browse [all releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases).

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

The `Release` workflow builds only that plugin at the tagged version, verifies
the resulting JAR contains a `plugin.yml` with the version baked in correctly,
and only then publishes a GitHub Release with the JAR attached
(e.g. `VacuLoot-1.2.3.jar`). Tags that don't match `<plugin>-v<semver>` are
rejected before the build starts.

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