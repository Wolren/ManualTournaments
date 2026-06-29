# Project Conventions

## Java 8 Compatibility (MUST)

This plugin supports Minecraft 1.8 through 1.21+. **All source code MUST compile with Java 8 target (`-target 8`).**

### Rules
- **No Java 9+ language features**: No `var`, no collection factory methods (`List.of`, `Map.of`, `Set.of`), no `java.util.Optional` methods added in Java 9+, no Stream API additions from Java 9+, no `Files.readString`/`writeString`, no `java.lang.Module`, no `Records`, no `sealed classes`, no `pattern matching`, no `switch expressions`.
- **Only Java 8 APIs**: Use Guava or Commons Lang equivalents for anything not in Java 8. Use `org.bukkit.util.StringUtil` where available.
- **Lombok is allowed**: `@Getter`, `@Setter`, `@SneakyThrows`, `@AllArgsConstructor` etc. are fine (compiles to regular Java 8 bytecode).
- **Test code is exempt**: Tests compile with Java 21 (MockBukkit requires it), but must still avoid testing Java 9+ features in the plugin code itself.

### Maven setup
- `pom.xml` uses `maven-compiler-plugin` with `<source>8</source>` and `<target>8</target>` for main compilation.
- Test compilation uses Java 21.
- CI uses JDK 21 with `--release 8` equivalence for main sources.

## Server Runtime Files

All local server files (worlds, config, logs, plugins, etc.) go in `server/`. This keeps the repo root clean.

## Local Dev Server

Run a Paper/Spigot server with the plugin loaded from IntelliJ:

1. **Download a Paper server jar** for each MC version you want to test
2. Place it at `server/versions/<version>/paper-<version>.jar` (e.g. `server/versions/1.21/paper-1.21.jar`)
3. Run the server once manually to generate config files (let it stop)
4. From IntelliJ, pick a **Server 1.xx** run configuration from the toolbar dropdown and click Run/Debug

The script (`scripts/StartServer.ps1`) will build the plugin, deploy it, and start the server with JPDA debugging on `localhost:50xx` (5005–5009). In Debug mode, attach a Remote JVM debugger to the corresponding port to set breakpoints in plugin code.

Available run configurations (each on a unique debug port):

| Name | Version | Debug Port |
|------|---------|------------|
| Server 1.8  | 1.8  | 5005 |
| Server 1.12 | 1.12 | 5006 |
| Server 1.16 | 1.16 | 5007 |
| Server 1.20 | 1.20 | 5008 |
| Server 1.21 | 1.21 | 5009 |

### Creating a server version directory

```
server/versions/<version>/
├── paper-<version>.jar       # or spigot-<version>.jar
├── server.properties
├── bukkit.yml
├── plugins/
│   └── ManualTournaments.jar  # (auto-deployed by the script)
└── ...                        # other server files
```
