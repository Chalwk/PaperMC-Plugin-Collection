---
layout: plugin
title: VacuLoot
description: Toggleable item magnet with tiered ranges, optional Vault economy cost, world restrictions, and item blacklist.
category: Gameplay
plugin_id: vaculoot
latest_version: "1.0.0"
version: 1.0.0
author: Chalwk
website: https://github.com/Chalwk
api-version: 1.21
main: com.chalwk.VacuLoot
source_path: papermc_plugins/vaculoot
minecraft_versions: "1.21+"
server_software: "Paper, Purpur, Spigot"
java_version: "21+"
tags:
  - magnet
  - items
  - economy
features:
  - "Personal item and XP magnet with `/magnet`"
  - "Tiered ranges: basic, advanced, ultimate, god"
  - "Optional Vault economy cost per toggle"
  - "World allow-list and item material blacklist"
  - "Toggle cooldown and admin tier management"
commands:
  - name: magnet
    description: Toggle item magnet
    usage: /magnet [player|toggle|reload|check|tier|help]
    permission: magnet.use
    aliases: [ml, mag]
permissions:
  - name: magnet.*
    description: All VacuLoot permissions
    children:
      - magnet.use
      - magnet.use.others
      - magnet.admin
      - magnet.tier.basic
      - magnet.tier.advanced
      - magnet.tier.ultimate
      - magnet.tier.god
  - name: magnet.use
    description: Allows using the magnet command
    default: op
  - name: magnet.use.others
    description: Allows toggling magnet for other players
    default: op
  - name: magnet.admin
    description: Access to admin management commands
    default: op
  - name: magnet.tier.basic
    description: Access to basic magnet tier
    default: true
  - name: magnet.tier.advanced
    description: Access to advanced magnet tier
    default: op
  - name: magnet.tier.ultimate
    description: Access to ultimate magnet tier
    default: op
  - name: magnet.tier.god
    description: Access to god magnet tier
    default: op
---

# VacuLoot

Toggleable item magnet with tiered ranges, optional Vault economy cost, world restrictions, and item blacklist.

## Features

- Toggle a personal item magnet with `/magnet`.
- Attracts nearby dropped items and experience orbs.
- Tiered magnet ranges and speed multipliers: `basic`, `advanced`, `ultimate`, `god`.
- Optional Vault economy cost per toggle.
- Toggle cooldown.
- World allow-list and item material blacklist.
- Admin commands to check status, set tiers, reload config, and toggle for others.
- In-memory magnet states, tiers, and cooldowns.

## Commands

All `/magnet` commands require `magnet.use` because the Bukkit command permission is `magnet.use`.

| Command                        | Description                      | Permission                                              |
| ------------------------------ | -------------------------------- | ------------------------------------------------------- |
| `/magnet` / `/ml` / `/mag`     | Toggle your magnet               | `magnet.use`                                            |
| `/magnet help`                 | Show help                        | `magnet.use`                                            |
| `/magnet toggle`               | Toggle your magnet               | `magnet.use`                                            |
| `/magnet toggle <player>`      | Toggle magnet for another player | `magnet.use` + `magnet.use.others`                      |
| `/magnet check [player]`       | Check magnet status              | `magnet.use`; other player requires `magnet.use.others` |
| `/magnet <player>`             | Toggle magnet for another player | `magnet.use` + `magnet.use.others`                      |
| `/magnet tier <player> <tier>` | Set a player's magnet tier       | `magnet.use` + `magnet.admin`                           |
| `/magnet reload`               | Reload configuration             | `magnet.use` + `magnet.admin`                           |

## Permissions

| Permission             | Description                              | Default |
| ---------------------- | ---------------------------------------- | ------- |
| `magnet.*`             | All VacuLoot permissions                 | op      |
| `magnet.use`           | Allows using the magnet command          | op      |
| `magnet.use.others`    | Allows toggling magnet for other players | op      |
| `magnet.admin`         | Access to admin management commands      | op      |
| `magnet.tier.basic`    | Access to basic magnet tier              | true    |
| `magnet.tier.advanced` | Access to advanced magnet tier           | op      |
| `magnet.tier.ultimate` | Access to ultimate magnet tier           | op      |
| `magnet.tier.god`      | Access to god magnet tier                | op      |

## Configuration

```yaml
config-version: 1

default_tier: "basic"

magnet:
  interval: 5

toggle:
  cooldown: 5

attraction:
  items: true
  experience: true
  speed: 0.3

economy:
  enabled: true
  toggle_cost: 10.0

worlds:
  allowed:
    - "world"
    - "world_nether"
    - "world_the_end"

blacklist:
  materials:
    - "BEDROCK"
    - "BARRIER"
    - "COMMAND_BLOCK"

tiers:
  basic:
    range: 5.0
    speed_multiplier: 1.0
    permission: "magnet.tier.basic"
  advanced:
    range: 10.0
    speed_multiplier: 1.2
    permission: "magnet.tier.advanced"
  ultimate:
    range: 15.0
    speed_multiplier: 1.5
    permission: "magnet.tier.ultimate"
  god:
    range: 25.0
    speed_multiplier: 2.0
    permission: "magnet.tier.god"
```

Messages are configurable under `messages`:

`enabled`, `disabled`, `cooldown`, `insufficient_funds`, `toggled_for`, `toggled_by`, `status`, `reloaded`, `player_not_found`, `invalid_tier`, `tier_set`, `tier_changed`, `no_permission`.

## Notes

- Requires Vault for economy features (`softdepend: [Vault]`).
- If Vault/economy is unavailable, economy checks are bypassed.
- Magnet states, tiers, and cooldowns are stored in memory and are not persisted across restarts.
- The magnet task runs every `magnet.interval` ticks and only processes players with an active magnet.
- Items with metadata `no-magnet` and materials in `blacklist.materials` are ignored.
- If `worlds.allowed` is empty, all worlds are allowed.