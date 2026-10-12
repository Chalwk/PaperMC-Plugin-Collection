---
layout: plugin
title: NexusPermissions
description: "Permissions plugin with groups, inheritance, per-world and per-region contexts, timed permissions, Vault, PlaceholderAPI, and promote/demote ladders."
category: Administration
plugin_id: nexuspermissions
latest_version: "1.1.0"
tool_name: "Open in Nexus Editor"
tool_url: "/tools/nexus-editor/"
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
  - "Per-world, per-server, and per-region contexts"
  - "Prefix, suffix, weight, and custom options"
  - "Timed permissions with duration syntax"
  - "Ladders for promote and demote"
  - "Vault, PlaceholderAPI, and WorldGuard integration"
  - "Public API for other plugins"
  - "UUID-based user storage"
  - "Optional audit log for every mutation"
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
      - nexus.user.delete
      - nexus.user.rename
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
      - nexus.group.delete
      - nexus.group.rename
---

# NexusPermissions

NexusPermissions is a permissions plugin. It provides groups, users,
inheritance, per-world, per-server and per-region contexts, timed
permissions, and promote/demote ladders, all managed from in-game
commands and stored in plain YAML that you can read and edit by hand.

The design is simple: everything is a subject. Users and groups are
both subjects, both hold permissions, both can have parents. A user's
effective permissions are the result of walking their parent chain from
lowest-weight to highest-weight, then applying their own entries on top.

> **Work in progress.** NexusPermissions is under active development. The
> core feature set is functional and tested, but the command surface and
> config schema may still change between releases. Back up your
> `groups.yml` and `users.yml` before upgrading.

---

## Features

- Users and groups, both first-class subjects.
- Multi-parent inheritance with cycle detection.
- Per-world, per-server, and per-region contexts. A permission can be
  granted in one world, one WorldGuard region, or nowhere at all.
- Group weight determines priority (lower weight wins).
- Negation via a `-` prefix in config files.
- Timed permissions. Grant a node for a fixed duration and it expires on
  its own.
- Ladders for `promote` and `demote`, with automatic ladder detection when
  only one is defined.
- Options for prefix, suffix, weight, and any custom key you want.
- Wildcards like `essentials.*` and `*` are supported through Bukkit's
  native permission lookup.
- Vault integration: registers as the server's permission and chat
  provider when Vault is present.
- PlaceholderAPI expansion: `%nexus_prefix%` and friends, for any plugin
  that prefers placeholders over Vault.
- Public API: `Bukkit.getServicesManager().load(NexusAPI.class)` for other
  plugins.
- UUID-based user storage. Legacy name-keyed entries are migrated
  automatically the first time each player joins.
- Optional audit log so admins can see who changed what and when.
- YAML storage that you can read and hand-edit.

---

## Commands

| Command                                                                     | Description                                           | Permission                                    |
| --------------------------------------------------------------------------- | ----------------------------------------------------- | --------------------------------------------- |
| `/nexus` / `/nexus help`                                                    | Show help                                             | `nexus.command`                               |
| `/nexus reload`                                                             | Reload all config files and reapply to online players | `nexus.command` + `nexus.reload`              |
| `/nexus debug`                                                              | Toggle debug mode                                     | `nexus.command` + `nexus.debug`               |
| `/nexus user <user> info`                                                   | Show a user's groups, options, and permissions        | `nexus.command` + `nexus.user.info`           |
| `/nexus user <user> groups`                                                 | Show just a user's groups                             | `nexus.command` + `nexus.user.info`           |
| `/nexus user <user> has <node>`                                             | Check whether a user has a permission                 | `nexus.command` + `nexus.user.info`           |
| `/nexus user <user> perm <node> <true\|false\|unset> [context] [temp:7d]`   | Set a user permission                                 | `nexus.command` + `nexus.user.perm.set`       |
| `/nexus user <user> parent add <group>`                                     | Add a user to a group                                 | `nexus.command` + `nexus.user.parent.add`     |
| `/nexus user <user> parent remove <group>`                                  | Remove a user from a group                            | `nexus.command` + `nexus.user.parent.remove`  |
| `/nexus user <user> option <key> <value\|unset>`                            | Set a user option                                     | `nexus.command` + `nexus.user.option.set`     |
| `/nexus user <user> delete`                                                 | Delete a stored user entry                            | `nexus.command` + `nexus.user.delete`         |
| `/nexus group <group> create`                                               | Create a group                                        | `nexus.command` + `nexus.group.create`        |
| `/nexus group <group> info`                                                 | Show a group                                          | `nexus.command` + `nexus.group.info`          |
| `/nexus group <group> members`                                              | List stored members of a group                        | `nexus.command` + `nexus.group.info`          |
| `/nexus group <group> perm <node> <true\|false\|unset> [context] [temp:7d]` | Set a group permission                                | `nexus.command` + `nexus.group.perm.set`      |
| `/nexus group <group> parent add <group>`                                   | Add a parent group                                    | `nexus.command` + `nexus.group.parent.add`    |
| `/nexus group <group> parent remove <group>`                                | Remove a parent group                                 | `nexus.command` + `nexus.group.parent.remove` |
| `/nexus group <group> option <key> <value\|unset>`                          | Set a group option                                    | `nexus.command` + `nexus.group.option.set`    |
| `/nexus group <group> weight <number>`                                      | Set a group's weight                                  | `nexus.command` + `nexus.group.weight.set`    |
| `/nexus group <group> delete`                                               | Delete a group                                        | `nexus.command` + `nexus.group.delete`        |
| `/nexus rename user <old> <new>`                                            | Rename a stored user                                  | `nexus.command` + `nexus.user.rename`         |
| `/nexus rename group <old> <new>`                                           | Rename a group and fix all references                 | `nexus.command` + `nexus.group.rename`        |
| `/nexus promote <user> [ladder]`                                            | Move a user up a ladder                               | `nexus.command` + `nexus.promote`             |
| `/nexus demote <user> [ladder]`                                             | Move a user down a ladder                             | `nexus.command` + `nexus.demote`              |
| `/nexus rank <ladder> add <group>`                                          | Add a group to the top of a ladder                    | `nexus.command` + `nexus.rank.manage`         |
| `/nexus who <node>`                                                         | List users and groups that have a permission          | `nexus.command`                               |
| `/nexus search <term>`                                                      | Search groups and users by name                       | `nexus.command`                               |

