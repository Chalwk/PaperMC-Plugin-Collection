---
layout: plugin
title: AutoMessages
description: Scheduled broadcasts to all players and console, with legacy colors, JSON click/hover components, and configurable interval.
category: Administration
plugin_id: automessages
latest_version: "1.0.0"
version: 1.0.0
author: Chalwk
website: https://github.com/Chalwk
api-version: 1.21
main: com.chalwk.AutoMessages
source_path: papermc_plugins/automessages
minecraft_versions: "1.21+"
server_software: "Paper, Purpur, Spigot"
java_version: "21+"
tags:
  - broadcast
  - announcements
  - scheduler
features:
  - "Periodic broadcasts on a fixed interval"
  - "Legacy `&` color codes and JSON text components"
  - "Interactive click and hover events via JSON lines"
  - "Multi-line broadcasts, cycled in order"
  - "Reload restarts the scheduler and resets the index"
commands:
  - name: automessages
    description: Manage AutoMessages plugin
    usage: /automessages [help|status|reload]
    permission: automessages.use
    aliases: [am]
permissions:
  - name: automessages.*
    description: All AutoMessages permissions
    children:
      - automessages.use
      - automessages.reload
  - name: automessages.use
    description: Allows using /automessages
    default: op
  - name: automessages.reload
    description: Allows reloading configuration
    default: op
---

# AutoMessages

AutoMessages periodically broadcasts configured messages to all online players and the console.

## Features

- Broadcasts configured messages at a fixed interval.
- Sends to all online players and the console.
- Supports multi-line broadcasts.
- Supports legacy `&` color codes.
- Supports JSON text components for interactive click/hover events. A line is treated as JSON when it starts with `{` and ends with `}`.
- Reload restarts the scheduler and resets the broadcast index.
- Status shows interval, total broadcasts, and next broadcast index.

## Commands

| Command                 | Description                         | Permission                                 |
| ----------------------- | ----------------------------------- | ------------------------------------------ |
| `/automessages` / `/am` | Show help                           | `automessages.use`                         |
| `/automessages help`    | Show help                           | `automessages.use`                         |
| `/automessages status`  | Show scheduler status               | `automessages.use`                         |
| `/automessages reload`  | Reload config and restart scheduler | `automessages.reload` + `automessages.use` |

## Permissions

| Permission            | Description                    | Default |
| --------------------- | ------------------------------ | ------- |
| `automessages.*`      | All AutoMessages permissions   | op      |
| `automessages.use`    | Allows using /automessages     | op      |
| `automessages.reload` | Allows reloading configuration | op      |

## Configuration

### Top-level

- `config-version`: schema version for migration.
- `interval`: seconds between broadcasts. Default: `600`.

### Broadcasts

`broadcasts` is a numbered list of message entries. Each entry is a list of lines.

Example:

```yaml
broadcasts:
  1:
    - '&eHello!'
    - '{"text":"Click here","clickEvent":{"action":"open_url","value":"https://example.com"}}'
```

### Messages

Includes:

- `no_permission`
- `reloaded`
- `unknown_command`
- `status_header`
- `status_interval`
- `status_total`
- `status_next`

Status placeholders:

- `{interval}`
- `{total}`
- `{next}`

## Notes

- Scheduler interval is `interval * 20` ticks, with a minimum of `20` ticks (1 second).
- Reload resets the broadcast index to `0`.
- Broadcasts cycle in order.