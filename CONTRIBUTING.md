---
layout: page
title: Contributing
subtitle: Bugs, ideas, and pull requests are welcome.
permalink: /contributing/
---

## Reporting a bug

Use the [bug report template](https://github.com/{{ site.repository }}/issues/new?template=bug-report.yaml)
and include:

- Which plugin is misbehaving (and its version)
- Your server software and version (Paper, Purpur, Spigot - and the Minecraft version)
- Your Java version
- The exact command or action that triggered the bug
- The full stack trace or error output
- Any relevant config file contents (redact secrets)

**Redact any API keys, database credentials, or real IP addresses before posting.**

## Reporting a security issue

**Do not open a public issue for security problems.**

Use the private [Report a vulnerability](https://github.com/{{ site.repository }}/security/advisories/new)
flow on the Security tab, or see [SECURITY.md](https://github.com/{{ site.repository }}/blob/main/SECURITY.md)
for the full policy, scope, and expected timelines.

## Suggesting a plugin or feature

Use the [plugin request template](https://github.com/{{ site.repository }}/issues/new?template=plugin-request.yaml).
Describe the problem you're trying to solve, not the solution you have in
mind - that gives me more room to suggest something simpler than what you'd
expect.

## Pull requests

Before opening a PR, make sure:

- The plugin compiles cleanly on Java 21 with `./gradlew build`
- The plugin targets PaperMC **1.21 or newer** and does not use APIs newer
  than `io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT` (the compile-time
  floor in the root `build.gradle`)
- No new dependencies unless they're listed in the plugin's `build.gradle`
  *and* in its docs page
- `/pluginname help` output is up to date
- Tab completion covers every subcommand and argument
- Permissions are declared in `plugin.yml` and documented
- No hardcoded messages - everything user-facing is in a config file
- No hardcoded secrets, API keys, or database credentials
- You've tested the cases described in the related issue

See [`.github/pull_request_template.md`](https://github.com/{{ site.repository }}/blob/main/.github/pull_request_template.md)
for the full checklist.

## Code style

There's no enforced linter, but the house style is:

- 4-space indentation, 120-column soft limit
- `final` on local variables where it clarifies intent
- Javadoc on public classes and non-obvious methods
- Prefer Paper API over Bukkit API where they diverge
- Prefer `Component` / Adventure API over legacy `String` messages
- No `@SuppressWarnings` without a comment explaining why

## Questions

Open a [GitHub Discussion](https://github.com/{{ site.repository }}/discussions)
or find me on [Discord]({{ site.discord_invite }}).