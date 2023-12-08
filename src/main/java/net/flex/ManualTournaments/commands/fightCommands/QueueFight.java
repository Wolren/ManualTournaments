package net.flex.ManualTournaments.commands.fightCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.Fight;
import net.flex.ManualTournaments.commands.Queue;
import net.flex.ManualTournaments.commands.Spectate;
import net.flex.ManualTournaments.commands.kitCommands.GiveKit;
import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.listeners.TeamFightListener;
import net.flex.ManualTournaments.utils.SharedComponents;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.commands.Fight.teams;
import static net.flex.ManualTournaments.commands.fightCommands.TeamFight.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class QueueFight implements FightType {
    private static final Set<Player> distinctFighters = new HashSet<>();
    public static int duration;

    @SneakyThrows
    public void startFight(Player player, List<Player> fighters) {
        List<Player> queueFighters = Queue.playerQueue;
        if (queueFighters.stream().anyMatch(Objects::isNull)) {
            send(player, "fighter-error");
            return;
        }
        distinctFighters.addAll(queueFighters);
        if (distinctFighters.size() == queueFighters.size()) {
            clearBeforeFight();
            team1 = Fight.board.registerNewTeam("1");
            team2 = Fight.board.registerNewTeam("2");
            setBoard(team1);
            setBoard(team2);
            config.load(getCustomConfigFile());
            setupFighter(queueFighters);
            if (config.getBoolean("count-fights")) DefaultFight.countFights();
            if (config.getBoolean("create-fights-folder")) setFightsFolder();
            if (config.getBoolean("freeze-on-start")) DefaultFight.countdownBeforeFight();
            else if (config.getBoolean("fight-good-luck-enabled")) {
                Bukkit.broadcastMessage(message("fight-good-luck"));
            }
        }
    }

    private static void clearBeforeFight() {
        Fight.board.getTeams().forEach(Team::unregister);
        teams.clear();
        cancelled.set(false);
        distinctFighters.clear();
    }

    @SneakyThrows
    private void setupFighter(List<Player> fighters) {
        Collections.shuffle(fighters);
        for (Player fighter : fighters) {
            fighter.setGameMode(GameMode.SURVIVAL);
            Spectate.stopWithoutKill(fighter);
            Team team = fighters.indexOf(fighter) < (fighters.size() / 2) ? team1 : team2;
            team.addEntry(fighter.getName());
            teams.computeIfAbsent(team, k -> new HashSet<>()).add(fighter.getUniqueId());
            fighter.teleport(location("Arenas." + config.getString("current-arena") + ".pos" + (team == team1 ? "1" : "2") + ".", getArenaConfig()));
            GiveKit.setKit(fighter, config.getString("current-kit"));
            if (config.getBoolean("freeze-on-start")) {
                DefaultFight.freezeOnStart(fighter, fighter.getUniqueId());
            }
        }
    }

    private void setBoard(Team team) {
        if (team.getName().equals("1")) team.setPrefix(message("team1-prefix"));
        else if (team.getName().equals("2")) team.setPrefix(message("team2-prefix"));
        if (Main.version >= 14) team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        if (!config.getBoolean("friendly-fire")) team.setAllowFriendlyFire(false);
        else if (config.getBoolean("friendly-fire")) team.setAllowFriendlyFire(true);
        Bukkit.getOnlinePlayers().forEach(online -> online.setScoreboard(Fight.board));
    }

    @SneakyThrows
    public void stopFight() {
        Bukkit.getServer().getOnlinePlayers().forEach(SharedComponents::removeEntry);
        cancelled.set(true);
        Bukkit.getServer().getOnlinePlayers().stream().filter(online -> playerIsInTeam(online.getUniqueId())).forEach(online -> {
            online.setWalkSpeed(0.2F);
            if (version <= 13) collidableReflection(online, true);
            if (config.getBoolean("kill-on-fight-end")) online.setHealth(0);
            else if (!config.getBoolean("kill-on-fight-end")) {
                String path = "fight-end-spawn.";
                clear(online);
                online.teleport(location(path, config));
            }
        });
        teams.clear();
        if (config.getBoolean("create-fights-folder") && !FightsConfig.isSet("cancelled")) {
            FightsConfig.set("cancelled", true);
            FightsConfig.save(FightsConfigFile);
        }
    }

    @Override
    public boolean canStartFight(String type) {
        if (Main.kitNames.contains(config.getString("current-kit"))) {
            if (Main.arenaNames.contains(config.getString("current-arena"))) {
                if (type.equalsIgnoreCase("queue")) {
                    if (Queue.playerQueue.size() > 1) {
                        if (teams.values().stream().allMatch(Set::isEmpty)) {
                            String path = "Arenas." + config.getString("current-arena") + ".";
                            boolean pos1 = getArenaConfig().isSet(path + "pos1");
                            boolean pos2 = getArenaConfig().isSet(path + "pos2");
                            boolean spectator = getArenaConfig().isSet(path + "spectator");
                            if (pos1 && pos2 && spectator) return true;
                            else {
                                if (!pos1) send(player, "arena-lacks-pos1");
                                if (!pos2) send(player, "arena-lacks-pos2");
                                if (!spectator) send(player, "arena-lacks-spectator");
                            }
                        } else send(player, "fight-concurrent");
                    } else send(player, "fight-requirements");
                }
            } else send(player, "current-arena-not-set");
        } else send(player, "current-kit-not-set");
        return false;
    }

    @SneakyThrows
    private void setFightsFolder() {
        duration = 0;
        TeamFightListener.stopper = true;
        new BukkitRunnable() {
            public void run() {
                if (!TeamFightListener.stopper || cancelled.get()) cancel();
                else duration++;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
        createFightsFolder();
        FightsConfig.set("team1", teamList("1"));
        FightsConfig.set("team2", teamList("2"));
        FightsConfig.set("damageTeam1", 0);
        FightsConfig.set("damageTeam2", 0);
        FightsConfig.set("regeneratedTeam1", 0);
        FightsConfig.set("regeneratedTeam2", 0);
        FightsConfig.set("arena", config.getString("current-arena"));
        FightsConfig.set("kit", config.getString("current-kit"));
        FightsConfig.set("duration", 0);
        FightsConfig.set("winners", "");
        FightsConfig.save(FightsConfigFile);
    }
}
