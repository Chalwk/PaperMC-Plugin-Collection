---
layout: plugin
title: NoctiView
description: Per-world night vision toggle with configurable particles, sound feedback, and admin world controls.
category: Gameplay
plugin_id: noctiview
latest_version: "1.0.0"
author: Chalwk
website: https://github.com/Chalwk
api-version: 1.21
main: com.chalwk.NoctiView
source_path: papermc_plugins/noctiview
minecraft_versions: "1.21+"
server_software: "Paper, Purpur, Spigot"
java_version: "21+"
tags:
  - night-vision
  - utility
  - world
features:
  - "Toggle your own night vision with `/nv`"
  - "Per-world enable/disable controls"
  - "Configurable particles and sound on toggle"
  - "Admin subcommands to list, enable, and disable worlds"
  - "Toggle for other players with `noctiview.admin`"
commands:
  - name: nightvision
    description: Toggle night vision or use admin commands
    usage: /nightvision [player|reload|worlds|enableworld|disableworld]
    permission: nightvision.use
    aliases: [nv]
permissions:
  - name: noctiview.*
    description: All NoctiView permissions
    children:
      - nightvision.use
      - noctiview.admin
  - name: nightvision.use
    description: Allows toggling your own night vision
    default: true
  - name: noctiview.admin
    description: Access to admin commands and toggling for others
    default: op
---

# NoctiView

Simple night vision toggle with per-world enable/disable and optional particle/sound feedback.

## Features

- Toggle night vision for yourself with `/nightvision` or `/nv`.
- Per-world enable/disable controls.
- Admin commands to list, enable, or disable worlds.
- Admin command to toggle night vision for another player.
- Configurable potion duration/amplifier, particles, and sound.
- Removes night vision from all online players when the plugin disables.

## Commands

All commands require `nightvision.use` at the Bukkit command level. Admin subcommands additionally require `noctiview.admin`.

| Command                             | Description                            | Permission                            |
| ----------------------------------- | -------------------------------------- | ------------------------------------- |
| `/nightvision` / `/nv`              | Toggle your night vision               | `nightvision.use`                     |
| `/nightvision help`                 | Show help                              | `nightvision.use`                     |
| `/nightvision <player>`             | Toggle night vision for another player | `nightvision.use` + `noctiview.admin` |
| `/nightvision worlds`               | List per-world settings                | `nightvision.use` + `noctiview.admin` |
| `/nightvision enableworld <world>`  | Enable night vision in a world         | `nightvision.use` + `noctiview.admin` |
| `/nightvision disableworld <world>` | Disable night vision in a world        | `nightvision.use` + `noctiview.admin` |
| `/nightvision reload`               | Reload configuration                   | `nightvision.use` + `noctiview.admin` |

## Permissions

| Permission        | Description                                      | Default |
| ----------------- | ------------------------------------------------ | ------- |
| `noctiview.*`     | All NoctiView permissions                        | op      |
| `nightvision.use` | Allows toggling your own night vision            | true    |
| `noctiview.admin` | Access to admin commands and toggling for others | op      |

## Configuration

```yaml
config-version: 1

particle:
  enabled: true
  type: GLOW
  count: 10
  offset_x: 0.5
  offset_y: 1.0
  offset_z: 0.5
  speed: 0.1

sound:
  enabled: true
  type: ENTITY_EXPERIENCE_ORB_PICKUP
  volume: 1.0
  pitch: 1.0

effect:
  duration: 999999
  amplifier: 0

worlds:
  world: true
  world_nether: false
  world_the_end: false
```

Messages are configurable under `messages`:

`no_permission`, `players_only`, `reloaded`, `player_not_found`, `world_not_found`, `world_enabled`, `world_disabled`, `world_header`, `world_not_enabled_self`, `world_not_enabled_other`, `night_vision_enabled`, `night_vision_disabled`, `night_vision_enabled_for`, `night_vision_disabled_for`, `night_vision_enabled_by`, `night_vision_disabled_by`, `usage_enableworld`, `usage_disableworld`.

## Notes

- Night vision toggles only work in worlds marked `true` in `worlds`.
- Active toggles are tracked in memory and are not persisted across server restarts or player reconnects.
- Admin world changes are saved back to `config.yml` via `setWorldEnabled`.
- The night vision effect is applied with ambient `true` and particles `false`.