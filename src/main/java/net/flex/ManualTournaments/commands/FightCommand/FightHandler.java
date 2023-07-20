package net.flex.ManualTournaments.commands.FightCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.FightCommand.Implementations.TeamFight;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class FightHandler {
    @SneakyThrows
    public FightType createFight(String type) {
        if (type.equalsIgnoreCase("team")) {
            if (TeamFight.team1.isEmpty() && TeamFight.team2.isEmpty()) {
                if (getPlugin().arenaNames.contains(config.getString("current-arena"))) {
                    if (getPlugin().kitNames.contains(config.getString("current-kit"))) {
                        return new TeamFight();
                    } else send(player, "current-kit-not-set");
                } else send(player, "current-arena-not-set");
            } else send(player, "fight-wrong-arguments");
        }
        return null;
    }
}
