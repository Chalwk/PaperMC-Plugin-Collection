---
layout: plugin
title: GameModeManager
description: Separate inventories and player state per gamemode, with world-change handling to preserve gamemode.
category: Gameplay
plugin_id: gamemodemanager
latest_version: "1.0.2"
author: Chalwk
website: https://github.com/Chalwk/PaperMC-Plugin-Collection
api-version: 1.21
main: com.chalwk.GameModeManager
source_path: papermc_plugins/gamemodemanager
minecraft_versions: "1.21+"
server_software: "Paper, Purpur, Spigot"
java_version: "21+"
tags:
  - gamemode
  - inventory
  - survival
features:
  - "Separate inventories and state for CREATIVE and SURVIVAL"
  - "Inventory, health, food, XP, and potion effects tracked"
  - "Per-UUID persistence under playerdata/"
  - "World-switch gamemode preservation"
  - "Saves all online players on plugin disable"
commands:
  - name: gmmanage
    description: Manage GameModeManager settings
    usage: /gmmanage [reload|help]
    permission: gmmanage.use
    aliases: [gmm]
permissions:
  - name: gmmanage.*
    description: All GameModeManager permissions
    children:
      - gmmanage.use
      - gmmanage.reload
  - name: gmmanage.use
    description: Allows using the /gmmanage command
    default: op
  - name: gmmanage.reload
    description: Allows reloading the configuration
    default: op
---

# GameModeManager

Manages separate inventories and player states per gamemode, with world-change handling to preserve gamemode.

## Features

- Tracks separate inventories for `CREATIVE` and `SURVIVAL` only.
- Stores inventory contents (41 slots), health, food, saturation, total experience, level, exp, and active potion effects.
- On gamemode change, captures the current state and applies the stored state for the new gamemode.
- On world change, captures the current gamemode before teleport/portal and restores it after the world change.
- Saves player data to `playerdata/<uuid>.yml`.
- Loads data on join, saves on quit, and saves all online players on plugin disable.
- Reload command for `config.yml`.

## Commands

| Command              | Description          | Permission                         |
| -------------------- | -------------------- | ---------------------------------- |
| `/gmmanage` / `/gmm` | Show help            | `gmmanage.use`                     |
| `/gmmanage help`     | Show help            | `gmmanage.use`                     |
| `/gmmanage reload`   | Reload configuration | `gmmanage.use` + `gmmanage.reload` |

## Permissions

| Permission        | Description                        | Default |
| ----------------- | ---------------------------------- | ------- |
| `gmmanage.*`      | All GameModeManager permissions    | op      |
| `gmmanage.use`    | Allows using the /gmmanage command | op      |
| `gmmanage.reload` | Allows reloading the configuration | op      |

## Configuration

```yaml
config-version: 1
messages:
  no_permission: "&cYou don't have permission to use this feature!"
  reloaded: "&aConfiguration reloaded!"
  unknown_subcommand: "&cUnknown subcommand. Use &6/gmmanage help"
  help:
    - "&6/gmmanage reload &7- Reload the configuration"
    - "&6/gmmanage help &7- Show this help"
```

## Notes

- Only `CREATIVE` and `SURVIVAL` are tracked. Other gamemodes are ignored by the inventory/state manager.
- Pending world-switch gamemodes are stored in memory and cleared on quit.
- Player data is stored per UUID under `playerdata/`.

---

## Dependencies

Bundles **Adventure** (`adventure-platform-bukkit`) inside its JAR,
relocated to `com.chalwk.libs.adventure`. No external dependencies.

---

## Changelog

### 1.0.2

**Changed**

- Adventure (`adventure-platform-bukkit`) is now shaded into the plugin JAR and relocated to `com.chalwk.libs.adventure`. Runs on Spigot, Paper, and Purpur.

**Fixed**

- Reload no longer rewrites `config.yml`. Comments and formatting are preserved across reloads.

**Internal**

- `MessageHelper` is now an instance class wrapping `BukkitAudiences`, created in `onEnable` and closed in `onDisable`.
- `GameModeCommand` now receives the `MessageHelper` instance via constructor.

### 1.0.1

**Fixed**

- `PlayerQuitEvent` now clears pending gamemode state via `clearPendingGameMode(UUID)`, preventing a stale entry from leaking between sessions if a player disconnects mid-world-switch.
- `GameModeListener#onGameModeChange` now checks `event.isCancelled()` before swapping inventories. Previously a cancelled gamemode change (e.g. by a protection plugin) would still trigger an inventory swap and desync state.
- `WorldSwitchListener` no longer registers a separate `PlayerPortalEvent` handler. `PlayerPortalEvent` extends `PlayerTeleportEvent`, so the portal path was already covered by the teleport handler - the duplicate listener was dead code that could double-fire on portal traversal.

**Changed**

- Help and unknown-subcommand output moved out of Java source and into `config.yml`. `/gmmanage help` now reads from a new `messages.help` list, and the unknown-subcommand message is `messages.unknown_subcommand`. Both are exposed through `PluginConfig` and can be edited without rebuilding the plugin.
- Tab completion for `/gmmanage reload` now respects the `gmmanage.reload` permission, matching the command's own permission check. Previously the subcommand was suggested to every player with `gmmanage.use`.
- Case-insensitive comparison in tab completion uses `Locale.ROOT`.
- Startup, shutdown, and reload log lines no longer use exclamation marks and no longer advertise the plugin's purpose on every enable (the description lives in `plugin.yml`).

**Internal**

- `getCommand("gmmanage")` is null-checked in `onEnable`. If the command is missing from `plugin.yml`, the plugin logs a clear error and disables itself instead of throwing an NPE at startup.
- `PluginConfig#getHelpLines()` returns an unmodifiable list, so callers cannot mutate the config-backed help output.
- `ConfigManager` and `PluginConfig` now carry class-level Javadoc.

### 1.0.0

- Initial release.