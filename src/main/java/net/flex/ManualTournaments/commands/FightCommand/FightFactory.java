package net.flex.ManualTournaments.commands.FightCommand;

import net.flex.ManualTournaments.commands.FightCommand.Implementations.TeamFight;

public class FightFactory {

    public static FightType fight = new NullFight();
    public FightType createFight(String type) {
        switch (type.toLowerCase()){
            case "stop":
                fight.stopFight();
                break;
            case "team":
                fight = new TeamFight();
                break;
        }
        if (fight.canStartFight(type)) return fight;
        else return new NullFight();
    }
}

