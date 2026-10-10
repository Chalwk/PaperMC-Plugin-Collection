---
layout: plugin
title: NexusPermissions
description: "Permissions plugin with groups, inheritance, per-world contexts, and promote/demote ladders."
category: Administration
plugin_id: nexuspermissions
latest_version: "1.0.0"
author: Chalwk
website: https://github.com/Chalwk/PaperMC-Plugin-Collection
api-version: 1.21
main: com.chalwk.nexus.NexusPermissions
source_path: papermc_plugins/nexuspermissions
minecraft_versions: "1.21+"
server_software: "Paper, Purpur, Spigot"
java_version: "21+"
tags:
  - permissions
  - groups
  - staff
features:
  - "Groups, users, and inheritance"
  - "Per-world and per-server contexts"
  - "Prefix, suffix, and weight options"
  - "Ladders for promote and demote"
  - "YAML storage that survives reloads"
  - "Runs on Spigot, Paper, and Purpur"
commands:
  - name: nexus
    description: NexusPermissions main command
    usage: /nexus [help]
    permission: nexus.command
    aliases: [nex, nexusperms, nperms]
permissions:
  - name: nexus.*
    description: All NexusPermissions permissions
    default: op
    children:
      - nexus.command
      - nexus.reload
      - nexus.debug
      - nexus.promote
      - nexus.demote
      - nexus.rank.manage
      - nexus.user.*
      - nexus.group.*
  - name: nexus.command
    description: Allows using /nexus
    default: op
  - name: nexus.reload
    description: Allows reloading configuration files
    default: op
  - name: nexus.debug
    description: Allows toggling debug mode
    default: op
  - name: nexus.promote
    description: Allows promoting a user along a ladder
    default: op
  - name: nexus.demote
    description: Allows demoting a user along a ladder
    default: op
  - name: nexus.rank.manage
    description: Allows managing ladders
    default: op
  - name: nexus.user.*
    description: All user management permissions
    default: op
    children:
      - nexus.user.info
      - nexus.user.perm.set
      - nexus.user.parent.add
      - nexus.user.parent.remove
      - nexus.user.option.set
  - name: nexus.group.*
    description: All group management permissions
    default: op
    children:
      - nexus.group.info
      - nexus.group.create
      - nexus.group.perm.set
      - nexus.group.parent.add
      - nexus.group.parent.remove
      - nexus.group.option.set
      - nexus.group.weight.set
---

# NexusPermissions

NexusPermissions is a permissions plugin. It provides groups, users,
inheritance, per-world and per-server contexts, and promote/demote
ladders, all managed from in-game commands and stored in plain YAML
that you can read and edit by hand.

The design is simple: everything is a subject. Users and groups are
both subjects, both hold permissions, both can have parents. A user's
effective permissions are the result of walking their parent chain from
lowest-weight to highest-weight, then applying their own entries on top.

> **Work in progress.** NexusPermissions is under active development. The
> core feature set (groups, inheritance, contexts, ladders) is functional
> and tested, but the command surface and config schema may still change
> between releases. Back up your `groups.yml` and `users.yml` before
> upgrading, and treat any 0.x release as a moving target.

---

## Features

- Users and groups, both first-class subjects.
- Multi-parent inheritance with cycle detection.
- Per-world and per-server contexts: a permission can be granted in one
  world but not another.
- Group weight determines priority (lower weight wins).
- Negation via a `-` prefix in config files.
- Ladders for `promote` and `demote`, with automatic ladder detection when
  only one is defined.
- Options for prefix, suffix, weight, and any custom key you want.
- Wildcards like `essentials.*` and `*` are supported through Bukkit's
  native permission lookup.
- YAML storage that you can read and hand-edit.
- Neither file is rewritten on reload until you make a change.

---

## Commands

| Command                                                           | Description                                           | Permission                                    |
| ----------------------------------------------------------------- | ----------------------------------------------------- | --------------------------------------------- |
| `/nexus` / `/nexus help`                                          | Show help                                             | `nexus.command`                               |
| `/nexus reload`                                                   | Reload all config files and reapply to online players | `nexus.command` + `nexus.reload`              |
| `/nexus debug`                                                    | Toggle debug mode                                     | `nexus.command` + `nexus.debug`               |
| `/nexus user <user> info`                                         | Show a user's groups, options, and permissions        | `nexus.command` + `nexus.user.info`           |
| `/nexus user <user> perm <node> <true\|false\|unset> [context]`   | Set a user permission                                 | `nexus.command` + `nexus.user.perm.set`       |
| `/nexus user <user> parent add <group>`                           | Add a user to a group                                 | `nexus.command` + `nexus.user.parent.add`     |
| `/nexus user <user> parent remove <group>`                        | Remove a user from a group                            | `nexus.command` + `nexus.user.parent.remove`  |
| `/nexus user <user> option <key> <value\|unset>`                  | Set a user option                                     | `nexus.command` + `nexus.user.option.set`     |
| `/nexus group <group> create`                                     | Create a group                                        | `nexus.command` + `nexus.group.create`        |
| `/nexus group <group> info`                                       | Show a group                                          | `nexus.command` + `nexus.group.info`          |
| `/nexus group <group> perm <node> <true\|false\|unset> [context]` | Set a group permission                                | `nexus.command` + `nexus.group.perm.set`      |
| `/nexus group <group> parent add <group>`                         | Add a parent group                                    | `nexus.command` + `nexus.group.parent.add`    |
| `/nexus group <group> parent remove <group>`                      | Remove a parent group                                 | `nexus.command` + `nexus.group.parent.remove` |
| `/nexus group <group> option <key> <value\|unset>`                | Set a group option                                    | `nexus.command` + `nexus.group.option.set`    |
| `/nexus group <group> weight <number>`                            | Set a group's weight (lower = higher priority)        | `nexus.command` + `nexus.group.weight.set`    |
| `/nexus promote <user> [ladder]`                                  | Move a user up a ladder                               | `nexus.command` + `nexus.promote`             |
| `/nexus demote <user> [ladder]`                                   | Move a user down a ladder                             | `nexus.command` + `nexus.demote`              |
| `/nexus rank <ladder> add <group>`                                | Add a group to the top of a ladder                    | `nexus.command` + `nexus.rank.manage`         |

