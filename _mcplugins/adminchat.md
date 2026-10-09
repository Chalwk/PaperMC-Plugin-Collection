---
layout: plugin
title: AdminChat
description: Multi-channel staff chat with per-channel permissions, formatting, sounds, cooldowns, and visibility toggles.
category: Administration
version: 1.0.0
author: Chalwk
website: https://github.com/Chalwk
api-version: 1.21
main: com.chalwk.AdminChat
source_path: papermc_plugins/adminchat
release_url: https://github.com/Chalwk/PaperMC-Plugin-Collection/releases/latest
minecraft_versions: "1.21+"
server_software: "Paper, Purpur, Spigot"
java_version: "21+"
tags:
  - chat
  - staff
  - channels
features:
  - "Multiple configurable channels (mod, admin, trial by default)"
  - "Per-channel permission, format, prefix, and sound"
  - "Channel toggling with `/achat <channel>` and `/achat off`"
  - "Visibility toggle with `/achat toggle [player]`"
  - "Join/quit notifications for staff"
commands:
  - name: achat
    description: Send messages in admin chat channels
    usage: /achat [help|off|toggle|reload] or /achat [channel] [message]
    permission: adminchat.use
    aliases: [ac]
  - name: adminchat
    description: Admin chat management commands
    usage: /adminchat [help|reload|toggle] [player]
    permission: adminchat.admin
permissions:
  - name: adminchat.*
    description: All AdminChat permissions
    children:
      - adminchat.use
      - adminchat.admin
      - adminchat.channel.mod
      - adminchat.channel.admin
      - adminchat.channel.trial
  - name: adminchat.use
    description: Allows using admin chat
    default: op
  - name: adminchat.admin
    description: Access to admin management commands
    default: op
  - name: adminchat.channel.mod
    description: Access to mod channel
    default: op
  - name: adminchat.channel.admin
    description: Access to admin channel
    default: op
  - name: adminchat.channel.trial
    description: Access to trial channel
    default: op
---

# AdminChat

AdminChat provides a secure, permission-based chat system for staff with multiple channels, per-channel formatting, sounds, cooldowns, and visibility toggles.

## Features

- Multiple configurable channels: `mod`, `admin`, `trial` by default.
- Per-channel permission, format, prefix, sound type, volume, and pitch.
- Default channel and global cooldown.
- Channel toggling: `/achat <channel>` enters/exits a channel, then `/achat <message>` sends to it.
- `/achat off` exits the toggled channel.
- `/achat toggle [player]` toggles admin-chat visibility.
- Join/quit notifications for staff with `adminchat.use`.
- Reload command for configuration.

## Commands

| Command                      | Description                                  | Permission                                                      |
| ---------------------------- | -------------------------------------------- | --------------------------------------------------------------- |
| `/achat`                     | Show usage or current toggled channel        | `adminchat.use`                                                 |
| `/achat help`                | Show help                                    | `adminchat.use`                                                 |
| `/achat <message>`           | Send to default or toggled channel           | `adminchat.use` + channel permission                            |
| `/achat <channel> <message>` | Send to a specific channel                   | `adminchat.use` + channel permission                            |
| `/achat <channel>`           | Toggle a channel on/off                      | `adminchat.use` + channel permission                            |
| `/achat off`                 | Exit the toggled channel                     | `adminchat.use`                                                 |
| `/achat toggle [player]`     | Toggle visibility for self or another player | `adminchat.use`; other player requires `adminchat.admin`        |
| `/achat reload`              | Reload configuration                         | `adminchat.use` + `adminchat.admin`                             |
| `/adminchat ...`             | Same executor as `/achat`                    | `adminchat.admin` at Bukkit level, plus `adminchat.use` in code |

## Permissions

| Permission                | Description                         | Default |
| ------------------------- | ----------------------------------- | ------- |
| `adminchat.*`             | All AdminChat permissions           | op      |
| `adminchat.use`           | Allows using admin chat             | op      |
| `adminchat.admin`         | Access to admin management commands | op      |
| `adminchat.channel.mod`   | Access to mod channel               | op      |
| `adminchat.channel.admin` | Access to admin channel             | op      |
| `adminchat.channel.trial` | Access to trial channel             | op      |

## Configuration

### Top-level

- `config-version`: schema version for migration.
- `default_channel`: default channel when none is toggled. Default: `admin`.
- `cooldown`: message cooldown in seconds. Default: `2`.

### Channels

Each channel supports:

- `permission`: permission node required to use the channel.
- `format`: message format with `{sender}`, `{channel}`, `{message}`.
- `prefix`: channel prefix.
- `sound.enabled`: whether to play a sound.
- `sound.type`: Bukkit sound enum name.
- `sound.volume`: sound volume.
- `sound.pitch`: sound pitch.

Default channels:

- `mod`: `adminchat.channel.mod`
- `admin`: `adminchat.channel.admin`
- `trial`: `adminchat.channel.trial`

### Messages

Includes: `no_permission`, `no_channel`, `toggled_on`, `toggled_off`, `toggled_for`, `cooldown`, `reloaded`, `usage`, `usage_toggled`, `channel_on`, `channel_off`, `player_not_found`, `players_only`, `no_toggled_channel`, `unknown_command`, `visibility_enabled_by`, `visibility_disabled_by`.

### Notifications

- `notifications.join_notification`: enable/disable join notification.
- `notifications.join_message`: message with `{player}`.
- `notifications.quit_notification`: enable/disable quit notification.
- `notifications.quit_message`: message with `{player}`.