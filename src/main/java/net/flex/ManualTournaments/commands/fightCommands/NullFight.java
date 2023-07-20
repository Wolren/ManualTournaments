package net.flex.ManualTournaments.commands.fightCommands;

import net.flex.ManualTournaments.interfaces.FightType;
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

