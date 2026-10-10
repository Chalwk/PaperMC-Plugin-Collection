---
layout: plugin
title: BigBrother
description: Player activity monitoring through command, sign, anvil, book, and portal spies with per-player toggles and filters.
category: Administration
plugin_id: bigbrother
latest_version: "1.0.3"
author: Chalwk
website: https://github.com/Chalwk/PaperMC-Plugin-Collection
api-version: 1.21
main: com.chalwk.BigBrother
source_path: papermc_plugins/bigbrother
minecraft_versions: "1.21+"
server_software: "Paper, Purpur, Spigot"
java_version: "21+"
tags:
  - monitoring
  - logging
  - staff
features:
  - "Five spy types: command, sign, anvil, book, portal"
  - "Per-player and per-spy toggles with `.toggle.others` support"
  - "Excluded commands, players, and worlds (matched case-insensitively for commands and players)"
  - "Configurable per-spy message formats with placeholders"
  - "Console mirroring for every notification"
  - "Runs on Spigot, Paper, and Purpur"
commands:
  - name: bigbrother
    description: Manage BigBrother spy features
    usage: /bigbrother [help|status|commands|signs|anvils|books|portals|reload] [player]
    permission: bigbrother.use
    aliases: [bb]
permissions:
  - name: bigbrother.*
    description: All BigBrother permissions
    children:
      - bigbrother.use
      - bigbrother.reload
      - bigbrother.spy.*
  - name: bigbrother.use
    description: Allows using BigBrother commands
    default: op
  - name: bigbrother.reload
    description: Allows reloading BigBrother configuration
    default: op
  - name: bigbrother.spy.*
    description: All spy features
    children:
      - bigbrother.commandspy.*
      - bigbrother.signspy.*
      - bigbrother.anvilspy.*
      - bigbrother.bookspy.*
      - bigbrother.portalspy.*
  - name: bigbrother.commandspy.*
    description: All command spy permissions
    children:
      - bigbrother.commandspy.toggle
      - bigbrother.commandspy.toggle.others
  - name: bigbrother.commandspy.toggle
    description: Allows toggling command spy
    default: op
  - name: bigbrother.commandspy.toggle.others
    description: Allows toggling command spy for others
    default: op
  - name: bigbrother.signspy.*
    description: All sign spy permissions
    children:
      - bigbrother.signspy.toggle
      - bigbrother.signspy.toggle.others
  - name: bigbrother.signspy.toggle
    description: Allows toggling sign spy
    default: op
  - name: bigbrother.signspy.toggle.others
    description: Allows toggling sign spy for others
    default: op
  - name: bigbrother.anvilspy.*
    description: All anvil spy permissions
    children:
      - bigbrother.anvilspy.toggle
      - bigbrother.anvilspy.toggle.others
  - name: bigbrother.anvilspy.toggle
    description: Allows toggling anvil spy
    default: op
  - name: bigbrother.anvilspy.toggle.others
    description: Allows toggling anvil spy for others
    default: op
  - name: bigbrother.bookspy.*
    description: All book spy permissions
    children:
      - bigbrother.bookspy.toggle
      - bigbrother.bookspy.toggle.others
  - name: bigbrother.bookspy.toggle
    description: Allows toggling book spy
    default: op
  - name: bigbrother.bookspy.toggle.others
    description: Allows toggling book spy for others
    default: op
  - name: bigbrother.portalspy.*
    description: All portal spy permissions
    children:
      - bigbrother.portalspy.toggle
      - bigbrother.portalspy.toggle.others
  - name: bigbrother.portalspy.toggle
    description: Allows toggling portal spy
    default: op
  - name: bigbrother.portalspy.toggle.others
    description: Allows toggling portal spy for others
    default: op
---

# BigBrother

BigBrother monitors player activity through configurable spy features.

## Features

- Spy types: Command, Sign, Anvil, Book, Portal.
- Global server toggle (`global_toggle`) and per-player global toggle via `/bigbrother`.
- Per-player per-spy toggles.
- Toggle other players with `.toggle.others` permissions.
- Filters: excluded commands, players, worlds.
- Notifies staff with matching spy permission, excluding the trigger player, and the console.
- Configurable per-spy messages with placeholders.
- Status command lists enabled spy features.
- Runs on Spigot, Paper, and Purpur

## Commands

All `/bigbrother` subcommands also require `bigbrother.use` because the command's base permission is checked first.

