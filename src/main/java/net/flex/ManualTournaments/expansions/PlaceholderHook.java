package net.flex.ManualTournaments.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.flex.ManualTournaments.Main;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("team_prefix")) {
            Scoreboard scoreboard = player.getScoreboard();

            for (Team team : scoreboard.getTeams()) {
                if (team.hasEntry(player.getName())) {
                    return team.getPrefix();
                }
            }
        }

        return "";
    }
}

