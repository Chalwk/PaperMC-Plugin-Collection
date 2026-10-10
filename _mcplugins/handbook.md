---
layout: plugin
title: Handbook
description: Gives every player a written book guide to the server, with chapters configurable in a separate handbook.yml.
category: Gameplay
plugin_id: handbook
latest_version: "1.0.0"
author: Chalwk
website: https://github.com/Chalwk/PaperMC-Plugin-Collection
api-version: 1.21
main: com.chalwk.Handbook
source_path: papermc_plugins/handbook
minecraft_versions: "1.21+"
server_software: "Paper, Purpur, Spigot"
java_version: "21+"
tags:
  - utility
  - onboarding
  - guide
features:
  - "Written book given on first join"
  - "Chapters configured in a separate handbook.yml"
  - "Auto-paginated with configurable lines per page"
  - "Manual page breaks via '---'"
  - "Chapter title rendered at the top of its first page"
  - "Re-issue the book at any time with /handbook"
  - "Give to other players with /handbook give"
  - "Does not rewrite your config files on reload"
  - "Runs on Spigot, Paper, and Purpur"
commands:
  - name: handbook
    description: Get a copy of the server handbook
    usage: /handbook [give|reload|help] [player]
    permission: handbook.use
    aliases: [book]
permissions:
  - name: handbook.*
    description: All Handbook permissions
    children:
      - handbook.use
      - handbook.give
      - handbook.reload
  - name: handbook.use
    description: Allows using /handbook
    default: true
  - name: handbook.give
    description: Allows giving the handbook to other players
    default: op
  - name: handbook.reload
    description: Allows reloading the handbook configuration
    default: op
---

# Handbook

Handbook gives every new player a written book guide to your server. The
content is entirely configurable, with chapter titles, colours, and page
breaks all driven from a separate `handbook.yml`.

The idea is simple: a player joins for the first time, and their inventory
already contains a book called *Compendium of the Realm*. They open it,
flip through a dozen pages, and know exactly what the server is about,
where to find things, and what the rules are. No chat scrolling, no
external links to click, no wiki to remember.

---

## Features

- Written book delivered automatically on first join.
- Chapters configured in a separate `handbook.yml` file.
- Automatic pagination with a configurable lines-per-page budget.
- Manual page breaks via a line consisting of `---`.
- Chapter titles rendered at the top of each chapter's first page.
- Colour codes in config, so headings and accents render in-game.
- Re-issue the book at any time with `/handbook`.
- Give the book to another player with `/handbook give <player>`.
- Neither `config.yml` nor `handbook.yml` is rewritten on reload.
- Runs on Spigot, Paper, and Purpur from a single JAR.

---

## Commands

| Command                   | Description                           | Permission                         |
| ------------------------- | ------------------------------------- | ---------------------------------- |
| `/handbook` / `/book`     | Get a copy of the handbook            | `handbook.use`                     |
| `/handbook give <player>` | Give the handbook to another player   | `handbook.use` + `handbook.give`   |
| `/handbook reload`        | Reload the configuration and handbook | `handbook.use` + `handbook.reload` |
| `/handbook help`          | Show help                             | `handbook.use`                     |

---

## Permissions

| Permission        | Description                          | Default |
| ----------------- | ------------------------------------ | ------- |
| `handbook.*`      | All Handbook permissions             | op      |
| `handbook.use`    | Allows using `/handbook`             | true    |
| `handbook.give`   | Allows giving the handbook to others | op      |
| `handbook.reload` | Allows reloading the configuration   | op      |

---

## Configuration

### `config.yml`

- `config-version`: schema version for migration.
- `give_on_first_join`: whether the handbook is given automatically.
- `only_first_join`: when true, only players who have never joined before
  receive the book. When false, anyone who hasn't received one yet gets it
  on their next join. Useful after editing the handbook to push the
  updated version to everyone.
- `drop_when_inventory_full`: drop the book at the player's feet if their
  inventory is full. When false, the give is silently skipped.
- `lines_per_page`: raw content lines per book page before a new page
  starts. Default: `9`. Minecraft renders at most 14 lines per page; our
  chapter header takes 4 of those, and wrapped content lines take more.
  Valid range is 1 to 14.
- `max_pages`: hard cap on total pages in the book. Default: `100`, which
  matches Minecraft's own limit. Content beyond this is silently dropped.

Messages are all under `messages:`.

### `handbook.yml`

The book itself. Two sections:

```yaml
book:
  title: "Compendium of the Realm"
  author: "JeriCraft"

chapters:
  - id: welcome
    title: "Welcome"
    content:
      - "&7Welcome to the server."
```

Each chapter supports:

- `id`: internal identifier (not shown in-game).
- `title`: rendered at the top of the chapter's first page.
- `content`: list of lines. Empty strings render blank lines. A line
  consisting of exactly `---` forces a new page.

---

## Writing content that fits

Minecraft book pages have hard rendering limits, and content that ignores
them spills awkwardly onto the next page. Three numbers to keep in mind:

- **14 rendered lines per page.** Anything beyond this is pushed to the
  next page. Our chapter header occupies 4 of those lines (blank, title,
  divider, blank), leaving 10 for content.
- **114 pixels of width per line.** A line that exceeds this wraps to two
  rendered lines, stealing one of the ten remaining slots.
- **22 characters per line (roughly).** At the default Minecraft font,
  this is what fits before wrapping. Colour codes count toward this. A
  line like `&0Welcome to JeriCraft.` is not 21 characters, it is 28.

Given those constraints, the default `lines_per_page: 9` is deliberately
conservative. It leaves a spare line for a wrap or two per page. If your
content is very tight (short lines, no wraps), you can raise it toward 14.
If it wraps heavily, lower it.

### Colour codes and the book page

Book pages have a pale cream background. Light colours are unreadable.
Use these:

- **Body text**: `&0` (black) or `&8` (dark grey).
- **Headings**: `&0&l`, `&6&l`, or `&4&l`.
- **Commands and links**: `&1` (dark blue) or `&9` (blue).
- **Emphasis**: `&4` (dark red) or `&5` (dark purple).

Avoid `&f`, `&7`, `&a`, `&b`, `&c`, `&d`, `&e` entirely. They either
disappear against the background or are very hard to read.

### Explicit page breaks

Where the automatic split isn't clean, force one with a `---` line:

```yaml
content:
  - "&0First part of chapter."
  - "---"
  - "&0Second part, on its own page."
```

The `---` line itself renders nothing; it just tells the plugin to close
the current page and start a new one.

---

## Notes

- Book title and author are truncated to 32 characters, which is
  Minecraft's own limit for written books.
- Total pages are capped at `max_pages`. Content beyond this is dropped
  silently. Very unlikely in practice; the default handbook uses far
  fewer pages than the 100-page limit.
- Neither config file is written to by the plugin. Reload preserves
  comments and formatting.

---

## Dependencies

Bundles **Adventure** (`adventure-platform-bukkit`) inside its JAR,
relocated to `com.chalwk.libs.adventure`. No external dependencies.

---

## Changelog

### 1.0.0

- Initial release.