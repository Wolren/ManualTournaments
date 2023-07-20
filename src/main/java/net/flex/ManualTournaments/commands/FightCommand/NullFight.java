package net.flex.ManualTournaments.commands.FightCommand;

import org.bukkit.entity.Player;

import java.util.List;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class NullFight implements FightType {
    @Override
    public void startFight(Player player, List<Player> players) {
        send(player, "fight-usage");
    }

    @Override
    public void stopFight() {

    }

    @Override
    public boolean canStartFight(String type) {
        return false;
    }
}

