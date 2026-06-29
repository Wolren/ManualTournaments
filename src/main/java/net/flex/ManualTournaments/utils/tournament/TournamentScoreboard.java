package net.flex.ManualTournaments.utils.tournament;

import net.flex.ManualTournaments.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;

public class TournamentScoreboard {

    private static final Map<Tournament, BukkitTask> activeBoards = new HashMap<>();
    private static final Map<UUID, String> playerRoundStatus = new HashMap<>();

    private TournamentScoreboard() {}

    /**
     * Show a live tournament scoreboard to all participants and spectators.
     */
    public static void showBoard(Tournament tournament) {
        hideBoard(tournament);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (tournament.getPhase() == Tournament.Phase.FINISHED
                        || tournament.getPhase() == Tournament.Phase.CANCELLED) {
                    hideBoard(tournament);
                    return;
                }

                ScoreboardManager manager = Bukkit.getScoreboardManager();
                if (manager == null) return;
                Scoreboard board = manager.getNewScoreboard();
                Objective obj = board.registerNewObjective("tournament", "dummy",
                        ChatColor.GOLD + tournament.getName());
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);

                int score = 15;
                obj.getScore("§7").setScore(score--);
                obj.getScore("§7Phase: §e" + tournament.getPhase().name()).setScore(score--);
                int activeRound = tournament.getActiveRound() + 1;
                obj.getScore("§7Round: §e" + activeRound + "/" + tournament.getTotalRounds()).setScore(score--);
                obj.getScore("§7Players: §e" + tournament.getPlayerCount()).setScore(score--);
                if (tournament.isTeamTournament()) {
                    obj.getScore("§7Teams: §e" + tournament.getTeamCount() + "  §7Size: §e" + tournament.getTeamSize()).setScore(score--);
                }
                obj.getScore("§7 ").setScore(score--);

                if (tournament.getPhase() == Tournament.Phase.REGISTRATION) {
                    obj.getScore("§7Waiting for start...").setScore(score--);
                    obj.getScore("§e/tournament start").setScore(score--);
                } else if (tournament.getPhase() == Tournament.Phase.IN_PROGRESS) {
                    obj.getScore("§7Matches:").setScore(score--);
                    List<List<TournamentMatch>> bracket = tournament.getBracket();
                    int shown = 0;
                    for (int r = 0; r < bracket.size() && shown < 4; r++) {
                        for (TournamentMatch match : bracket.get(r)) {
                            if (shown >= 4) break;
                            if (match.getPlayer1() == null && match.getPlayer2() == null) continue;
                            String p1 = tournament.getParticipantDisplayName(match.getPlayer1());
                            String p2 = tournament.getParticipantDisplayName(match.getPlayer2());
                            String status;
                            if (match.isPlayed()) {
                                String w = tournament.getParticipantDisplayName(match.getWinner());
                                status = "§7" + w;
                            } else if (match.isBye()) {
                                status = "§7(auto)";
                            } else {
                                status = "§e⚔";
                            }
                            obj.getScore(p1 + " vs " + p2 + " " + status).setScore(score--);
                            shown++;
                        }
                    }
                }

                obj.getScore("§8  ").setScore(score--);
                obj.getScore("§7manualtournaments").setScore(score--);

                // Apply board only to tournament participants and spectators
                Set<UUID> seen = new HashSet<>();
                for (UUID uid : tournament.getPlayers()) {
                    Player p = Bukkit.getPlayer(uid);
                    if (p != null && p.isOnline()) {
                        p.setScoreboard(board);
                        seen.add(uid);
                    }
                }
                // Also show to active spectators of this tournament
                for (UUID uid : TournamentManager.getInstance().getSpectators(tournament.getName())) {
                    if (!seen.contains(uid)) {
                        Player p = Bukkit.getPlayer(uid);
                        if (p != null && p.isOnline()) {
                            p.setScoreboard(board);
                            seen.add(uid);
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(), 20L, getIntervalTicks());

        activeBoards.put(tournament, task);
    }

    private static int getIntervalTicks() {
        int secs = Main.getPlugin().getConfig().getInt("tournament-scoreboard-interval", 2);
        return Math.max(10, Math.min(200, secs)) * 20;
    }

    /**
     * Hide a tournament scoreboard.
     */
    public static void hideBoard(Tournament tournament) {
        BukkitTask task = activeBoards.remove(tournament);
        if (task != null) {
            task.cancel();
        }
        // Reset players' scoreboards
        for (UUID uid : tournament.getPlayers()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) {
                Scoreboard empty = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
                p.setScoreboard(empty);
            }
        }
    }

    public static void hideAll() {
        for (BukkitTask task : activeBoards.values()) {
            task.cancel();
        }
        activeBoards.clear();
    }

    private static String getName(UUID uuid) {
        if (uuid == null) return "?";
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if (name == null) return uuid.toString().substring(0, 8);
        return name.length() > 14 ? name.substring(0, 14) : name;
    }
}
