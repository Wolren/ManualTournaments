package net.flex.ManualTournaments.interfaces;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
public interface FightType {
    @SneakyThrows
    void startFight(Player player, List<Player> fighters, String arenaName, Map<Team, Set<UUID>> teams, Scoreboard board);
    @SneakyThrows
    void stopFight();
    boolean canStartFight(String type);
}

