package net.flex.ManualTournaments.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class TeamPrefix extends PlaceholderExpansion {
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
    public String onPlaceholderRequest(Player player, String identifier) {
        if(identifier.equals("team_prefix")) {
            Scoreboard scoreboard = player.getScoreboard();
            for (Team team : scoreboard.getTeams()) {
                if (team.hasEntry(player.getName())) {
                    return team.getPrefix();
                }
            }
        }
        return null;
    }
}
