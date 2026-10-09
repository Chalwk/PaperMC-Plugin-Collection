# Building

This repository uses **Gradle** with a committed wrapper. You do **not** need
Gradle installed to build these plugins - the wrapper downloads the correct
version on first use. You only need a JDK.

---

## Prerequisites

| Requirement | Version          | Why                                                  |
| ----------- | ---------------- | ---------------------------------------------------- |
| Java (JDK)  | **21 or newer**  | Paper 1.21 targets Java 21 bytecode                  |
| Gradle      | **not required** | The wrapper (`gradlew` / `gradlew.bat`) handles this |

Verify Java:

```bash
java -version
```

You should see `openjdk version "21.x"` or higher. If not, install a JDK 21
from [Adoptium](https://adoptium.net/) or your package manager of choice.

---

## The wrapper vs. global Gradle

There are two ways to invoke Gradle. Use the wrapper unless you have a reason
not to.

| Command                             | What it uses                                  | When to use                                                    |
| ----------------------------------- | --------------------------------------------- | -------------------------------------------------------------- |
| `./gradlew` (Unix)                  | Wrapper - pinned to a specific Gradle version | Always, for local dev and CI                                   |
| `gradlew` / `gradlew.bat` (Windows) | Same                                          | Always, on Windows                                             |
| `gradle`                            | Whatever Gradle you have installed globally   | Only for regenerating the wrapper, or if the wrapper is broken |

The wrapper files live at the repo root:

```
gradlew                       # Unix/macOS shell script
gradlew.bat                   # Windows batch script
gradle/wrapper/
├── gradle-wrapper.jar        # Bootstrap jar (must be committed)
└── gradle-wrapper.properties # Pins the Gradle version
```

**These four files must be committed.** If they're missing, CI fails and you
can't build from the command line.

---

## Shell-specific syntax

Pick the row that matches your terminal:

| Shell              | Invoke the wrapper           |
| ------------------ | ---------------------------- |
| Windows CMD        | `gradlew :adminchat:build`   |
| Windows PowerShell | `.\gradlew :adminchat:build` |
| Git Bash           | `./gradlew :adminchat:build` |
| macOS / Linux      | `./gradlew :adminchat:build` |

Windows CMD does **not** understand `./`. PowerShell uses `.\` (backslash).
Unix shells use `./` (forward slash).

---

## Common commands

All commands below assume you're in the repository root
(`C:\GitHub Repositories\PaperMC-Plugin-Collection` on Windows).

### Build every plugin

```bash
./gradlew build
```

Produces a JAR for each plugin under
`papermc_plugins/<plugin>/build/libs/`.

### Build one plugin

```bash
./gradlew :<plugin>:build
```

Replace `<plugin>` with one of:

- `adminchat`
- `automessages`
- `bigbrother`
- `gamemodemanager`
- `noctiview`
- `vaculoot`

Example:

```bash
./gradlew :adminchat:build
```

### Build one plugin at a specific version

This is what the release workflow does. It stamps the version into
`plugin.yml` inside the JAR.

```bash
./gradlew :adminchat:build -Pversion=1.0.0
```

Produces `papermc_plugins/adminchat/build/libs/AdminChat-1.0.0.jar`.

If you omit `-Pversion`, the version falls back to `0.0.0-dev` (see
`build.gradle` at the repo root).

### Clean and rebuild

```bash
./gradlew :adminchat:clean :adminchat:build
```

`clean` deletes `build/` for that plugin. Combine with `build` to force a
full recompile.

### Clean everything

```bash
./gradlew clean
```

### List all available tasks

```bash
./gradlew tasks
```

To see tasks for a single plugin:

```bash
./gradlew :adminchat:tasks
```

---

## Where the JARs go

After a build, the output JAR is at:

```
papermc_plugins/<plugin>/build/libs/<PluginName>-<version>.jar
```

For example:

```
papermc_plugins/adminchat/build/libs/AdminChat-1.0.0.jar
papermc_plugins/vaculoot/build/libs/VacuLoot-0.0.0-dev.jar
```

The JAR name is controlled by `build.gradle` inside each plugin:

```groovy
jar {
    archiveBaseName.set('AdminChat')
}
```

The `-<version>` suffix comes from whatever `-Pversion=` you passed (or the
fallback).

---

## Verifying the built JAR

A JAR is just a ZIP. You can inspect its `plugin.yml` to confirm the version
was stamped correctly.

### Windows (CMD)

```cmd
jar xf papermc_plugins\adminchat\build\libs\AdminChat-1.0.0.jar plugin.yml
type plugin.yml
del plugin.yml
```

Look for the `version:` line. It should read `version: 1.0.0`, not
`version: '${version}'`.

### macOS / Linux / Git Bash

```bash
unzip -p papermc_plugins/adminchat/build/libs/AdminChat-1.0.0.jar plugin.yml | head
```

### PowerShell

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead("papermc_plugins\adminchat\build\libs\AdminChat-1.0.0.jar")
$entry = $zip.GetEntry("plugin.yml")
$reader = New-Object System.IO.StreamReader($entry.Open())
$reader.ReadToEnd()
$reader.Close()
$zip.Dispose()
```

If the version shows as the literal string `${version}`, the
`processResources` expansion in the root `build.gradle` isn't firing - check
that the plugin's `plugin.yml` uses `version: '${version}'` (quoted) and not
`version: 1.0.0`.

---

## Installing a built JAR

1. Copy the JAR into your server's `plugins/` directory.
2. Restart the server (or use a plugin manager that supports hot-loading).
3. The plugin generates its config on first run under
   `plugins/<PluginName>/config.yml`.
4. Edit the config, then run `/reload confirm` or restart.

---

## The VS Code Gradle panel

The Gradle extension for VS Code shows a tree of every task in the project.
You can run tasks by right-clicking and picking **Run**.

**Limitation:** the panel cannot pass project properties like
`-Pversion=1.0.0`. Panel builds will always use the fallback version
(`0.0.0-dev`) unless you add a `gradle.properties` file (see below).

If you need a specific version, use the terminal. The panel is fine for
day-to-day compile checks.

---

## Setting a default version

If you want panel builds (and bare `./gradlew build` invocations) to use a
specific version without typing `-Pversion=...` every time, add a
`gradle.properties` at the repo root:

```properties
version=1.0.0
```

Precedence (highest wins):

1. Command-line `-Pversion=X`
2. `gradle.properties` at repo root
3. Fallback in `build.gradle` (`0.0.0-dev`)

For per-plugin defaults, put a `gradle.properties` inside each plugin folder
instead. You'll need to adjust the root `build.gradle` to read from the
subproject - see the plugin's `build.gradle` for details.

---

## Troubleshooting

### `'.' is not recognized as an internal or external command`

You're on Windows CMD and used `./gradlew`. Drop the `./`:

```cmd
gradlew :adminchat:build
```

### `'gradlew' is not recognized as an internal or external command`

The wrapper isn't in the repo. Check:

```cmd
dir /b gradlew*
```

If nothing is printed, the wrapper files are missing. Regenerate them:

```cmd
gradle wrapper --gradle-version 8.10.2
```

This requires a global Gradle install (see below). After running, commit all
four wrapper files:

```cmd
git add gradlew gradlew.bat gradle/wrapper/
git commit -m "Add Gradle wrapper files"
```

### `gradle` is not recognized

You need a global Gradle install to bootstrap the wrapper (or to regenerate
it). Options:

- **Scoop:** `scoop install gradle`
- **Chocolatey:** `choco install gradle`
- **Manual:** download from https://gradle.org/releases/, extract to
  e.g. `C:\Gradle\gradle-8.10.2\`, and add `C:\Gradle\gradle-8.10.2\bin` to
  your `PATH`.

Verify with:

```cmd
gradle -v
```

### `WARNING: A restricted method in java.lang.System has been called`

You're running Gradle 8.10.2 on a very recent JDK (21.0.5+ or 22+). It's a
harmless warning about native library loading and doesn't affect the build.
It will disappear in a future Gradle release.

To suppress it in the meantime, add to `gradle.properties` at the repo root:

```properties
org.gradle.jvmargs=--enable-native-access=ALL-UNNAMED
```

### `Could not resolve io.papermc.paper:paper-api`

You don't have network access to the PaperMC Maven repository. Check that
your firewall/proxy allows `https://repo.papermc.io`. The URL is declared in
the root `build.gradle` under `repositories`.

### `Unsupported class file major version 65`

You're compiling with Java 21 but running Gradle on an older JDK. Check:

```bash
java -version
```

and make sure `JAVA_HOME` points at a JDK 21+ installation. On Windows:

```cmd
echo %JAVA_HOME%
```

### Build succeeds but the JAR has the wrong version

Make sure you passed `-Pversion=X` (capital `P`, lowercase `v`):

```bash
./gradlew :adminchat:build -Pversion=1.0.0
```

Confirm the plugin's `plugin.yml` uses `version: '${version}'` - quoted, so
Gradle's resource expansion doesn't strip it as a Groovy expression.

### Gradle daemon is slow to start

Gradle runs a background daemon. The first build after a boot takes longer
because the daemon has to spin up. Subsequent builds reuse it.

To see daemon status:

```bash
./gradlew --status
```

To stop all daemons:

```bash
./gradlew --stop
```

To disable the daemon for a single build (slower, used in CI):

```bash
./gradlew build --no-daemon
```

---

## See also

- [`README.md`](README.md) - project overview and layout
- [`CONTRIBUTING.md`](CONTRIBUTING.md) - PR checklist and code style
- [`.github/workflows/build.yml`](.github/workflows/build.yml) - the CI build
- [`.github/workflows/release.yml`](.github/workflows/release.yml) - the release workflow
```

---

## Where to put this

Save it as `BUILDING.md` at the repo root:

```
C:\GitHub Repositories\PaperMC-Plugin-Collection\BUILDING.md
```

Then commit:

```cmd
git add BUILDING.md
git commit -m "Add BUILDING.md"
```

---

## Small follow-ups worth doing while you're here

1. **Update the README's build section** to link to this file instead of duplicating the commands. Keeps one source of truth. Change:

   ```markdown
   ## Building

   See [BUILDING.md](BUILDING.md) for the full guide, or:
   ```

   …and drop the rest of that section.

2. **Commit the wrapper files you just generated.** They aren't tracked yet, so CI still won't work until you do:

   ```cmd
   dir /b gradlew*
   git status
   ```

   You should see `gradlew`, `gradlew.bat`, and `gradle/wrapper/` listed as untracked. Add them all:

   ```cmd
   git add gradlew gradlew.bat gradle/wrapper/
   git commit -m "Add Gradle wrapper files"
   git push
   ```

3. **Verify the version stamp worked.** Run:

   ```cmd
   jar xf papermc_plugins\adminchat\build\libs\AdminChat-1.0.0.jar plugin.yml
   type plugin.yml
   del plugin.yml
   ```

   The `version:` line should read `1.0.0`. If it shows `${version}`, tell me - that means the `plugin.yml` still has `version: 1.0.0` unquoted, or the `processResources` block isn't firing.