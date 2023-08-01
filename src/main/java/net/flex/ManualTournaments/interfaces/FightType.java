package net.flex.ManualTournaments.interfaces;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public interface FightType {
    AtomicBoolean cancelled = new AtomicBoolean(false);
    @SneakyThrows
    void startFight(Player player, List<Player> fighters);
    @SneakyThrows
    void stopFight();
    boolean canStartFight(String type);
}

