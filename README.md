[![License](https://img.shields.io/github/license/Wolren/ManualTournaments)](LICENSE)
[![Last commit](https://img.shields.io/github/last-commit/Wolren/ManualTournaments)](https://github.com/Wolren/ManualTournaments/commits)
[![Issues](https://img.shields.io/github/issues/Wolren/ManualTournaments)](https://github.com/Wolren/ManualTournaments/issues)
[![Repo size](https://img.shields.io/github/repo-size/Wolren/ManualTournaments)](https://github.com/Wolren/ManualTournaments)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](pom.xml)
[![Spigot](https://img.shields.io/badge/Spigot-1.20-yellow)](pom.xml)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20-green?logo=minecraft)](https://minecraft.net)

General information
There are numerous automated event solutions available, but only a limited number of manual ones. Sometimes there is a need for a serious tournament with cash prizes, where full control over all aspects, such as kits, settings, and cooldowns, becomes essential. This plugin offers precisely that capability by enabling you to organize fights between teams of varying sizes

Please note that the current version of the plugin is in its alpha stage, which means it may have some bugs. I'm actively working on addressing these issues and providing fixes for any existing problem so any reports are welcome


Future plans
Support for future mc versions with bugfixes
New features
Currently on the waiting list:

    Adding new fight types and programmable tournaments

    Kit editor

    Party system

    Adding kit handling of less used metas - BundleMeta, MusicInstrumentMeta, SkullMeta, ArmorMeta and getting the skull of any player

    Translations into other languages

Support
The plugin extends support to all versions ranging from 1.8 to 1.20, although it may not be flawlessly compatible with each version

    Countdown sounds, prior to version 1.12, the experience orb was unavailable in the Spigot API,

    Spigot, when using spigot as the server, spectators can perform empty hits on entities, sounds of which aren't cancellable to the fighters

    /kit unbreakable, before 1.11 using this command will not set the items in the kit to be unbreakable

    Freeze during the countdown, freezing mechanism is optimized for newer versions and may be less comfortable on older ones.

GUI Setup
Use /arena gui, /kit gui, and /settings gui to intuitively create arena, kit, and set the right settings. In case you don't understand what something does read the Commands in Documentation tab


Manual Setup
1. Create arena with: /arena create (name)
2. Set position for a first team: /arena pos1 (name)
3. Set position for the second team: /arena pos2 (name)
4. Set position for spectators: /arena spectator (name)
*Check if all positions are set correctly with: /arena validate (name)
5. Set current arena with /settings current_arena (name)
6. Create kit with /kit create (name)
7. Set current kit with /settings current_kit (name)
*Change the settings to your needs before starting the fight!
8. Use /fight team (player1) (player2) to start a fight between player1 and player2
*To start a fight between larger teams, first half of the names is team1 and second - team2. So /fight team (player1) (player2) (player3) (player4) will result in creating two teams, team1 with player1 and player2, team2 with player3 and player4