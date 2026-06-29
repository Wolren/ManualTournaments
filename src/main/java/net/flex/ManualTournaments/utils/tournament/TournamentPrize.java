package net.flex.ManualTournaments.utils.tournament;

import net.flex.ManualTournaments.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Handles prize distribution for tournaments.
 * Prizes are command strings with {player} placeholder for the winner name.
 */
public class TournamentPrize {

    private TournamentPrize() {}

    /**
     * Distribute prizes to the tournament winner.
     * Each prize command is executed via the server console.
     */
    public static void distribute(Tournament tournament) {
        if (tournament.getPhase() != Tournament.Phase.FINISHED) return;
        UUID winnerId = tournament.getWinner();
        if (winnerId == null) return;

        List<String> commands = tournament.getPrizeCommands();
        if (commands.isEmpty()) return;

        String winnerName = Bukkit.getOfflinePlayer(winnerId).getName();
        if (winnerName == null) {
            winnerName = winnerId.toString().substring(0, 8);
        }

        Player onlineWinner = Bukkit.getPlayer(winnerId);
        boolean online = onlineWinner != null && onlineWinner.isOnline();

        Main.getPlugin().getLogger().info("[ManualTournaments] Distributing "
                + commands.size() + " prizes to tournament winner '" + winnerName + "'");

        for (String cmd : commands) {
            String resolved = cmd.replace("{player}", winnerName)
                    .replace("{winner}", winnerName)
                    .replace("{tournament}", tournament.getName());
            try {
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
                if (success) {
                    Main.getPlugin().getLogger().info("[Prize] Executed: /" + resolved);
                } else {
                    Main.getPlugin().getLogger().warning("[Prize] Failed: /" + resolved);
                }
            } catch (Exception e) {
                Main.getPlugin().getLogger().log(Level.WARNING,
                        "[Prize] Error executing: /" + resolved, e);
            }
        }

        // Notify winner
        if (online) {
            onlineWinner.sendMessage("§6§l=== PRIZES ===");
            onlineWinner.sendMessage("§7You received §e" + commands.size()
                    + " §7prize(s) for winning §e" + tournament.getName() + "§7!");
        }

        Bukkit.broadcastMessage("§6[Tournament] §e" + tournament.getName()
                + " §7winner §6§l" + winnerName + " §7received their prizes!");
    }
}
