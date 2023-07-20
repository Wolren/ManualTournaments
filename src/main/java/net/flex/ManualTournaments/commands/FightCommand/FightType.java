package net.flex.ManualTournaments.commands.FightCommand;

import org.bukkit.entity.Player;

import java.util.List;

public interface FightType {
    void startFight(List<Player> players);
    void stopFight();
}

