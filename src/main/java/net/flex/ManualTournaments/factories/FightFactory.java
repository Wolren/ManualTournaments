package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.fightCommands.DefaultFight;
import net.flex.ManualTournaments.commands.fightCommands.FfaFight;
import net.flex.ManualTournaments.commands.fightCommands.QueueFight;
import net.flex.ManualTournaments.commands.fightCommands.TeamFight;
import net.flex.ManualTournaments.interfaces.FightType;

import java.util.HashMap;
import java.util.Map;

public class FightFactory {

    public static final Map<String, FightType> fightTypesMap =  new HashMap<String, FightType>() {{
        put("TEAM", new TeamFight());
        put("FFA", new FfaFight());
        put("QUEUE", new QueueFight());
    }};

    public static FightType fight = new DefaultFight();
    public FightType createFight(String type) {
        if (fightTypesMap.containsKey(type.toUpperCase())) {
            fight = fightTypesMap.get(type.toUpperCase());
        } else if (type.equalsIgnoreCase("STOP")) {
            fight.stopFight();
        }
        if (fight.canStartFight(type)) return fight;
        else return new DefaultFight();
    }
}

