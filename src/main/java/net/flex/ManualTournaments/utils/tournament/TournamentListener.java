package net.flex.ManualTournaments.utils.tournament;

import net.flex.ManualTournaments.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Handles cleanup and reconnection for tournament spectators and participants.
 * Registered in Main.java on enable.
 */
public class TournamentListener implements Listener {

    /**
     * On quit: clean up spectator tracking for all tournaments.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        TournamentManager mgr = TournamentManager.getInstance();

        // Clean from all tournament spectator lists
        for (String tName : mgr.getTournamentNames()) {
            mgr.removeSpectator(tName, playerId);
        }

        // Check if player was in an active tournament match
        // The fight system handles forfeits via TeamFightListener.onLeave
        // No additional action needed — the death listener triggers match end
    }

    /**
     * On rejoin: notify if the player was in a tournament but is eliminated.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Check all in-progress tournaments for this player's elimination status
        for (Tournament t : TournamentManager.getInstance().getTournaments().values()) {
            if (t.getPhase() != Tournament.Phase.IN_PROGRESS) continue;
            if (!t.getPlayers().contains(player.getUniqueId())) continue;

            // Check if player was eliminated (lost a match)
            boolean eliminated = t.getBracket().stream()
                    .flatMap(java.util.Collection::stream)
                    .filter(m -> m.containsPlayer(player.getUniqueId()) && m.isPlayed())
                    .anyMatch(m -> {
                        UUID w = m.getWinner();
                        return w != null && !w.equals(player.getUniqueId());
                    });

            if (eliminated) {
                player.sendMessage("§7You were eliminated from §e" + t.getName() + "§7.");
            } else {
                // Check if player has a pending match
                boolean hasPending = t.getBracket().stream()
                        .flatMap(java.util.Collection::stream)
                        .anyMatch(m -> m.containsPlayer(player.getUniqueId()) && !m.isPlayed());
                if (hasPending) {
                    player.sendMessage("§aYou have a pending match in §e" + t.getName() + "§a. Wait for the next round.");
                }
            }
        }
    }
}
