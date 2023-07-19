package net.flex.ManualTournaments.commands.FightCommand;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;
import static net.flex.ManualTournaments.utils.SharedComponents.currentKit;

public class FightFactory {
    public FightType createFight(String type) {
        if (type.equalsIgnoreCase("team")) {
            if (TeamFight.team1.isEmpty() && TeamFight.team2.isEmpty()) {
                if (getPlugin().arenaNames.contains(currentArena)) {
                    if (getPlugin().kitNames.contains(currentKit)) {
                        return new TeamFight();
                    } else send(player, "current-kit-not-set");
                } else send(player, "current-arena-not-set");
            } else send(player, "fight-wrong-arguments");
        }
        return null;
    }
}
