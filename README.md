# PaperMC-Plugin-Collection

[![Website][website-badge]][repo_url]
[![Build][build-badge]][build-workflow]
[![Security Policy][security-badge]][security-policy]
[![License: MIT][license-badge]][license]

A collection of plugins for **PaperMC** (and compatible forks). Every plugin is a complete, standalone JAR - no shared library,
no dependency chain between plugins.

Full documentation, downloads, and release notes for each plugin live on
the [website][repo_url].

---

## Plugins

### Gameplay

| Plugin                                      | Docs                                  | What it does                                                                                                         |
| ------------------------------------------- | ------------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| [`GameModeManager`][gamemodemanager-source] | [Read the docs][gamemodemanager-docs] | Separate inventories and player state per game mode, with world-change preservation.                                 |
| [`Handbook`][handbook-source]               | [Read the docs][handbook-docs]        | Gives every player a written book guide to the server, with chapters configurable in a separate `handbook.yml`.      |
| [`NoctiView`][noctiview-source]             | [Read the docs][noctiview-docs]       | Per-world night vision toggle with configurable particles, sound feedback, and admin world controls.                 |
| [`PerkMenu`][perkmenu-source]               | [Read the docs][perkmenu-docs]        | Paginated chat-based perk browser that reads ownership from permissions, with prices, descriptions, and buy links.   |
| [`VacuLoot`][vaculoot-source]               | [Read the docs][vaculoot-docs]        | Toggleable item and XP magnet with tiered ranges, optional Vault economy cost, world allow-list, and item blacklist. |

### Administration

| Plugin                                        | Docs                                   | What it does                                                                                       |
| --------------------------------------------- | -------------------------------------- | -------------------------------------------------------------------------------------------------- |
| [`AdminChat`][adminchat-source]               | [Read the docs][adminchat-docs]        | Multi-channel staff chat with per-channel permissions, formatting, sounds, and visibility toggles. |
| [`AutoMessages`][automessages-source]         | [Read the docs][automessages-docs]     | Scheduled broadcasts with legacy colors and JSON click/hover components.                           |
| [`BigBrother`][bigbrother-source]             | [Read the docs][bigbrother-docs]       | Command, sign, anvil, book, and portal spies with per-player toggles and filters.                  |
| [`NexusPermissions`][nexuspermissions-source] | [Read the docs][nexuspermissions-docs] | Permissions plugin with groups, inheritance, per-world contexts, and promote/demote ladders.       |

> **Note:** NexusPermissions is still under active development. The core
> feature set works, but the command surface and config schema may change
> between releases. See its docs page for details.

---

## Downloads

Each plugin is released independently. The [website][repo_url] links to the latest
JAR for every plugin, or you can browse [all releases][releases] on GitHub.

---

## Requirements

- **Java 21** or newer - required by Paper 1.21+
- **PaperMC**, **Purpur**, or **Spigot** server
  - Minecraft **1.21+**; check individual plugin docs for exact version support
- **Gradle 8.x** to build from source (or use the included `./gradlew` wrapper)
- No external dependencies unless a plugin says otherwise in its docs

> **Build JVM note:** the Gradle wrapper (8.10.2) does not yet run on Java 25
> or newer. If your default JDK is newer than the wrapper supports, point
> `org.gradle.java.home` at a compatible JDK via a local `gradle.properties`,
> or set `JAVA_HOME` accordingly. The compiled output still targets Java 21
> bytecode regardless of which JDK builds it.

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

## Releasing (for maintainers)

Releases are tag-driven. Each plugin is released independently, tagged as
`<plugin>-v<semver>`. Pushing a tag matching that format triggers the
`Release` workflow, which builds only the tagged plugin, verifies the
resulting JAR, and publishes a GitHub Release with the JAR attached.

### Prerequisites

- Write access to the repository.
- A local clone with the plugin's source ready to release.
- Java 21 installed, or a working `./gradlew` wrapper.

### Tag format

Tags must match `<plugin>-v<semver>`, all lowercase:

| Valid                    | Invalid                  | Why                          |
| ------------------------ | ------------------------ | ---------------------------- |
| `gamemodemanager-v1.0.1` | `GameModeManager-v1.0.1` | Uppercase                    |
| `vaculoot-v2.1.3`        | `vaculoot-2.1.3`         | Missing `v`                  |
| `adminchat-v1.0.0`       | `adminchat_v1.0.0`       | Underscore instead of hyphen |

Tags that don't match are rejected by `release.yml` before the build starts.

### Before tagging

One file references the release version and must be updated before the
release commit:

**`_mcplugins/<plugin>.md`** - bump `latest_version` in the frontmatter. The
plugin page's **Download** button is built from this field. If it's skipped,
the site will keep pointing at the previous release after the new one ships.

### Checklist

Run from the repository root.

1. Edit the plugin's Java source as needed.

2. Bump `latest_version` in `_mcplugins/<plugin>.md`.

3. **(Optional)** Build locally to catch CI failures early:

   ```bash
   ./gradlew :<plugin>:clean :<plugin>:build -Pversion=<version>
   ```

   Change `<version>` in this command too - omitting `-Pversion` builds with
   the default `0.0.0-dev`. The resulting JAR appears at
   `papermc_plugins/<plugin>/build/libs/`.

4. Stage and commit:

   ```bash
   git add .
   git commit -m "fix(<plugin>): <summary>, release <version>"
   ```

5. Push to `main`:

   ```bash
   git push origin main
   ```

   This triggers `build.yml` (compiles every plugin) and `jekyll.yml`
   (rebuilds the docs site). Wait for `build.yml` to pass before tagging -
   if it fails, fix the problem, commit, and push again.

