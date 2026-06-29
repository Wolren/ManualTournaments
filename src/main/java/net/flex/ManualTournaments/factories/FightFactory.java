package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.fightCommands.*;
import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.utils.FightContext;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class FightFactory {
    private static final List<FightType> activeFights = new ArrayList<>();

    public FightType createFight(String type, Map<Team, Set<UUID>> teams, Player sender) {
        if (type.equalsIgnoreCase("STOP")) return null;
        FightType fight;
        switch (type.toUpperCase()) {
            case "TEAM":
                fight = new TeamFight();
                break;
            case "TEAM_ARENA":
                fight = new TeamArenaFight();
                break;
            case "FFA":
                fight = new FfaFight();
                break;
            case "QUEUE":
                fight = new QueueFight();
                break;
            default:
                fight = new DefaultFight();
        }
        if (fight.canStartFight(type, sender)) {
            activeFights.add(fight);
            return fight;
        }
        return new DefaultFight();
    }

    public static boolean isValidFightType(String type) {
        switch (type.toUpperCase()) {
            case "TEAM":
            case "TEAM_ARENA":
            case "FFA":
            case "QUEUE":
                return true;
            default:
                return false;
        }
    }

    public static void registerFight(FightType fight) {
        activeFights.add(fight);
    }

    public static void unregisterFight(FightType fight) {
        activeFights.remove(fight);
    }

    public static boolean isPlayerInAnyFight(UUID playerId) {
        for (FightType fight : activeFights) {
            FightContext ctx = fight.getContext();
            if (ctx != null && ctx.playerIsInTeam(playerId)) return true;
        }
        return false;
    }

    public static void stopAllFights() {
        for (FightType fight : new ArrayList<>(activeFights)) {
            fight.stopFight();
        }
    }
}
