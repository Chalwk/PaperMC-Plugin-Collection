# Security Policy

This repository contains Minecraft plugins for PaperMC and compatible server
software. This document explains which versions receive security updates, how
to report a vulnerability, and what to expect when you do.

---

## Supported Versions

Each plugin carries its own version number in its `plugin.yml` and its
`build.gradle`. Fixes are applied to the latest version on `main` and, where
practical, backported to the most recent release tag.

| Version          | Supported          |
| ---------------- | ------------------ |
| Latest release   | :white_check_mark: |
| Latest on `main` | :white_check_mark: |
| Older releases   | :x:                |
| Forks            | :x:                |

If you have an older copy of a plugin, re-download from the
[Releases](https://github.com/Chalwk/PaperMC-Plugin-Collection/releases) page
or from `main` before reporting anything. It may already be fixed.

---

## Reporting a Vulnerability

**Please do not open a public issue for security problems.**

Two private channels are available:

1. **GitHub Private Vulnerability Reporting** (preferred). Use the
   [Report a vulnerability](https://github.com/Chalwk/PaperMC-Plugin-Collection/security/advisories/new)
   button on the repository's Security tab. This keeps the discussion private,
   tracks the fix, and lets us coordinate disclosure.
2. **Email**. If you'd rather not use GitHub, email
   [chalwk.dev@gmail.com](mailto:chalwk.dev@gmail.com) with "SECURITY" in the
   subject line.

### What to include

- The plugin and version (from `plugin.yml` or the release tag)
- Your server software and version (Paper, Purpur, Spigot - and the Minecraft version)
- A clear description of the issue
- Steps to reproduce, or a minimal proof of concept
- The impact you believe it has
- Whether you've disclosed it anywhere else

Redact any real API keys, database credentials, IP addresses, or player data
from what you send.

---

## Scope

### In scope

- Permission bypasses - a player can do something their permission nodes
  should prevent
- Command injection, argument injection, or unsafe use of `Runtime.exec`,
  `ProcessBuilder`, or reflection
- SQL injection or unsafe query construction
- Path traversal or arbitrary file read/write via plugin commands or config
- Insecure deserialization (`ObjectInputStream`, unsafe YAML loading, etc.)
- Arbitrary code execution via plugin config, command input, or API events
- Sensitive data (API keys, database credentials, player IPs) leaked into
  logs, chat, error output, or files
- Duplication bugs - a player can obtain items, currency, or XP they shouldn't
  be able to
- Economy exploits - a player can gain or destroy currency through a bug
- Protection bypasses - a plugin that claims to protect something (blocks,
  containers, regions) can be circumvented

### Out of scope

- Issues that require an attacker to already have OP or console access
- Issues that require an attacker to already be running untrusted code on
  the server
- Griefing or social engineering
- Denial of service against the server itself (e.g., a player spamming a
  command - that's what cooldowns and permissions are for)
- Rate limiting, uptime, or downtime of third-party services
- Typos, cosmetic bugs, or feature requests (open a regular issue for those)
- Findings from automated scanners with no demonstrated impact
- Vulnerabilities in PaperMC, Spigot, or Bukkit themselves - report those
  upstream

If you're not sure whether something is in scope, report it anyway and I'll
tell you.

---

## What to expect

This is a personal project maintained by one person. Realistic timelines:

- **Acknowledgement:** within 7 days
- **Initial assessment:** within 14 days
- **Fix or workaround:** depends on severity, but usually within 30 days for
  anything confirmed
- **Public disclosure:** coordinated with you. I'll credit you in the advisory
  unless you'd prefer to stay anonymous.

If a report is declined, I'll explain why. If it's a duplicate or already
known, I'll say so.

---

## Using these plugins safely

A few practices worth following regardless of any issue in the code itself:

- **Never hardcode database credentials or API keys in a config file that's
  committed to version control.** Use environment variables or a secrets
  manager where possible.
- **Audit before you install.** These plugins are open source and written to
  be read. Open the source. Check what it does. Only then drop the JAR into
  your server.
- **Keep your server patched.** PaperMC releases security fixes regularly.
  A vulnerability in the server itself can bypass any plugin's protections.
- **Limit OP.** Most plugins assume OP players are trusted. If you don't
  trust someone with full server access, don't give them OP.
- **Check your permissions.** A misconfigured permission node can expose
  functionality you didn't intend. Use a permissions plugin and audit
  regularly.

---

## Automated security

This repository runs the following GitHub security features on every push:

- CodeQL static analysis
- Dependabot alerts and security updates
- Secret scanning with push protection

Findings from these tools are triaged by the maintainer. If you've spotted
something the automated tools missed, that's exactly what the private
reporting channels above are for.