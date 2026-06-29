package net.flex.ManualTournaments.commands.fightCommands;

import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.utils.FightContext;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class DefaultFight implements FightType {
    @Override
    public void startFight(Player player, List<Player> players, String arenaName, Map<Team, Set<UUID>> teams, Scoreboard board) {
    }

    @Override
    public void stopFight() {
    }

    @Override
    public boolean canStartFight(String type, Player sender) {
        return false;
    }
}
