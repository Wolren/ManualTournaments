package net.flex.ManualTournaments.commands.fightCommands;

import net.flex.ManualTournaments.interfaces.FightType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultFight implements FightType {
    public final AtomicBoolean cancelled = new AtomicBoolean(false);
    @Override
    public void startFight(Player player, List<Player> players, String arenaName, Map<Team, Set<UUID>> teams, Scoreboard board) {
    }

    @Override
    public void stopFight() {
    }

    @Override
    public boolean canStartFight(String type) {
        return false;
    }
}

