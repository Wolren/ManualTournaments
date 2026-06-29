package net.flex.ManualTournaments.interfaces;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public interface FightType {
    AtomicBoolean cancelled = new AtomicBoolean(false);

    void startFight(Player player, List<Player> fighters);

    void stopFight();

    boolean canStartFight(String type);
}