Tab completion covers every subcommand, every argument, and every option
the sender has permission to use.

---

## Contexts

A context scopes a permission to a specific world, server, or WorldGuard
region. The context argument accepts these forms:

| Input                   | Interpreted as                     |
| ----------------------- | ---------------------------------- |
| *(omitted)* or `global` | Applies everywhere                 |
| `world_nether`          | Shorthand for `world:world_nether` |
| `world:world_nether`    | Full form                          |
| `server:lobby`          | Server-wide context                |
| `region:spawn`          | WorldGuard region context          |

When resolving a player's effective permissions, the plugin consults, in
order: `server:<server-name>`, `world:<current-world>`,
`region:<region-name>` for each region the player is standing in, then
global. Later entries in the list override earlier ones, so a region-level
grant overrides a world-level one.

Region contexts require WorldGuard and are toggled with
`region-context.enabled` in `config.yml`.

---

## Configuration

### `config.yml`

- `config-version`: schema version for migration.
- `debug`: enable verbose logging.
- `default-group`: the group applied to users with no explicit parents.
- `server-name`: used to build the `server:<name>` context.
- `region-context.enabled`: enable WorldGuard region contexts.
- `audit-log.enabled`: enable the audit log.
- `timed-permission-sweep-seconds`: how often to prune expired entries.
- `ladders`: named ladders for promote/demote.

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
- `permissions`: a list of nodes. A leading `-` negates. A `;` suffix
  marks an expiry as epoch milliseconds. See the header comment in the
  bundled file for the exact format.
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

Users are keyed by UUID. The `name` field is kept for readability. Users
who have never been edited live implicitly in the default group and have
no entry.

Legacy entries keyed by name are still read. The UUID is filled in the
first time that player joins, and the entry is rewritten under the UUID
on the next save.

```yaml
users:

  550e8400-e29b-41d4-a716-446655440000:
    name: ExamplePlayer
    parents:
      - "default"
      - "moderator"

  550e8400-e29b-41d4-a716-446655440001:
    name: ExampleDonor
    options:
      prefix: "&6&l[VIP] &6"
    parents:
      - "default"
      - "patron"

  550e8400-e29b-41d4-a716-446655440002:
    name: ExampleRestricted
    permissions:
      - "-essentials.fly"
    parents:
      - "default"
      - "patron"
```

---

## Timed permissions

Any perm subcommand accepts an optional `temp:<duration>` argument.

```
/nexus user Chalwk perm essentials.fly true temp:7d
/nexus group moderator perm essentials.vanish true temp:30m
/nexus user Chalwk perm essentials.god true world_nether temp:1h
```

Supported duration suffixes are `s` (seconds), `m` (minutes), `h` (hours),
`d` (days), and `w` (weeks).

The expiry is enforced at resolution time, so the node stops applying the
moment it expires, even before the periodic sweep removes it from disk.
The sweep runs every `timed-permission-sweep-seconds` and also triggers a
save and online refresh when it removes anything.

To see the expiry of a node, use `/nexus user <name> info` or
`/nexus group <name> info`. The YAML stores it as a `;` suffix on the
permission entry.

---

## Vault and prefix display

When Vault is present, Nexus registers as both a permission provider and
a chat provider. Plugins that read through Vault see the correct
permissions and prefixes automatically.

