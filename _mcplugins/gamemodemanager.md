---
layout: plugin
title: GameModeManager
description: Separate inventories and player state per gamemode, with world-change handling to preserve gamemode.
category: Gameplay
plugin_id: gamemodemanager
latest_version: "1.0.0"
author: Chalwk
website: https://github.com/Chalwk
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
```

## Notes

- Only `CREATIVE` and `SURVIVAL` are tracked. Other gamemodes are ignored by the inventory/state manager.
- Pending world-switch gamemodes are stored in memory and cleared on quit.
- Player data is stored per UUID under `playerdata/`.