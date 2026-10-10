---
layout: plugin
title: PerkMenu
description: Paginated chat-based perk browser that reads ownership from permissions, with prices, descriptions, and buy links.
category: Gameplay
plugin_id: perkmenu
latest_version: "1.0.0"
author: Chalwk
website: https://github.com/Chalwk/PaperMC-Plugin-Collection
api-version: 1.21
main: com.chalwk.PerkMenu
source_path: papermc_plugins/perkmenu
minecraft_versions: "1.21+"
server_software: "Paper, Purpur, Spigot"
java_version: "21+"
tags:
  - perks
  - store
  - utility
features:
  - "Paginated chat menu, no GUI"
  - "Ownership is read from permission nodes"
  - "Click-to-buy opens the configured store URL"
  - "Hover a perk to see its description"
  - "Player sees their own perks and total listed value"
  - "Everything configurable in `config.yml`"
  - "Runs on Spigot, Paper, and Purpur"
commands:
  - name: perks
    description: Browse and inspect server perks
    usage: /perks [page|owned|available|info|check|reload|help]
    permission: perkmenu.use
    aliases: [perk]
permissions:
  - name: perkmenu.*
    description: All PerkMenu permissions
    children:
      - perkmenu.use
      - perkmenu.check
      - perkmenu.reload
  - name: perkmenu.use
    description: Allows using /perks
    default: true
  - name: perkmenu.check
    description: Allows checking another player's perks
    default: op
  - name: perkmenu.reload
    description: Allows reloading the configuration
    default: op
---

# PerkMenu

PerkMenu is a paginated chat-based perk browser. Players type `/perks` and
see every perk on the server, along with which ones they own, what they
cost, and a click-to-buy link for anything they don't have yet.

Ownership is determined entirely by permissions. A perk is "owned" when the
player holds every permission node the perk declares. There is no store API,
no economy plugin, no database. If the permissions are there, the perk is
there.

---

## Features

- Paginated chat menu. No inventory GUI, no chest, no resource pack.
- Ownership derived from permissions configured per perk.
- Click an unowned perk to open its store URL in the browser.
- Click an owned perk to see detailed info.
- Hover any perk to see its description.
- Summary line shows owned count, total count, and total listed value.
- Admin command to inspect another player's perks.
- Everything configurable in `config.yml`.

---

## Commands

| Command                   | Description                         | Permission                         |
| ------------------------- | ----------------------------------- | ---------------------------------- |
| `/perks` / `/perk`        | Browse all perks, page 1            | `perkmenu.use`                     |
| `/perks <page>`           | Jump to a specific page             | `perkmenu.use`                     |
| `/perks owned [page]`     | Only perks you own                  | `perkmenu.use`                     |
| `/perks available [page]` | Only perks you don't own            | `perkmenu.use`                     |
| `/perks info <perk>`      | Detailed info about one perk        | `perkmenu.use`                     |
| `/perks check <player>`   | Check another player's perk summary | `perkmenu.use` + `perkmenu.check`  |
| `/perks reload`           | Reload configuration                | `perkmenu.use` + `perkmenu.reload` |
| `/perks help`             | Show help                           | `perkmenu.use`                     |

---

## Permissions

| Permission        | Description                            | Default |
| ----------------- | -------------------------------------- | ------- |
| `perkmenu.*`      | All PerkMenu permissions               | op      |
| `perkmenu.use`    | Allows using `/perks`                  | true    |
| `perkmenu.check`  | Allows checking another player's perks | op      |
| `perkmenu.reload` | Allows reloading the configuration     | op      |

---

## Configuration

### Top-level

- `config-version`: schema version for migration.
- `perks_per_page`: how many perks to show per page. Default: `6`.
- `buy_url_template`: URL template used for buy links. `{package_id}` is
  replaced with the perk's own `package_id` field.
- `show_owned_in_list`: whether the default `/perks` listing includes perks
  the player already owns. Default: `true`.

### Perks

Each entry under `perks:` is one perk. The key is the perk's ID (used by
`/perks info <id>` and in tab completion). Each perk supports:

- `name`: display name.
- `category`: free-form grouping shown in `/perks info`.
- `price`: listed price in your store's currency.
- `package_id`: used to build the buy URL.
- `description`: list of lines shown on hover and in `/perks info`.
- `permissions`: list of permission nodes. A player owns this perk when
  they hold **every** node in this list.

---

## Notes

- Ownership is a permissions check. If a player buys a perk but the
  permissions are delayed on your permissions plugin's side, they won't see
  the perk as owned until those nodes land.
- Wildcards like `cmi.colors.*` are resolved by your permissions plugin,
  not by PerkMenu. If your setup doesn't expand wildcards, list the exact
  nodes instead.
- "Total listed value" is the sum of the listed prices of every perk the
  player owns. It is **not** necessarily what they paid, and it does not
  reflect sales, gifts, or price changes over time.
- Reload does not rewrite `config.yml`. Comments and formatting are
  preserved.

---

## Dependencies

Bundles **Adventure** (`adventure-platform-bukkit`) inside its JAR,
relocated to `com.chalwk.libs.adventure`. No external dependencies.

---

## Changelog

### 1.0.0

- Initial release.