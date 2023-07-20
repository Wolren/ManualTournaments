package net.flex.ManualTournaments.commands.FightCommand;

import org.bukkit.entity.Player;

import java.util.List;

public interface FightType {
    void startFight(Player player, List<Player> fighters);
    void stopFight();
    boolean canStartFight(String type);
}

