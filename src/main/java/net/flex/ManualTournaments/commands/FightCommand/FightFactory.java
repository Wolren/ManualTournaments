package net.flex.ManualTournaments.commands.FightCommand;

public class FightFactory {
    public FightType createFight(String type) {
        if (type.equalsIgnoreCase("team")) {
            return new TeamFight();
        }
        return null;
    }
}
