package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.fightCommands.*;
import net.flex.ManualTournaments.interfaces.FightType;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FightFactory {

    public static final Map<String, FightType> fightTypesMap =  new HashMap<String, FightType>() {{
        put("TEAM", new TeamFight());
        put("TEAM_ARENA", new TeamArenaFight());
        put("FFA", new FfaFight());
        put("QUEUE", new QueueFight());
    }};

    public static FightType fight = new DefaultFight();
    public FightType createFight(String type, Map<Team, Set<UUID>> teams) {
        if (fightTypesMap.containsKey(type.toUpperCase())) {
            fight = fightTypesMap.get(type.toUpperCase());
        } else if (type.equalsIgnoreCase("STOP")) {
            fight.stopFight();
        }
        if (fight.canStartFight(type)) return fight;
        else return new DefaultFight();
    }
}

