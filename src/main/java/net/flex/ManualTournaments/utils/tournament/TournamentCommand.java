package net.flex.ManualTournaments.utils.tournament;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.Spectate;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class TournamentCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "create", "join", "leave", "start", "cancel", "info", "list",
            "set", "forcestart", "bracket", "setprize", "spectate", "pause", "resume",
            "forceadvance", "delete", "substitute", "reload", "history",
            "confirm", "kick", "stats", "migrate"
    );

    private static final List<String> SET_OPTIONS = Arrays.asList("arena", "kit", "maxplayers", "matchtimeout", "teamsize");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create":
                return cmdCreate(sender, args);
            case "join":
                return cmdJoin(sender, args);
            case "leave":
                return cmdLeave(sender, args);
            case "start":
                return cmdStart(sender, args);
            case "forcestart":
                return cmdForceStart(sender, args);
            case "cancel":
                return cmdCancel(sender, args);
            case "info":
                return cmdInfo(sender, args);
            case "list":
                return cmdList(sender);
            case "set":
                return cmdSet(sender, args);
            case "bracket":
                return cmdBracket(sender, args);
            case "setprize":
                return cmdSetPrize(sender, args);
            case "spectate":
                return cmdSpectate(sender, args);
            case "pause":
                return cmdPause(sender, args);
            case "resume":
                return cmdResume(sender, args);
            case "forceadvance":
                return cmdForceAdvance(sender, args);
            case "delete":
                return cmdDelete(sender, args);
            case "substitute":
                return cmdSubstitute(sender, args);
            case "reload":
                return cmdReload(sender);
            case "history":
                return cmdHistory(sender);
            case "confirm":
                return cmdConfirm(sender);
            case "kick":
                return cmdKick(sender, args);
            case "stats":
                return cmdStats(sender, args);
            case "migrate":
                return cmdMigrate(sender);
            default:
                sendUsage(sender);
                return true;
        }
    }

    // --- /tournament create <name> [maxPlayers] [arena] [kit] [teamSize] ---
    private boolean cmdCreate(CommandSender sender, String[] args) {
        Player player = checkPlayer(sender);
        if (player == null) return true;
        if (!player.hasPermission("mt.tournament.create")) {
            player.sendMessage("§6You don't have permission to create tournaments.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§7Usage: §e/tournament create <name> [maxPlayers] [arena] [kit]");
            return true;
        }

        String name = args[1];

        // Validate name: alphanumeric, underscores, hyphens only
        if (!name.matches("^[a-zA-Z0-9_-]{2,32}$")) {
            player.sendMessage("§6Tournament name must be 2-32 characters, alphanumeric with underscores and hyphens only.");
            return true;
        }

        int maxPlayers = args.length > 2 ? parseInt(args[2], 16) : 16;
        String arena = args.length > 3 ? args[3] : config.getString("current-arena", "");
        String kit = args.length > 4 ? args[4] : config.getString("current-kit", "");
        int teamSize = args.length > 5 ? parseInt(args[5], 1) : 1;

        if (!Main.arenaNames.contains(arena)) {
            player.sendMessage("§6Arena '§e" + arena + "§6' doesn't exist. Create it with §e/arena create");
            return true;
        }
        if (!Main.kitNames.contains(kit)) {
            player.sendMessage("§6Kit '§e" + kit + "§6' doesn't exist. Create it with §e/kit create");
            return true;
        }
        if (teamSize < 1 || teamSize > 16) {
            player.sendMessage("§6Team size must be 1-16.");
            return true;
        }
        // Ensure at least 2 players can play
        if (maxPlayers < 2) {
            player.sendMessage("§6Max players must be at least 2.");
            return true;
        }

        boolean created = TournamentManager.getInstance().createTournament(name, maxPlayers, arena, kit);
        if (created) {
            Tournament t = TournamentManager.getInstance().getTournament(name);
            if (t != null && teamSize > 1) t.setTeamSize(teamSize);
            TournamentManager.getInstance().saveAll();
            player.sendMessage("§aTournament '§e" + name + "§a' created.");
            player.sendMessage("§7  Max players: §e" + maxPlayers + "  §7Team size: §e" + teamSize);
            player.sendMessage("§7  Arena: §e" + arena + "  §7Kit: §e" + kit);
            if (teamSize > 1) {
                player.sendMessage("§7  This is a team tournament — players are grouped into teams of §e" + teamSize);
            }
            player.sendMessage("§7Players can join with: §e/tournament join " + name);
        } else {
            player.sendMessage("§6A tournament named '§e" + name + "§6' already exists.");
        }
        return true;
    }

    // --- /tournament join <name> ---
    private boolean cmdJoin(CommandSender sender, String[] args) {
        Player player = optional(sender);
        if (player == null) return true;
        if (args.length < 2) {
            player.sendMessage("§7Usage: §e/tournament join <name>");
            return true;
        }

        String name = args[1];
        Tournament t = TournamentManager.getInstance().getTournament(name);
        if (t == null) {
            player.sendMessage("§6Tournament '§e" + name + "§6' doesn't exist.");
            return true;
        }
        if (t.getPhase() != Tournament.Phase.REGISTRATION) {
            player.sendMessage("§6Tournament '§e" + name + "§6' is not accepting players.");
            return true;
        }

        // Check if already in another tournament
        List<Tournament> active = TournamentManager.getInstance().getPlayerTournaments(player.getUniqueId());
        if (!active.isEmpty()) {
            player.sendMessage("§6You're already registered in '§e"
                    + active.get(0).getName() + "§6'. Leave first with §e/tournament leave");
            return true;
        }

        // Check per-tournament join permission if configured
        if (Main.getPlugin().getConfig().getBoolean("tournament-require-join-permission", false)) {
            if (!player.hasPermission("mt.tournament.join." + name)) {
                player.sendMessage("§6You don't have permission to join this tournament.");
                return true;
            }
        }

        boolean joined = TournamentManager.getInstance().joinTournament(name, player.getUniqueId());
        if (joined) {
            player.sendMessage("§aJoined tournament '§e" + name + "§a' (" + t.getPlayerCount()
                    + "/" + t.getMaxPlayers() + ")");
            Bukkit.getLogger().info("[ManualTournaments] " + player.getName()
                    + " joined tournament '" + name + "'");
        } else if (t.getPlayerCount() >= t.getMaxPlayers()) {
            player.sendMessage("§6Tournament '§e" + name + "§6' is full (" + t.getMaxPlayers() + "/" + t.getMaxPlayers() + ")");
        } else {
            player.sendMessage("§6Could not join tournament '§e" + name + "§6'.");
        }
        return true;
    }

    // --- /tournament leave <name> ---
    private boolean cmdLeave(CommandSender sender, String[] args) {
        Player player = optional(sender);
        if (player == null) return true;
        if (args.length < 2) {
            player.sendMessage("§7Usage: §e/tournament leave <name>");
            return true;
        }

        Tournament t = TournamentManager.getInstance().getTournament(args[1]);
        if (t != null && t.getPhase() == Tournament.Phase.IN_PROGRESS) {
            TournamentManager.getInstance().requireConfirm(player,
                    "Forfeit from tournament '§e" + args[1] + "§6'? This counts as a loss.",
                    () -> {
                        TournamentManager.getInstance().leaveTournament(args[1], player.getUniqueId());
                        player.sendMessage("§aYou forfeited from '§e" + args[1] + "§a'.");
                    });
            return true;
        }

        boolean left = TournamentManager.getInstance().leaveTournament(args[1], player.getUniqueId());
        if (left) {
            player.sendMessage("§aLeft tournament '§e" + args[1] + "§a'.");
        } else {
            player.sendMessage("§6Could not leave tournament '§e" + args[1] + "§6'.");
        }
        return true;
    }

    // --- /tournament start <name> ---
    private boolean cmdStart(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§7Usage: §e/tournament start <name>");
            return true;
        }
        boolean started = TournamentManager.getInstance().startTournament(args[1]);
        if (started) {
            Tournament t = TournamentManager.getInstance().getTournament(args[1]);
            String msg = "§6[Tournament] §e" + args[1] + " §7has started! "
                    + t.getPlayers().size() + " players, " + t.getTotalRounds() + " rounds.";
            Bukkit.broadcastMessage(msg);
            Bukkit.getLogger().info("[ManualTournaments] Tournament '" + args[1] + "' started.");
        } else {
            sender.sendMessage("§6Could not start tournament '§e" + args[1]
                    + "§6'. Check it exists, has 2+ players, and is in registration phase.");
        }
        return true;
    }

    // --- /tournament forcestart <name> ---
    private boolean cmdForceStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mt.tournament.forcestart")) {
            sender.sendMessage("§6You don't have permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§7Usage: §e/tournament forcestart <name>");
            return true;
        }
        Tournament t = TournamentManager.getInstance().getTournament(args[1]);
        if (t == null) {
            sender.sendMessage("§6Tournament '§e" + args[1] + "§6' doesn't exist.");
            return true;
        }
        t.setStartedByForce(true);
        boolean started = TournamentManager.getInstance().startTournament(args[1]);
        if (started) {
            Bukkit.broadcastMessage("§6[Tournament] §e" + args[1] + " §7force-started by admin!");
        } else {
            sender.sendMessage("§6Could not force-start. Check tournament status and player count.");
        }
        return true;
    }

    // --- /tournament cancel <name> ---
    private boolean cmdCancel(CommandSender sender, String[] args) {
        Player player = checkPlayer(sender);
        if (player == null) return true;
        if (!player.hasPermission("mt.tournament.cancel")) {
            player.sendMessage("§6You don't have permission.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§7Usage: §e/tournament cancel <name>");
            return true;
        }
        TournamentManager.getInstance().requireConfirm(player,
                "Cancel tournament '§e" + args[1] + "§6'?",
                () -> {
                    TournamentManager.getInstance().cancelTournament(args[1]);
                    player.sendMessage("§aTournament '§e" + args[1] + "§a' cancelled.");
                });
        return true;
    }

    // --- /tournament info <name> ---
    private boolean cmdInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§7Usage: §e/tournament info <name>");
            return true;
        }
        Tournament t = TournamentManager.getInstance().getTournament(args[1]);
        if (t == null) {
            sender.sendMessage("§6Tournament '§e" + args[1] + "§6' doesn't exist.");
            return true;
        }

        sender.sendMessage("§6=== §e" + t.getName() + " §6===");
        sender.sendMessage("§7Phase: §e" + t.getPhase().name());
        sender.sendMessage("§7Players: §e" + t.getPlayerCount() + "/" + t.getMaxPlayers());
        if (t.isTeamTournament()) {
            sender.sendMessage("§7Teams: §e" + t.getTeamCount() + "  §7Team size: §e" + t.getTeamSize());
        }
        sender.sendMessage("§7Arena: §e" + (t.getArenaName() != null ? t.getArenaName() : "§7not set"));
        sender.sendMessage("§7Kit: §e" + (t.getKitName() != null ? t.getKitName() : "§7not set"));

        if (t.getPhase() == Tournament.Phase.IN_PROGRESS) {
            int round = t.getActiveRound() + 1;
            int total = t.getTotalRounds();
            sender.sendMessage("§7Round: §e" + round + "/" + total);

            StringBuilder players = new StringBuilder("§7Participants: ");
            for (UUID uid : t.getPlayers()) {
                String name = Bukkit.getOfflinePlayer(uid).getName();
                if (name != null) {
                    boolean alive = t.getBracket().stream()
                            .flatMap(List::stream)
                            .filter(m -> m.containsPlayer(uid))
                            .noneMatch(m -> {
                                UUID w = m.getWinner();
                                return w != null && !w.equals(uid);
                            });
                    players.append(alive ? "§a" : "§7").append(name).append("§r, ");
                }
            }
            sender.sendMessage(players.toString().replaceAll(", $", ""));
        } else if (t.getPhase() == Tournament.Phase.FINISHED) {
            if (t.getWinner() != null) {
                String name = Bukkit.getOfflinePlayer(t.getWinner()).getName();
                sender.sendMessage("§7Winner: §6§l" + (name != null ? name : t.getWinner().toString()));
            }
        } else if (t.getPhase() == Tournament.Phase.REGISTRATION) {
            StringBuilder list = new StringBuilder("§7Registered: ");
            for (UUID uid : t.getPlayers()) {
                String name = Bukkit.getOfflinePlayer(uid).getName();
                list.append("§e").append(name != null ? name : "unknown").append("§7, ");
            }
            sender.sendMessage(list.toString().replaceAll(", $", ""));
        }
        return true;
    }

    // --- /tournament list ---
    private boolean cmdList(CommandSender sender) {
        Collection<Tournament> all = TournamentManager.getInstance().getTournaments().values();
        if (all.isEmpty()) {
            sender.sendMessage("§7No tournaments exist. Create one with §e/tournament create");
            return true;
        }
        sender.sendMessage("§6=== Tournaments ===");
        for (Tournament t : all) {
            String phaseColor;
            switch (t.getPhase()) {
                case REGISTRATION: phaseColor = "§a"; break;
                case IN_PROGRESS:  phaseColor = "§e"; break;
                case FINISHED:     phaseColor = "§7"; break;
                case CANCELLED:    phaseColor = "§c"; break;
                default:           phaseColor = "§7";
            }
            sender.sendMessage(" §e" + t.getName() + " " + phaseColor + t.getPhase().name()
                    + " §7(" + t.getPlayerCount() + "/" + t.getMaxPlayers() + ")");
        }
        return true;
    }

    // --- /tournament set <name> <option> <value> ---
    private boolean cmdSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mt.tournament.set")) {
            sender.sendMessage("§6You don't have permission.");
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage("§7Usage: §e/tournament set <name> <arena|kit|maxplayers> <value>");
            return true;
        }
        Tournament t = TournamentManager.getInstance().getTournament(args[1]);
        if (t == null) {
            sender.sendMessage("§6Tournament '§e" + args[1] + "§6' doesn't exist.");
            return true;
        }
        if (t.getPhase() != Tournament.Phase.REGISTRATION) {
            sender.sendMessage("§6Can only modify tournament during registration phase.");
            return true;
        }

        String option = args[2].toLowerCase();
        String value = args[3];

        switch (option) {
            case "arena":
                if (!Main.arenaNames.contains(value)) {
                    sender.sendMessage("§6Arena '§e" + value + "§6' doesn't exist.");
                    return true;
                }
                t.setArenaName(value);
                sender.sendMessage("§aTournament arena set to §e" + value);
                break;
            case "kit":
                if (!Main.kitNames.contains(value)) {
                    sender.sendMessage("§6Kit '§e" + value + "§6' doesn't exist.");
                    return true;
                }
                t.setKitName(value);
                sender.sendMessage("§aTournament kit set to §e" + value);
                break;
            case "maxplayers":
                int max = parseInt(value, -1);
                if (max <= 0 || (max & (max - 1)) != 0 || max > 64) {
                    sender.sendMessage("§6maxPlayers must be a power of 2 (2, 4, 8, 16, 32, 64).");
                    return true;
                }
                if (t.getPlayerCount() > max) {
                    sender.sendMessage("§6Cannot reduce maxPlayers below current player count (" + t.getPlayerCount() + ").");
                    return true;
                }
                sender.sendMessage("§7Max players will apply to new tournaments. Recreate to change.");
                break;
            case "matchtimeout":
                int timeout = parseInt(value, -1);
                if (timeout < 0 || timeout > 3600) {
                    sender.sendMessage("§6Match timeout must be 0-3600 seconds (0 = use config default).");
                    return true;
                }
                t.setMatchTimeout(timeout);
                sender.sendMessage("§aMatch timeout set to §e" + timeout + " §aseconds (0 = config default).");
                break;
            case "teamsize":
                int ts = parseInt(value, -1);
                if (ts < 1 || ts > 16) {
                    sender.sendMessage("§6Team size must be 1-16.");
                    return true;
                }
                if (t.getPlayerCount() > 0 && t.getPlayerCount() < ts * 2 && t.getPlayerCount() >= 2) {
                    sender.sendMessage("§6Current player count (" + t.getPlayerCount() + ") needs at least " + (ts * 2) + " for 2 full teams.");
                    return true;
                }
                t.setTeamSize(ts);
                sender.sendMessage("§aTeam size set to §e" + ts + "§a.");
                break;
            default:
                sender.sendMessage("§6Unknown option. Use: arena, kit, maxplayers, matchtimeout, teamsize");
        }
        TournamentManager.getInstance().saveAll();
        return true;
    }

    // --- /tournament setprize <name> add|remove|list [command|index] ---
    private boolean cmdSetPrize(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mt.tournament.setprize")) {
            sender.sendMessage("§6You don't have permission.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§7Usage:");
            sender.sendMessage("§e/tournament setprize <name> add <command>");
            sender.sendMessage("§e/tournament setprize <name> remove <index>");
            sender.sendMessage("§e/tournament setprize <name> list");
            sender.sendMessage("§7Use {player} as placeholder for winner name.");
            return true;
        }
        Tournament t = TournamentManager.getInstance().getTournament(args[1]);
        if (t == null) {
            sender.sendMessage("§6Tournament '§e" + args[1] + "§6' doesn't exist.");
            return true;
        }
        String action = args[2].toLowerCase();
        switch (action) {
            case "add": {
                if (args.length < 4) {
                    sender.sendMessage("§6Specify a command, e.g. §e give {player} diamond 64");
                    return true;
                }
                StringBuilder cmd = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    if (i > 3) cmd.append(" ");
                    cmd.append(args[i]);
                }
                t.addPrizeCommand(cmd.toString());
                TournamentManager.getInstance().saveAll();
                sender.sendMessage("§aPrize added: §e/" + cmd);
                break;
            }
            case "remove": {
                if (args.length < 4) {
                    sender.sendMessage("§6Specify the prize index. Use §e/tournament setprize <name> list");
                    return true;
                }
                try {
                    int index = Integer.parseInt(args[3]);
                    if (index < 1 || index > t.getPrizeCommands().size()) {
                        sender.sendMessage("§6Index out of range (1-" + t.getPrizeCommands().size() + ").");
                        return true;
                    }
                    String removed = t.getPrizeCommands().get(index - 1);
                    t.removePrizeCommand(index - 1);
                    TournamentManager.getInstance().saveAll();
                    sender.sendMessage("§aRemoved prize §e#" + index + ": §7/" + removed);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§6Index must be a number.");
                }
                break;
            }
            case "list": {
                List<String> prizes = t.getPrizeCommands();
                if (prizes.isEmpty()) {
                    sender.sendMessage("§7No prizes configured for §e" + t.getName());
                } else {
                    sender.sendMessage("§6=== Prizes for §e" + t.getName() + " §6===");
                    for (int i = 0; i < prizes.size(); i++) {
                        sender.sendMessage(" §e#" + (i + 1) + " §7/" + prizes.get(i));
                    }
                }
                break;
            }
            default:
                sender.sendMessage("§6Use: add, remove, or list.");
        }
        return true;
    }

    // --- /tournament spectate <name> ---
    private boolean cmdSpectate(CommandSender sender, String[] args) {
        Player player = optional(sender);
        if (player == null) return true;
        if (args.length < 2) {
            player.sendMessage("§7Usage: §e/tournament spectate <name>");
            return true;
        }
        Tournament t = TournamentManager.getInstance().getTournament(args[1]);
        if (t == null) {
            player.sendMessage("§6Tournament '§e" + args[1] + "§6' doesn't exist.");
            return true;
        }
        if (t.getPhase() != Tournament.Phase.IN_PROGRESS) {
            player.sendMessage("§6Tournament is not in progress.");
            return true;
        }

        String arenaName = t.getArenaName();
        if (arenaName == null || !Main.arenaNames.contains(arenaName)) {
            player.sendMessage("§6Tournament arena not configured.");
            return true;
        }

        // Use existing Spectate command for the teleport, but track as tournament spectator
        Spectate spectatorCmd = new Spectate();
        spectatorCmd.onCommand(player, null, "spectate", new String[]{arenaName});
        TournamentManager.getInstance().addSpectator(t.getName(), player.getUniqueId());
        player.sendMessage("§7Now spectating §e" + t.getName() + "§7. Use §e/spec stop §7to leave.");
        return true;
    }

    // --- /tournament pause <name> ---
    private boolean cmdPause(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mt.tournament.pause")) {
            sender.sendMessage("§6You don't have permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§7Usage: §e/tournament pause <name>");
            return true;
        }
        boolean paused = TournamentManager.getInstance().pauseTournament(args[1]);
        if (!paused) {
            sender.sendMessage("§6Could not pause. Check tournament exists and is in progress.");
        }
        return true;
    }

    // --- /tournament resume <name> ---
    private boolean cmdResume(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mt.tournament.pause")) {
            sender.sendMessage("§6You don't have permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§7Usage: §e/tournament resume <name>");
            return true;
        }
        boolean resumed = TournamentManager.getInstance().resumeTournament(args[1]);
        if (!resumed) {
            sender.sendMessage("§6Could not resume. Check tournament exists and is paused.");
        }
        return true;
    }

    // --- /tournament forceadvance <name> <player> ---
    private boolean cmdForceAdvance(CommandSender sender, String[] args) {
        Player player = checkPlayer(sender);
        if (player == null) return true;
        if (!player.hasPermission("mt.tournament.forceadvance")) {
            player.sendMessage("§6You don't have permission.");
            return true;
        }
        if (args.length < 3) {
            player.sendMessage("§7Usage: §e/tournament forceadvance <name> <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            player.sendMessage("§6Player '§e" + args[2] + "§6' not found.");
            return true;
        }
        TournamentManager.getInstance().requireConfirm(player,
                "Force-advance tournament '§e" + args[1] + "§6' — " + target.getName() + " wins?",
                () -> {
                    TournamentManager.getInstance().forceAdvance(args[1], target.getUniqueId());
                    player.sendMessage("§aForce-advanced.");
                });
        return true;
    }

    // --- /tournament delete <name> ---
    private boolean cmdDelete(CommandSender sender, String[] args) {
        Player player = checkPlayer(sender);
        if (player == null) return true;
        if (!player.hasPermission("mt.tournament.delete")) {
            player.sendMessage("§6You don't have permission.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§7Usage: §e/tournament delete <name>");
            return true;
        }
        TournamentManager.getInstance().requireConfirm(player,
                "Delete tournament '§e" + args[1] + "§6' permanently?",
                () -> {
                    TournamentManager.getInstance().deleteTournament(args[1]);
                    player.sendMessage("§aTournament '§e" + args[1] + "§a' deleted.");
                });
        return true;
    }

    // --- /tournament substitute <name> <oldPlayer> <newPlayer> ---
    private boolean cmdSubstitute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mt.tournament.substitute")) {
            sender.sendMessage("§6You don't have permission.");
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage("§7Usage: §e/tournament substitute <name> <oldPlayer> <newPlayer>");
            return true;
        }
        Player oldPlayer = Bukkit.getPlayer(args[2]);
        Player newPlayer = Bukkit.getPlayer(args[3]);
        if (oldPlayer == null) {
            sender.sendMessage("§6Player '§e" + args[2] + "§6' not found.");
            return true;
        }
        if (newPlayer == null) {
            sender.sendMessage("§6Player '§e" + args[3] + "§6' not found.");
            return true;
        }
        boolean substituted = TournamentManager.getInstance().substitutePlayer(
                args[1], oldPlayer.getUniqueId(), newPlayer.getUniqueId()
        );
        if (substituted) {
            sender.sendMessage("§aSubstituted §e" + oldPlayer.getName() + " §a→ §e" + newPlayer.getName()
                    + " §ain tournament '§e" + args[1] + "§a'.");
            oldPlayer.sendMessage("§6You have been replaced by §e" + newPlayer.getName() + " §6in tournament '§e" + args[1] + "§6'.");
            newPlayer.sendMessage("§aYou have been added to tournament '§e" + args[1] + "§a' as a substitute for §e" + oldPlayer.getName() + "§a.");
        } else {
            sender.sendMessage("§6Could not substitute. Check the tournament exists, is in registration phase, and the old player is registered.");
        }
        return true;
    }

    // --- /tournament reload ---
    private boolean cmdReload(CommandSender sender) {
        if (!sender.hasPermission("mt.tournament.reload")) {
            sender.sendMessage("§6You don't have permission.");
            return true;
        }
        TournamentManager.getInstance().reloadAll();
        sender.sendMessage("§aTournament data reloaded from disk.");
        return true;
    }

    // --- /tournament history [name] ---
    private boolean cmdHistory(CommandSender sender) {
        Collection<Tournament> all = TournamentManager.getInstance().getTournaments().values();
        List<Tournament> finished = all.stream()
                .filter(t -> t.getPhase() == Tournament.Phase.FINISHED)
                .sorted((a, b) -> Long.compare(b.getCreatedTime(), a.getCreatedTime()))
                .collect(Collectors.toList());
        if (finished.isEmpty()) {
            sender.sendMessage("§7No finished tournaments yet.");
            return true;
        }
        sender.sendMessage("§6=== Tournament History ===");
        int shown = 0;
        for (Tournament t : finished) {
            if (shown >= 15) break;
            String name = t.getName();
            String winner = t.getWinner() != null
                    ? Bukkit.getOfflinePlayer(t.getWinner()).getName()
                    : "?";
            if (winner == null) winner = t.getWinner().toString().substring(0, 8);
            sender.sendMessage(" §7- §e" + name + " §7→ Winner: §6" + winner
                    + " §7(" + t.getPlayerCount() + " players)");
            shown++;
        }
        if (finished.size() > 15) {
            sender.sendMessage(" §7... and " + (finished.size() - 15) + " more.");
        }
        return true;
    }

    // --- /tournament bracket <name> ---
    private boolean cmdBracket(CommandSender sender, String[] args) {
        Player player = optional(sender);
        if (player == null) return true;
        if (args.length < 2 && TournamentManager.getInstance().getTournaments().size() == 1) {
            // Single tournament — open its bracket directly
            String name = TournamentManager.getInstance().getTournamentNames().get(0);
            TournamentGUI.openBracket(player, TournamentManager.getInstance().getTournament(name));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§7Usage: §e/tournament bracket <name>");
            return true;
        }
        Tournament t = TournamentManager.getInstance().getTournament(args[1]);
        if (t == null) {
            player.sendMessage("§6Tournament '§e" + args[1] + "§6' doesn't exist.");
            return true;
        }
        TournamentGUI.openBracket(player, t);
        return true;
    }

    // --- Tab completion ---
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS;
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "join":
                case "leave":
                case "start":
                case "forcestart":
                case "cancel":
                case "info":
                case "set":
                case "setprize":
                case "spectate":
                case "pause":
                case "resume":
                case "forceadvance":
                case "delete":
                case "substitute":
                case "kick":
                case "bracket":
                    return TournamentManager.getInstance().getTournamentNames();
                default:
                    return Collections.emptyList();
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("substitute")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("substitute")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("kick")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return SET_OPTIONS;
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            String option = args[2].toLowerCase();
            if (option.equals("arena")) return new ArrayList<>(Main.arenaNames);
            if (option.equals("kit")) return new ArrayList<>(Main.kitNames);
        }
        return Collections.emptyList();
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6=== Tournament Commands ===");
        sender.sendMessage("§e/tournament create <name> [maxPlayers] [arena] [kit] [teamSize] §7- Create");
        sender.sendMessage("§e/tournament join <name> §7- Join tournament");
        sender.sendMessage("§e/tournament leave <name> §7- Leave tournament");
        sender.sendMessage("§e/tournament start <name> §7- Start tournament");
        sender.sendMessage("§e/tournament forcestart <name> §7- Force start (admin)");
        sender.sendMessage("§e/tournament cancel <name> §7- Cancel tournament");
        sender.sendMessage("§e/tournament pause <name> §7- Pause tournament");
        sender.sendMessage("§e/tournament resume <name> §7- Resume tournament");
        sender.sendMessage("§e/tournament forceadvance <name> <player> §7- Force-advance match");
        sender.sendMessage("§e/tournament delete <name> §7- Delete tournament");
        sender.sendMessage("§e/tournament substitute <name> <old> <new> §7- Replace a player");
        sender.sendMessage("§e/tournament reload §7- Reload tournament data");
        sender.sendMessage("§e/tournament history §7- Past winners");
        sender.sendMessage("§e/tournament info <name> §7- Tournament info");
        sender.sendMessage("§e/tournament list §7- List tournaments");
        sender.sendMessage("§e/tournament set <name> <opt> <val> §7- Modify settings");
        sender.sendMessage("§e/tournament setprize <name> add|remove|list §7- Manage prizes");
        sender.sendMessage("§e/tournament spectate <name> §7- Spectate tournament");
        sender.sendMessage("§e/tournament stats [player] §7- View statistics");
        sender.sendMessage("§e/tournament migrate §7- Migrate YAML data to MySQL");
        sender.sendMessage("§e/tournament confirm §7- Confirm pending action");
        sender.sendMessage("§e/tournament bracket <name> §7- Open bracket view");
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    // --- helpers ---

    private static Player checkPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§4Only players can use this command.");
            return null;
        }
        return (Player) sender;
    }

    // --- /tournament confirm ---
    private boolean cmdConfirm(CommandSender sender) {
        Player player = checkPlayer(sender);
        if (player == null) return true;
        TournamentManager.getInstance().confirmAction(player);
        return true;
    }

    // --- /tournament kick <name> <player> ---
    private boolean cmdKick(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mt.tournament.kick")) {
            sender.sendMessage("§6You don't have permission.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§7Usage: §e/tournament kick <name> <player>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage("§6Player '§e" + args[2] + "§6' not found.");
            return true;
        }
        boolean kicked = TournamentManager.getInstance().kickPlayer(args[1], target.getUniqueId());
        if (kicked) {
            sender.sendMessage("§aKicked §e" + target.getName() + " §afrom tournament '§e" + args[1] + "§a'.");
            target.sendMessage("§6You were kicked from tournament '§e" + args[1] + "§6'.");
        } else {
            sender.sendMessage("§6Could not kick. Check the tournament exists and is in registration phase.");
        }
        return true;
    }

    // --- /tournament migrate ---
    private boolean cmdMigrate(CommandSender sender) {
        if (!sender.hasPermission("mt.tournament.reload")) {
            sender.sendMessage("§6You don't have permission.");
            return true;
        }
        if (!TournamentDatabase.getInstance().isAvailable()) {
            sender.sendMessage("§6MySQL is not enabled or connected. Set mysql-enabled: true in config.yml and restart.");
            return true;
        }
        boolean migrated = TournamentManager.getInstance().migrateToMySQL();
        if (migrated) {
            sender.sendMessage("§aTournament data migrated from YAML to MySQL.");
        } else {
            sender.sendMessage("§6Migration failed. Check console for details.");
        }
        return true;
    }

    // --- /tournament stats [player] ---
    private boolean cmdStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mt.tournament.stats")) {
            sender.sendMessage("§6You don't have permission.");
            return true;
        }
        UUID targetId;
        String targetName;
        if (args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§6Player '§e" + args[1] + "§6' not found.");
                return true;
            }
            targetId = target.getUniqueId();
            targetName = target.getName();
        } else {
            Player player = checkPlayer(sender);
            if (player == null) return true;
            targetId = player.getUniqueId();
            targetName = player.getName();
        }

        net.flex.ManualTournaments.utils.tournament.stats.TournamentStats stats =
                net.flex.ManualTournaments.utils.tournament.stats.TournamentStats.getInstance();
        if (!stats.hasData(targetId)) {
            sender.sendMessage("§7No tournament stats for §e" + targetName + "§7.");
            return true;
        }
        sender.sendMessage("§6=== Stats: §e" + targetName + " §6===");
        sender.sendMessage("§7Tournaments: §e" + stats.getTournamentsPlayed(targetId));
        sender.sendMessage("§7Wins: §e" + stats.getWins(targetId));
        sender.sendMessage("§7Losses: §e" + stats.getLosses(targetId));
        sender.sendMessage("§7Win rate: §e" + stats.getWinRate(targetId) + "%");
        return true;
    }
}