Two things to be aware of when wiring this up to a chat or tab list
plugin:

- **Swap your plugin's own prefix token for the Vault one.** Some chat
  plugins have a built-in prefix token (CMI's `[prefix]`, for example)
  that reads from the plugin's own rank system, not from Vault. Replace
  it with `%vault_prefix%` via PlaceholderAPI, or the native
  `%nexus_prefix%` described below.
- **Nexus returns prefixes with section-sign codes, not ampersands.**
  Consumers that translate `&` codes themselves leave section signs
  alone. Consumers that don't translate still render the colours.

## PlaceholderAPI

When PlaceholderAPI is installed, Nexus registers a `nexus` expansion:

| Placeholder             | Value                                        |
| ----------------------- | -------------------------------------------- |
| `%nexus_prefix%`        | Resolved prefix, with section-sign colours   |
| `%nexus_suffix%`        | Resolved suffix, with section-sign colours   |
| `%nexus_primary_group%` | Highest priority group name                  |
| `%nexus_groups%`        | Comma separated group list, sorted by weight |
| `%nexus_group_count%`   | Number of effective groups                   |
| `%nexus_has_<node>%`    | `true` or `false`                            |
| `%nexus_option_<key>%`  | Any option value                             |

Use these in CMI, tab list plugins, DeluxeMenus, and anywhere else that
supports PAPI.

## Public API

Other plugins can query Nexus directly:

```java
NexusAPI api = Bukkit.getServicesManager().load(NexusAPI.class);
if (api != null) {
    Map<String, Boolean> perms = api.getEffectivePermissions(player.getUniqueId(), player.getWorld().getName());
    String prefix = api.getOption(player.getUniqueId(), "prefix");
    List<String> groups = api.getGroups(player.getUniqueId());
}
```

Add `softdepend: [NexusPermissions]` to your plugin.yml. The API is
read-only. Mutations go through the `/nexus` commands.

## Audit log

Set `audit-log.enabled: true` in config.yml to append a line to
`plugins/NexusPermissions/audit.log` for every mutation:

```
[2026-10-11 14:22:31] [Chalwk] user teddy perm essentials.fly true global
[2026-10-11 14:23:02] [Chalwk] group villager weight 200
[2026-10-11 14:24:15] [Chalwk] user teddy parent add artisan
```

Useful on servers with more than one admin.

---

## Notes

- Users are keyed by UUID. Legacy name-keyed entries from earlier
  versions migrate automatically the first time each player joins after
  this update. Nothing is written on join; the entry is rewritten under
  the UUID the next time any command saves users.yml.
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
- Vault integration is strictly read-only. Any plugin that tries to add
  or remove permissions, groups, prefixes, or suffixes through Vault will
  get an `UnsupportedOperationException`.
- Region contexts require WorldGuard. If it's absent or the lookup fails,
  the plugin falls back to server and world contexts only.

---

## Dependencies

Bundles **Adventure** (`adventure-platform-bukkit`) inside its JAR,
relocated to `com.chalwk.libs.adventure`.

Optional integrations, all `softdepend`, none required:

- **Vault**. Registers as a permission and chat provider.
- **PlaceholderAPI**. Registers the `nexus` expansion.
- **WorldGuard**. Enables region contexts.

---

## Changelog

### 1.1.0

- Added timed permissions. Any perm subcommand accepts a `temp:<duration>`
  argument, and expired entries are filtered at resolution time and swept
  every `timed-permission-sweep-seconds`.
- Added a native PlaceholderAPI expansion. Provides `%nexus_prefix%`,
  `%nexus_suffix%`, `%nexus_primary_group%`, `%nexus_groups%`,
  `%nexus_group_count%`, `%nexus_has_<node>%`, and
  `%nexus_option_<key>%`.
- Added a public read API. Registered under `NexusAPI` via the Bukkit
  services manager. Query effective permissions, options, groups, and
  reverse lookups.
- Added WorldGuard region contexts. Players standing inside a region
  resolve the `region:<name>` context alongside server and world.
- Added audit log, off by default, toggled with `audit-log.enabled`. Every
  mutation writes a line to `plugins/NexusPermissions/audit.log`.
- User storage is now keyed by UUID. Legacy name-keyed entries migrate
  automatically the first time each player joins.
- Added `/nexus user <user> delete` and `/nexus group <group> delete`,
  with safety checks that refuse to leave a config in a broken state.
- Added `/nexus user <user> groups`, `/nexus user <user> has <node>`,
  `/nexus group <group> members`, `/nexus who <node>`,
  `/nexus search <term>`, `/nexus rename user <old> <new>`, and
  `/nexus rename group <old> <new>`.

### 1.0.2

- Prefixes and suffixes returned through Vault now come back with section
  sign codes rather than ampersands.

### 1.0.1

- Added Vault integration. Read-only.
- Registered as Vault permission and chat provider.

### 1.0.0

- Initial release.