package net.flex.ManualTournaments.commands.FightCommand;

import net.flex.ManualTournaments.commands.FightCommand.Implementations.TeamFight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FightFactory {

    public static final Map<String, FightType> fightTypesMap;

    static {
        fightTypesMap = new ConcurrentHashMap<>();
        fightTypesMap.put("TEAM", new TeamFight());
    }

    public static FightType fight = new NullFight();
    public FightType createFight(String type) {
        if (fightTypesMap.containsKey(type.toUpperCase())) {
            fight = fightTypesMap.get(type.toUpperCase());
        } else if (type.equalsIgnoreCase("STOP")) {
            fight.stopFight();
        }
        if (fight.canStartFight(type)) return fight;
        else return new NullFight();
    }
}

