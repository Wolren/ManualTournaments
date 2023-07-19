package net.flex.ManualTournaments.commands.FightCommand;

import org.bukkit.entity.Player;

import java.util.List;

public interface FightType {
    boolean startFight(List<Player> players);
    boolean stopFight();
}

