---
layout: plugin
title: AutoMessages
description: Scheduled broadcasts to all players and console, with legacy colors, JSON click/hover components, and configurable interval.
category: Administration
plugin_id: automessages
latest_version: "1.0.1"
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
  - "Reload preserves your config comments and formatting"
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
- Reload preserves comments and formatting in `config.yml` - the plugin never rewrites your file.
- Status shows interval, total broadcasts, and next broadcast index.
- All user-facing strings (help text, status labels, error messages) are defined in `config.yml` under `messages:`, so nothing is hardcoded.

## Commands

| Command                 | Description                         | Permission                                 |
| ----------------------- | ----------------------------------- | ------------------------------------------ |
| `/automessages` / `/am` | Show help                           | `automessages.use`                         |
| `/automessages help`    | Show help                           | `automessages.use`                         |
| `/automessages status`  | Show scheduler status               | `automessages.use`                         |
| `/automessages reload`  | Reload config and restart scheduler | `automessages.reload` + `automessages.use` |

Tab completion covers `help`, `status`, and (if you have permission) `reload`.

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

`broadcasts` is a numbered map of message entries. Each entry is a list of lines. Keys are sorted numerically before being broadcast, so gaps (e.g. skipping `8`) are fine.

Example:

```yaml
broadcasts:
  1:
    - '&eHello!'
    - '{"text":"Click here","clickEvent":{"action":"open_url","value":"https://example.com"}}'
```

### Messages

All user-visible strings live under `messages:`. Every entry supports legacy `&` color codes.

- `no_permission` - shown when a sender lacks the required permission.
- `reloaded` - shown after a successful `/automessages reload`.
- `unknown_command` - shown for an unrecognised subcommand.
- `status_header` - header line printed by `/automessages status`.
- `status_interval` - supports the `{interval}` placeholder.
- `status_total` - supports the `{total}` placeholder.
- `status_next` - supports the `{next}` and `{total}` placeholders.
- `help` - a list of lines printed by `/automessages help` (or `/automessages` with no args).

Status placeholders:

- `{interval}` - configured interval in seconds.
- `{total}` - number of configured broadcasts.
- `{next}` - 1-based index of the next broadcast, or `0` if there are no broadcasts.

Example help block:

```yaml
messages:
  help:
    - "&6&lAutoMessages Help"
    - "&e/automessages status &7- Show current status"
    - "&e/automessages reload &7- Reload configuration and restart scheduler"
    - "&e/automessages help &7- Show this help"
```

## Notes

- Scheduler interval is `interval * 20` ticks, with a minimum of `20` ticks (1 second).
- Reload resets the broadcast index to `0`.
- Broadcasts cycle in order.
- Reload does **not** rewrite `config.yml`. New keys added in a future plugin release will be applied in memory from the bundled defaults, but will not appear on disk until you add them yourself. This is deliberate: rewriting the file would strip every comment from your config.

## Changelog

### 1.0.1

**Fixed**

- Reload no longer rewrites `config.yml`. Previously the plugin called `YamlConfiguration#save` on every reload, which silently stripped every comment and reformatted the file. Comments and formatting are now preserved across reloads.

**Changed**

- Help text moved out of the plugin and into `config.yml` under a new `messages.help` list. Server owners can now customise the help output without rebuilding the plugin.
- Tab completion is case-insensitive using `Locale.ROOT`.
- `PluginConfig` now returns an unmodifiable view of the broadcasts list, and each inner line list is defensively copied at load time, so callers cannot mutate plugin state.
- `MessageHelper` and `MessageParser` are now `final` utility classes with private constructors.

**Internal**

- `getCommand("automessages")` is null-checked in `onEnable`. If the command is missing from `plugin.yml`, the plugin logs a clear error and disables itself instead of throwing an NPE at startup.
- `ConfigManager` guards against a missing embedded `config.yml` and no longer assumes `configFile` is set before `reloadConfig` runs.
- Renamed `PluginConfig#getMessages()` to `PluginConfig#getBroadcasts()`. Field names updated to match.
- `MessageParser` catches JSON deserialization failures per-line and falls back to legacy formatting, so one malformed JSON line does not break an entire broadcast.

### 1.0.0

- Initial release.