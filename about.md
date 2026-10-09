---
layout: page
title: About
subtitle: "What this collection is, and what it isn't."
permalink: /about/
---

`PaperMC-Plugin-Collection` is a personal collection of Minecraft plugins I've
written for PaperMC and compatible server software (Purpur, Spigot, Bukkit).
They span different categories - gameplay, administration, cosmetics - but
there's no unifying theme beyond "I needed this, so I wrote it".

## What you'll find here

- **Standalone plugins.** Each plugin is a complete, self-contained JAR. No
  shared library, no package to install, no inter-plugin dependency chain.
- **Paper API first.** Where Paper and Bukkit diverge, the Paper API wins.
  If a plugin genuinely needs a platform-specific API, it says so in its docs.
- **Granular permissions.** No `pluginname.*` wildcards that silently grant
  everything. Each action has its own permission node.
- **Configurable everything.** No hardcoded messages. Every user-facing string
  lives in `config.yml` or `messages.yml`.
- **`/pluginname help` always works.** Every command has a help subcommand,
  tab completion, and documented permissions.

## What you won't find here

- Frameworks, abstractions, or "platforms".
- Cross-plugin hard dependencies.
- Anything that requires editing a compiled JAR to configure.
- Code I wouldn't run on my own server.

## Conventions

A few rules I try to stick to:

1. **Java 21+.** Paper 1.21 requires Java 21; every plugin targets Java 21
   bytecode and is compiled against the Paper 1.21 API.
2. **Configs are versioned.** A `config-version` key lets migrations happen
   without guesswork.
3. **Destructive actions are opt-in**, not default. Nothing deletes blocks,
   items, or player data without an explicit confirmation.
4. **No reflection into server internals** unless there is no API alternative,
   and if there is, it's documented with the exact server versions it targets.

## License

MIT. Take what's useful, ignore the rest.

Copyright &copy; {{ site.author.name }}.