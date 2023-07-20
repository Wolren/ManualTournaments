package net.flex.ManualTournaments.commands.FightCommand;

import org.bukkit.entity.Player;

import java.util.List;

public class NullFight implements FightType {
    @Override
    public void startFight(Player player, List<Player> players) {
    }

    @Override
    public void stopFight() {
    }

    @Override
    public boolean canStartFight(String type) {
        return false;
    }
}

