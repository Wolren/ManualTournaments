package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.fightCommands.NullFight;
import net.flex.ManualTournaments.commands.fightCommands.TeamFight;
import net.flex.ManualTournaments.interfaces.FightType;

import java.util.HashMap;
import java.util.Map;

public class FightFactory {

    public static final Map<String, FightType> fightTypesMap =  new HashMap<String, FightType>() {{
        put("TEAM", new TeamFight());
    }};

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

