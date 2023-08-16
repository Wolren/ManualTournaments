package net.flex.ManualTournaments.commands.fightCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.Fight;
import net.flex.ManualTournaments.commands.kitCommands.GiveKit;
import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.utils.SharedComponents;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.commands.Fight.teams;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class FfaFight implements FightType {
    private static final Set<Player> distinctFighters = new HashSet<>();

    @SneakyThrows
    @Override
    public void startFight(Player player, List<Player> fighters) {
        clearBeforeFight();
        distinctFighters.addAll(fighters);
        if (distinctFighters.size() == fighters.size()) {
            fighters.forEach(this::setupFighter);
            if (config.getBoolean("count-fights")) DefaultFight.countFights();
            if (config.getBoolean("freeze-on-start")) DefaultFight.countdownBeforeFight();
            else if (config.getBoolean("fight-good-luck-enabled")) {
                Bukkit.broadcastMessage(message("fight-good-luck"));
            }
        }
    }

    private void clearBeforeFight() {
        Fight.board.getTeams().forEach(Team::unregister);
        teams.clear();
        cancelled.set(false);
        distinctFighters.clear();
    }

    @SneakyThrows
    private void setupFighter(Player fighter) {
        Team team = Fight.board.registerNewTeam("Team " + fighter.getName());
        setBoard(team, fighter);
        teams.put(team, new HashSet<>(Collections.singleton(fighter.getUniqueId())));
        fighter.setGameMode(GameMode.SURVIVAL);
        if (version <= 13) collidableReflection(fighter, false);
        config.load(getCustomConfigFile());
        fighter.teleport(location("Arenas." + config.getString("current-arena") + ".pos1.", getArenaConfig()));
        GiveKit.setKit(fighter, config.getString("current-kit"));
        if (config.getBoolean("freeze-on-start")) {
            DefaultFight.freezeOnStart(fighter, fighter.getUniqueId());
        }
    }


    private void setBoard(Team team, Player fighter) {
        if (Main.version >= 14) {
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        Bukkit.getOnlinePlayers().forEach(online -> online.setScoreboard(Fight.board));
        team.addEntry(fighter.getDisplayName());
    }

    @Override
    public void stopFight() {
        player.setWalkSpeed(0.2F);
        Bukkit.getServer().getOnlinePlayers().forEach(SharedComponents::removeEntry);
        cancelled.set(true);
        Bukkit.getServer().getOnlinePlayers().stream().filter(online -> playerIsInTeam(online.getUniqueId())).forEach(online -> {
            if (version <= 13) collidableReflection(player, true);
            if (config.getBoolean("kill-on-fight-end")) online.setHealth(0);
            else if (!config.getBoolean("kill-on-fight-end")) {
                String path = "fight-end-spawn.";
                clear(online);
                online.teleport(location(path, config));
            }
        });
        teams.clear();
    }

    @Override
    public boolean canStartFight(String type) {
        if (Main.kitNames.contains(config.getString("current-kit"))) {
            if (Main.arenaNames.contains(config.getString("current-arena"))) {
                if (type.equalsIgnoreCase("ffa")) {
                    if (teams.values().stream().allMatch(Set::isEmpty)) {
                        String path = "Arenas." + config.getString("current-arena") + ".";
                        boolean pos1 = getArenaConfig().isSet(path + "pos1");
                        boolean spectator = getArenaConfig().isSet(path + "spectator");
                        if (pos1 && spectator) return true;
                        else {
                            if (!pos1) send(player, "arena-lacks-pos1");
                            if (!spectator) send(player, "arena-lacks-spectator");
                        }
                    } else send(player, "fight-concurrent");
                }
            } else send(player, "current-arena-not-set");
        } else send(player, "current-kit-not-set");
        return false;
    }
}