6. Create the tag:

   ```bash
   git tag -a <plugin>-v<version> -m "Release <Plugin> <version>"
   ```

7. Push the tag:

   ```bash
   git push origin <plugin>-v<version>
   ```

   This triggers `release.yml`, which:

   - Parses the tag into a plugin name and version.
   - Verifies `papermc_plugins/<plugin>/` exists.
   - Builds with `-Pversion=<version>`.
   - Verifies the JAR's `plugin.yml` contains the correct version, with no
     leftover `${version}` placeholder.
   - Publishes a GitHub Release with the JAR attached.

8. Watch the **Actions** tab for `release.yml` to complete. When it's green,
   the release is live and the JAR is downloadable.

### Worked example

Releasing `GameModeManager` `1.0.0` → `1.0.1`:

```bash
./gradlew :gamemodemanager:clean :gamemodemanager:build -Pversion=1.0.1

git add .
git commit -m "fix(gamemodemanager): harden state handling, release 1.0.1"
git push origin main

git tag -a gamemodemanager-v1.0.1 -m "Release GameModeManager 1.0.1"
git push origin gamemodemanager-v1.0.1
```

On Windows CMD, drop the `./` prefix (`gradlew`, not `./gradlew`). In
PowerShell, use `.\gradlew`.

### If `release.yml` fails

A tag is an immutable pointer to a commit. You cannot simply re-run the
workflow against a corrected commit - the tag itself has to move.

Delete the tag locally and remotely:

```bash
git tag -d <plugin>-v<version>
git push origin :refs/tags/<plugin>-v<version>
```

Fix the underlying issue, commit, and push to `main`. Then repeat steps 6-7
with the same tag name. The tag now points at the corrected commit and
`release.yml` will re-run.

### Releasing multiple plugins

Each plugin is tagged and pushed independently, and each tag fires its own
`release.yml` run. To release several at once:

1. Update the docs for every plugin being released (step 2).
2. Commit once.
3. Push to `main` once.
4. Tag and push each plugin separately.

---

## Installing

1. Build from source (above), or download a release JAR from the [Releases][releases] page.
1. Drop the JAR into your server's `plugins/` directory.
2. Restart the server (or use a plugin manager that supports hot-loading).
3. Edit the generated config in `plugins/<PluginName>/config.yml` as needed.
4. Run `/reload confirm` or restart to apply config changes.

Every plugin generates its own config on first run with sensible defaults.

---

## Contributing

Bugs, ideas, and pull requests are welcome. See [CONTRIBUTING.md][contributing]
for how to report a bug, suggest a plugin, or open a PR.

---

## Security

**Do not open a public issue for security problems.** See [SECURITY.md][security-policy]
for the private reporting channels, scope, and expected timelines.

---

## License

MIT - see [LICENSE][license].

Copyright (c) 2026 Jericho Crosby (Chalwk)

<!-- Repository status, contribution, and project links -->
[repo_url]: https://chalwk.github.io/PaperMC-Plugin-Collection/
[build-badge]: https://github.com/Chalwk/PaperMC-Plugin-Collection/actions/workflows/build.yml/badge.svg
[license-badge]: https://img.shields.io/badge/license-MIT-green
[security-badge]: https://img.shields.io/badge/security-policy-blue
[website-badge]: https://img.shields.io/badge/website-chalwk.github.io%2FPaperMC--Plugin--Collection-blue
[build-workflow]: https://github.com/Chalwk/PaperMC-Plugin-Collection/actions/workflows/build.yml
[releases]: https://github.com/Chalwk/PaperMC-Plugin-Collection/releases
[contributing]: CONTRIBUTING.md
[security-policy]: SECURITY.md
[license]: LICENSE

<!-- Plugin documentation and source links -->
[adminchat-docs]: https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/adminchat/
[adminchat-source]: https://github.com/Chalwk/PaperMC-Plugin-Collection/tree/main/papermc_plugins/adminchat

[automessages-docs]: https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/automessages/
[automessages-source]: https://github.com/Chalwk/PaperMC-Plugin-Collection/tree/main/papermc_plugins/automessages

[bigbrother-docs]: https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/bigbrother/
[bigbrother-source]: https://github.com/Chalwk/PaperMC-Plugin-Collection/tree/main/papermc_plugins/bigbrother

[gamemodemanager-docs]: https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/gamemodemanager/
[gamemodemanager-source]: https://github.com/Chalwk/PaperMC-Plugin-Collection/tree/main/papermc_plugins/gamemodemanager

[handbook-docs]: https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/handbook/
[handbook-source]: https://github.com/Chalwk/PaperMC-Plugin-Collection/tree/main/papermc_plugins/handbook

[nexuspermissions-docs]: https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/nexuspermissions/
[nexuspermissions-source]: https://github.com/Chalwk/PaperMC-Plugin-Collection/tree/main/papermc_plugins/nexuspermissions

[noctiview-docs]: https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/noctiview/
[noctiview-source]: https://github.com/Chalwk/PaperMC-Plugin-Collection/tree/main/papermc_plugins/noctiview

[perkmenu-docs]: https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/perkmenu/
[perkmenu-source]: https://github.com/Chalwk/PaperMC-Plugin-Collection/tree/main/papermc_plugins/perkmenu

[vaculoot-docs]: https://chalwk.github.io/PaperMC-Plugin-Collection/plugins/vaculoot/
[vaculoot-source]: https://github.com/Chalwk/PaperMC-Plugin-Collection/tree/main/papermc_plugins/vaculoot