| Command                         | Description                          | Permission                                                                                                     |
| ------------------------------- | ------------------------------------ | -------------------------------------------------------------------------------------------------------------- |
| `/bigbrother`                   | Toggle all spy features for yourself | `bigbrother.use`                                                                                               |
| `/bigbrother help`              | Show help                            | `bigbrother.use`                                                                                               |
| `/bigbrother status`            | Show enabled spy features            | `bigbrother.use`                                                                                               |
| `/bigbrother reload`            | Reload configuration                 | `bigbrother.use` + `bigbrother.reload`                                                                         |
| `/bigbrother commands [player]` | Toggle command spy                   | `bigbrother.use` + `bigbrother.commandspy.toggle`; other player requires `bigbrother.commandspy.toggle.others` |
| `/bigbrother signs [player]`    | Toggle sign spy                      | `bigbrother.use` + `bigbrother.signspy.toggle`; other player requires `bigbrother.signspy.toggle.others`       |
| `/bigbrother anvils [player]`   | Toggle anvil spy                     | `bigbrother.use` + `bigbrother.anvilspy.toggle`; other player requires `bigbrother.anvilspy.toggle.others`     |
| `/bigbrother books [player]`    | Toggle book spy                      | `bigbrother.use` + `bigbrother.bookspy.toggle`; other player requires `bigbrother.bookspy.toggle.others`       |
| `/bigbrother portals [player]`  | Toggle portal spy                    | `bigbrother.use` + `bigbrother.portalspy.toggle`; other player requires `bigbrother.portalspy.toggle.others`   |

## Spy Details

- **Command**: logs commands before execution. Respects `filters.excluded_commands` (matched case-insensitively against the command name, without the leading `/`). Placeholders: `{player}`, `{command}`.
- **Sign**: logs sign edits (`SignChangeEvent`) and right-click sign interactions (`PlayerInteractEvent`). Placeholders: `{player}`, `{sign_lines}`. Only the front side of the sign is currently reported.
- **Anvil**: logs taking the result from an anvil (slot 2). Placeholders: `{player}`, `{old_name}`, `{new_name}`. `{old_name}` is the input item's display name, or its material key if it has no custom name.
- **Book**: logs book edits. Placeholders: `{player}`, `{title}`, `{preview}`. `{preview}` is the first page, truncated to 50 characters.
- **Portal**: logs portal travel (`PlayerPortalEvent`). Placeholders: `{player}`, `{from_world}`, `{to_world}`. End-portal teleports are not covered (they fire `PlayerTeleportEvent`).

## Persistence

Per-player spy state is held in memory only and is **not** persisted across server restarts. State is also dropped when a player quits, so reconnecting players start from `enabled_by_default`.

## Configuration

- `config-version`: schema version for migration.
- `global_toggle`: server-wide master switch.
- `enabled_by_default`: whether new players start with all spies enabled.
- `spy.<type>.enabled`: enable/disable spy type.
- `spy.<type>.message`: message format.
- `filters.excluded_commands`: commands ignored by command spy (case-insensitive).
- `filters.excluded_players`: players ignored by all spies (case-insensitive).
- `filters.excluded_worlds`: worlds ignored by all spies (case-sensitive).
- `messages`: plugin command/status/help messages. Includes `state_enabled` / `state_disabled` (used as `{state}`), and the `help_*` keys that drive `/bigbrother help`.

Default spy keys: `command`, `sign`, `anvil`, `book`, `portal`.

## Dependencies

BigBrother bundles **Adventure** (`adventure-platform-bukkit`) inside its JAR, relocated to `com.chalwk.libs.adventure` to avoid conflicts with server-provided or other-plugin copies. You do not need to install anything extra.

## Changelog

### 1.0.3

**Fixed**

- Reload no longer rewrites `config.yml`. Previously the plugin called `YamlConfiguration#save` on every reload, which silently stripped every comment and reformatted the file. Comments and formatting are now preserved across reloads.

**Changed**

- Adventure (`adventure-platform-bukkit`) is now shaded into the plugin JAR and relocated to `com.chalwk.libs.adventure`. BigBrother runs on Spigot, Paper, and Purpur from a single JAR without relying on a server-provided Adventure library.
- Supported server list widened to include Spigot.

**Internal**

- `MessageHelper` is now an instance class wrapping `BukkitAudiences`, created in `onEnable` and closed in `onDisable`.
- `BigBrotherCommand` and `SpyListener` now receive the `MessageHelper` instance via constructor rather than calling a static method.
- Command argument parsing now uses `Locale.ROOT` for case-insensitive comparison, matching the collection convention.
- `ConfigManager` guards against a missing embedded `config.yml` and no longer assumes `configFile` is set before `reloadConfig` runs.

### 1.0.2

- Book spy now uses the non-deprecated `BookMeta#page(int)` API. No behavioural change, but the plugin no longer emits a deprecation warning at build time and is compatible with future Paper releases that remove `getPages()`.

### 1.0.0

- Internal development builds.