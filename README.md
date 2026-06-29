# ManualTournaments

[![License](https://img.shields.io/github/license/Wolren/ManualTournaments)](LICENSE)
[![Last commit](https://img.shields.io/github/last-commit/Wolren/ManualTournaments)](https://github.com/Wolren/ManualTournaments/commits)
[![Issues](https://img.shields.io/github/issues/Wolren/ManualTournaments)](https://github.com/Wolren/ManualTournaments/issues)
[![Repo size](https://img.shields.io/github/repo-size/Wolren/ManualTournaments)](https://github.com/Wolren/ManualTournaments)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=java)](pom.xml)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8--1.21-green?logo=minecraft)](https://minecraft.net)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/Wolren/ManualTournaments/badge)](https://securityscorecards.dev/viewer/?uri=github.com/Wolren/ManualTournaments)

ManualTournaments is a Spigot/Paper plugin for organizing manual fights and automated single-elimination tournaments. Create arenas, configure kits, and run team fights or full bracket tournaments with prizes, scheduling, and MySQL persistence.

## Features

- **Fight modes** - team, FFA, queue, team-arena
- **Tournament brackets** - single-elimination with auto-advancement and bye handling
- **Team tournaments** - 2v2, 3v3, up to 16 players per team
- **Configurable match timeout** - defaults to 15 minutes, per-tournament override
- **Prize distribution** - console commands executed on tournament end, `{player}` placeholder
- **Scheduled tournaments** - configurable times with auto-start
- **MySQL storage** - HikariCP connection pool, async writes, YAML fallback
- **Bukkit API events** - TournamentStartEvent, TournamentMatchEndEvent, PlayerEliminatedEvent, TournamentEndEvent
- **PlaceholderAPI** - 11 placeholders for tournament data
- **Per-player stats** - wins, losses, tournaments played
- **Granular permissions** - 18 per-command nodes + per-tournament join restriction
- **Bracket GUI** - clickable bracket inventory, click to spectate
- **Live scoreboard** - shown only to participants and spectators
- **Configurable messages** - all chat messages in config.yml with placeholders
- **Interactive GUIs** - arena, kit, settings, and bracket management
- **1.8 to 1.21 support** - across all listed Minecraft versions

## Installation

1. Download the JAR from the [releases page](https://github.com/Wolren/ManualTournaments/releases)
2. Place it in your server's `plugins/` folder
3. Restart or reload the server
4. Configure arenas and kits (see Setup below)

No external dependencies required. PlaceholderAPI is optional.

## Quick Setup

### GUI
```
/arena gui     - create and manage arenas
/kit gui       - create and manage kits
/settings gui  - configure plugin settings
```

### Manual
1. `/arena create (name)` - create an arena
2. `/arena pos1 (name)` - set team 1 spawn
3. `/arena pos2 (name)` - set team 2 spawn
4. `/arena spectator (name)` - set spectator position
5. `/arena validate (name)` - verify all positions
6. `/settings current_arena (name)` - set active arena
7. `/kit create (name)` - create a kit (hold items in your inventory)
8. `/settings current_kit (name)` - set active kit
9. `/fight team (player1) (player2)` - start a fight

For larger teams, first half of the names is team 1, second half is team 2.
Example: `/fight team p1 p2 p3 p4` creates 2v2 (p1+p2 vs p3+p4).

## Tournament System

### Quick start
```
/tournament create myEvent 16 myArena myKit       - 16 player solo tournament
/tournament create myEvent 16 myArena myKit 2      - 2v2 tournament (8 teams)
/tournament join myEvent                           - join the tournament
/tournament start myEvent                          - generate bracket and start
/tournament bracket myEvent                        - open bracket GUI
/tournament setprize myEvent add give {player} diamond 32  - set prizes
```

### Commands

| Command | Description |
|---------|-------------|
| `/tournament create (name) [maxPlayers] [arena] [kit] [teamSize]` | Create a tournament |
| `/tournament join (name)` | Join a tournament |
| `/tournament leave (name)` | Leave (forfeits during bracket) |
| `/tournament start (name)` | Start the tournament |
| `/tournament forcestart (name)` | Force start with fewer players |
| `/tournament info (name)` | Show tournament details |
| `/tournament list` | List all tournaments |
| `/tournament bracket (name)` | Open bracket GUI |
| `/tournament cancel (name)` | Cancel a tournament |
| `/tournament set (name) (option) (value)` | Change arena, kit, maxPlayers, matchtimeout, teamSize |
| `/tournament setprize (name) add|remove|list [cmd\|index]` | Manage prize commands |
| `/tournament spectate (name)` | Spectate running tournament |
| `/tournament pause (name)` | Pause match progression |
| `/tournament resume (name)` | Resume match progression |
| `/tournament forceadvance (name) (player)` | Force-advance a match |
| `/tournament delete (name)` | Permanently delete |
| `/tournament substitute (name) (old) (new)` | Replace a player |
| `/tournament kick (name) (player)` | Kick player from registration |
| `/tournament confirm` | Confirm pending action |
| `/tournament stats [player]` | View player statistics |
| `/tournament history` | Show past winners |
| `/tournament reload` | Reload config and schedules |
| `/tournament migrate` | Migrate YAML data to MySQL |

All commands also available via the `mt_` prefix.

## Permissions

| Permission | Description | Default |
|-----------|-------------|---------|
| `mt.*` | All commands | op |
| `mt.arena` | All /arena commands | op |
| `mt.fight` | Start fights | op |
| `mt.kit` | All /kit commands | op |
| `mt.settings` | All /settings commands | op |
| `mt.spectate` | /spectate | true |
| `mt.queue` | /queue | true |
| `mt.tournament` | Basic tournament commands (join, leave, spectate, info, list, bracket, confirm) | true |
| `mt.tournament.admin` | All tournament admin commands | op |
| `mt.tournament.create` | Create tournaments | op |
| `mt.tournament.delete` | Delete tournaments | op |
| `mt.tournament.cancel` | Cancel tournaments | op |
| `mt.tournament.set` | Modify tournament settings | op |
| `mt.tournament.setprize` | Manage prizes | op |
| `mt.tournament.forcestart` | Force-start | op |
| `mt.tournament.forceadvance` | Force-advance matches | op |
| `mt.tournament.pause` | Pause/resume | op |
| `mt.tournament.reload` | Reload config | op |
| `mt.tournament.substitute` | Substitute players | op |
| `mt.tournament.kick` | Kick players | op |
| `mt.tournament.stats` | View stats | op |

When `tournament-require-join-permission: true` in config.yml, players additionally need `mt.tournament.join.<tournamentName>` to join a specific tournament.

## Configuration

Key settings in `config.yml` (all documented inline):

| Setting | Default | Description |
|---------|---------|-------------|
| `tournament-match-timeout` | 900 | Match auto-end seconds (0 = disabled) |
| `tournament-confirm-timeout` | 10 | Confirmation expiry seconds (0 = disabled) |
| `tournament-stats-enabled` | true | Toggle per-player stats |
| `tournament-require-join-permission` | false | Require per-tournament permission to join |
| `tournament-database-pool-size` | 4 | HikariCP connection pool (MySQL only) |
| `tournament-scoreboard-interval` | 2 | Scoreboard refresh seconds |
| `tournament-save-debounce` | 2 | Save coalescing window seconds |
| `tournament-scheduler-interval` | 30 | Schedule check seconds |
| `tournament-block-state-limit` | 5000 | Max tracked blocks per fight |

All tournament chat messages are customizable via config.yml with placeholders:
`{name}`, `{player}`, `{winner}`, `{loser}`, `{p1}`, `{p2}`, `{players}`, `{rounds}`

### MySQL Setup
```yaml
mysql-enabled: true
mysql:
  url: localhost:3306
  username: root
  password: ""
```
Then run `/tournament reload` and optionally `/tournament migrate` to move existing data.
Paper ships a compatible JDBC driver. Falls back to YAML automatically if unavailable.

## PlaceholderAPI

| Placeholder | Returns |
|-----------|---------|
| `%mt_tournament_<name>_phase%` | REGISTRATION, IN_PROGRESS, FINISHED |
| `%mt_tournament_<name>_players%` | Current player count |
| `%mt_tournament_<name>_maxplayers%` | Max players |
| `%mt_tournament_<name>_winner%` | Winner name or TBD |
| `%mt_tournament_<name>_round%` | Active round |
| `%mt_tournament_<name>_totalrounds%` | Total rounds |
| `%mt_tournament_<name>_arena%` | Arena name |
| `%mt_tournament_<name>_kit%` | Kit name |
| `%mt_tournament_<name>_paused%` | true/false |
| `%mt_player_tournament%` | Tournament player is in |
| `%mt_player_eliminated%` | true/false |

## API Events

Other plugins can listen for tournament lifecycle:

- `TournamentStartEvent` - bracket generated, matches starting
- `TournamentMatchEndEvent` - match finished with winner and loser
- `PlayerEliminatedEvent` - player eliminated from tournament
- `TournamentEndEvent` - tournament finished with winner

## Building

Requires JDK 21 and IntelliJ IDEA (bundled Maven).

```bash
cd ManualTournaments
JAVA_HOME="path/to/jdk-21" \
"path/to/IntelliJ/plugins/maven/lib/maven3/bin/mvn" \
  clean package
```

The shaded JAR is at `target/ManualTournaments-1.5.jar`.

### Running tests
```bash
JAVA_HOME="path/to/jdk-21" \
"path/to/IntelliJ/plugins/maven/lib/maven3/bin/mvn" \
  test
```

31 tests covering bracket logic, team generation, serialization, and edge cases.

## Version Support

The plugin supports Minecraft 1.8 through 1.21 on Spigot and Paper. Some features may behave differently across versions:

- **Countdown sounds** - prior to 1.12 the experience orb was unavailable
- **Spigot** - spectators can perform empty hits on entities; sounds not cancellable
- **`/kit unbreakable`** - before 1.11 does not set items to unbreakable
- **Freeze during countdown** - optimized for newer versions

## Links

- [Spigot page](https://www.spigotmc.org/resources/manualtournaments.XXXXX/)
- [Issue tracker](https://github.com/Wolren/ManualTournaments/issues)
- [License](LICENSE)
