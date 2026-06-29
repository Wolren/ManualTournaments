package net.flex.ManualTournaments.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.utils.tournament.Tournament;
import net.flex.ManualTournaments.utils.tournament.TournamentManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlaceholderHook extends PlaceholderExpansion {
    private final Main plugin;
    public PlaceholderHook(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mt";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Wolren";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.3.2";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * Supported placeholders:
     *   %mt_team_prefix%           — player's current scoreboard team prefix
     *   %mt_tournament_<name>_phase%  — phase of named tournament
     *   %mt_tournament_<name>_players%  — player count
     *   %mt_tournament_<name>_maxplayers%
     *   %mt_tournament_<name>_winner%
     *   %mt_tournament_<name>_round%   — current round number
     *   %mt_tournament_<name>_totalrounds%
     *   %mt_tournament_<name>_arena%
     *   %mt_tournament_<name>_kit%
     *   %mt_player_tournament%     — the tournament the player is registered in (or "")
     *   %mt_player_eliminated%     — "true"/"false"
     */
    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // Legacy placeholder
        if (identifier.equals("team_prefix")) {
            Scoreboard scoreboard = player.getScoreboard();
            for (Team team : scoreboard.getTeams()) {
                if (team.hasEntry(player.getName())) {
                    return team.getPrefix();
                }
            }
            return "";
        }

        // Tournament placeholders
        if (identifier.startsWith("tournament_")) {
            String rest = identifier.substring("tournament_".length());
            int underscoreIdx = rest.indexOf('_');
            if (underscoreIdx < 0) return "";
            String tName = rest.substring(0, underscoreIdx);
            String field = rest.substring(underscoreIdx + 1);

            Tournament t = TournamentManager.getInstance().getTournament(tName);
            if (t == null) return "NOT_FOUND";

            switch (field) {
                case "phase": return t.getPhase().name();
                case "players": return String.valueOf(t.getPlayerCount());
                case "maxplayers": return String.valueOf(t.getMaxPlayers());
                case "winner": {
                    UUID w = t.getWinner();
                    if (w == null) return "TBD";
                    String name = Bukkit.getOfflinePlayer(w).getName();
                    return name != null ? name : w.toString().substring(0, 8);
                }
                case "round": return String.valueOf(t.getActiveRound() + 1);
                case "totalrounds": return String.valueOf(t.getTotalRounds());
                case "arena": return t.getArenaName() != null ? t.getArenaName() : "";
                case "kit": return t.getKitName() != null ? t.getKitName() : "";
                case "paused": return t.isPaused() ? "true" : "false";
                default: return "";
            }
        }

        if (identifier.equals("player_tournament")) {
            for (Tournament t : TournamentManager.getInstance().getTournaments().values()) {
                if (t.getPlayers().contains(player.getUniqueId())
                        && t.getPhase() == Tournament.Phase.REGISTRATION) {
                    return t.getName();
                }
            }
            return "";
        }

        if (identifier.equals("player_eliminated")) {
            for (Tournament t : TournamentManager.getInstance().getTournaments().values()) {
                if (t.getPhase() == Tournament.Phase.IN_PROGRESS
                        && t.getPlayers().contains(player.getUniqueId())) {
                    // Check if they lost any match
                    boolean eliminated = t.getBracket().stream()
                            .flatMap(java.util.Collection::stream)
                            .filter(m -> m.containsPlayer(player.getUniqueId()) && m.isPlayed())
                            .anyMatch(m -> {
                                UUID w = m.getWinner();
                                return w != null && !w.equals(player.getUniqueId());
                            });
                    return eliminated ? "true" : "false";
                }
            }
            return "";
        }

        return "";
    }
}