Tab completion covers every subcommand, every argument, and every option
the sender has permission to use.

---

## Contexts

A context scopes a permission to a specific world or server. The context
argument accepts three forms:

| Input                   | Interpreted as                     |
| ----------------------- | ---------------------------------- |
| *(omitted)* or `global` | Applies everywhere                 |
| `world_nether`          | Shorthand for `world:world_nether` |
| `world:world_nether`    | Full form                          |
| `server:lobby`          | Server-wide context                |

When resolving a player's effective permissions, the plugin consults, in
order: `server:<server-name>`, `world:<current-world>`, then global.

---

## Configuration

### `config.yml`

- `config-version`: schema version for migration.
- `debug`: enable verbose logging.
- `default-group`: the group applied to users with no explicit parents.
  Defaults to `default`.
- `server-name`: used to build the `server:<name>` context. Set this
  differently on each backend server if you're running a proxy network.
- `ladders`: named ladders for promote/demote. Each is a list of groups,
  lowest rank first.

```yaml
ladders:
  staff:
    - "helper"
    - "moderator"
    - "admin"
    - "owner"
  donor:
    - "supporter"
    - "patron"
    - "benefactor"
```

### `groups.yml`

Every group's definition. Keys use `/` as the YAML path separator, so
group names containing `.` are safe.

Each group supports:

- `options`: arbitrary key-value pairs. `weight` and `prefix` are the two
  the plugin reads natively.
- `permissions`: a list of nodes. A leading `-` negates.
- `contexts`: a map of context name to permission list.
- `parents`: a list of parent group names.

```yaml
groups:

  default:
    options:
      prefix: "&7"
      weight: 1000
    parents: []
    permissions:
      - "essentials.spawn"
      - "essentials.help"
      - "essentials.balance"
      - "-essentials.fly"

  builder:
    options:
      prefix: "&a[Builder] &a"
      weight: 500
    parents:
      - "default"
    permissions:
      - "essentials.gamemode.creative"
      - "worldedit.wand"
      - "worldedit.selection.pos"
    contexts:
      "world:creative":
        - "essentials.fly"

  moderator:
    options:
      prefix: "&8[&bMod&8] &b"
      weight: 100
      rank-ladder: "staff"
    parents:
      - "default"
    permissions:
      - "essentials.fly"
      - "essentials.kick"
      - "essentials.mute"
```

The bundled file also includes `admin`, `supporter`, and `patron` as
further examples, showing wildcards, negation, and a multi-tier donor
chain.

### `users.yml`

Only users with explicit modifications are saved here. A user who has
never been edited lives implicitly in the default group and has no entry.

```yaml
users:

  ExamplePlayer:
    parents:
      - "default"
      - "moderator"

  ExampleDonor:
    options:
      prefix: "&6&l[VIP] &6"
    parents:
      - "default"
      - "patron"

  ExampleRestricted:
    permissions:
      - "-essentials.fly"
    parents:
      - "default"
      - "patron"

  ExampleScoped:
    contexts:
      "world:creative":
        - "worldedit.region.set"
        - "worldedit.selection.pos"
    parents:
      - "default"
```

The bundled file also includes `ExampleBuilder`, showing a per-user
permission on top of group membership.

---

## Notes

- Users are stored by name, not by UUID. This is simple and works well on
  online-mode servers. On offline-mode servers (BungeeCord, Velocity in
  offline mode, or standalone offline), names can be taken by different
  players, so use with care. A UUID-based mode is a possible future
  improvement.
- Wildcards (`essentials.*`, `*`) are left in the permission attachment
  as-is. Bukkit resolves them at query time.
- Inheritance uses weight to order parents: lower weight applies first,
  so its entries are overridden by higher-weight parents and by the
  subject's own permissions.
- Neither `config.yml`, `groups.yml`, nor `users.yml` is rewritten by
  a reload. Edits from `/nexus` commands are written immediately; nothing
  else is.
- If `groups.yml` or `users.yml` fails to parse on startup, the plugin
  logs the error and enters a locked state where saving is refused. Fix
  the YAML and run `/nexus reload` to unlock.

---

## Dependencies

Bundles **Adventure** (`adventure-platform-bukkit`) inside its JAR,
relocated to `com.chalwk.libs.adventure`. No external dependencies.

---

## Changelog

### 1.0.0

- Initial release.