package net.flex.ManualTournaments.commands.fightCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.Queue;
import net.flex.ManualTournaments.commands.Spectate;
import net.flex.ManualTournaments.commands.kitCommands.GiveKit;
import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.listeners.TeamFightListener;
import net.flex.ManualTournaments.listeners.TemporaryListener;
import net.flex.ManualTournaments.utils.SharedComponents;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.commands.fightCommands.TeamFight.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class QueueFight implements FightType {
    public static final AtomicBoolean cancelled = new AtomicBoolean(false);
    private static final Set<Player> distinctFighters = new HashSet<>();
    public static int duration;

    @SneakyThrows
    public void startFight(Player player, List<Player> fighters, String arenaName, Map<Team, Set<UUID>> teams, Scoreboard board) {
        TeamFightListener listener = new TeamFightListener(this, cancelled, teams, board);
        TemporaryListener temporaryListener = new TemporaryListener(frozen);
        Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin());
        Bukkit.getPluginManager().registerEvents(temporaryListener, Main.getPlugin());
        Main.getPlugin().addFightListener(listener);
        Main.getPlugin().addTemporaryListener(temporaryListener);

        List<Player> queueFighters = Queue.playerQueue;
        if (queueFighters.stream().anyMatch(Objects::isNull)) {
            send(player, "fighter-error");
            return;
        }
        distinctFighters.addAll(queueFighters);
        if (distinctFighters.size() == queueFighters.size()) {
            clearBeforeFight(board);
            team1 = board.registerNewTeam("1");
            team2 = board.registerNewTeam("2");
            setBoard(team1, board);
            setBoard(team2, board);
            config.load(getCustomConfigFile());
            setupFighter(queueFighters, teams);
            if (config.getBoolean("count-fights")) countFights();
            if (config.getBoolean("create-fights-folder")) setFightsFolder();
            if (config.getBoolean("freeze-on-start")) countdownBeforeFight(queueFighters);
            else if (config.getBoolean("fight-good-luck-enabled")) {
                Bukkit.broadcastMessage(message("fight-good-luck"));
            }
        }
    }

    private static void clearBeforeFight(Scoreboard board) {
        board.getTeams().forEach(Team::unregister);
        cancelled.set(false);
        distinctFighters.clear();
    }

    @SneakyThrows
    private void setupFighter(List<Player> fighters, Map<Team, Set<UUID>> teams) {
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
                freezeOnStart(fighter, fighter.getUniqueId());
            }
        }
    }

    private void setBoard(Team team, Scoreboard board) {
        if (team.getName().equals("1")) team.setPrefix(message("team1-prefix"));
        else if (team.getName().equals("2")) team.setPrefix(message("team2-prefix"));
        if (Main.version >= 14) team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        if (!config.getBoolean("friendly-fire")) team.setAllowFriendlyFire(false);
        else if (config.getBoolean("friendly-fire")) team.setAllowFriendlyFire(true);
        Bukkit.getOnlinePlayers().forEach(online -> online.setScoreboard(board));
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

    public final Set<UUID> frozen = new HashSet<>();

    static void countdownBeforeFight(List<Player> fighters) {
        new BukkitRunnable() {
            int countdownTime = config.getInt("countdown-time");

            public void run() {
                if (countdownTime == 0) {
                    if (config.getBoolean("fight-good-luck-enabled")) {
                        for (Player fighter : fighters) {
                            fighter.sendMessage(message("fight-good-luck"));
                        }
                    }
                    cancel();
                } else if (cancelled.get()) {
                    cancel();
                } else {
                    for (Player fighter : fighters) {
                        fighter.sendMessage(String.format(message("fight-will-start"), countdownTime));
                    }
                }

                countdownTime--;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
    }

    @SneakyThrows
    static void countFights() {
        int fightCount = config.getInt("fight-count");
        config.set("fight-count", ++fightCount);
        config.save(getCustomConfigFile());
    }

    void freezeOnStart(Player fighter, UUID fighterId) {
        new BukkitRunnable() {
            int countdownTime = config.getInt("countdown-time");

            public void run() {
                frozen.add(fighterId);
                fighter.setWalkSpeed(0.0F);
                if (countdownTime == 0) {
                    frozen.remove(fighterId);
                    fighter.setWalkSpeed(0.2F);
                    playSound(fighter);
                    cancel();
                } else if (cancelled.get()) {
                    frozen.clear();
                    fighter.setWalkSpeed(0.2F);
                    cancel();
                } else playNote(fighter);

                countdownTime--;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
    }

    public boolean isPlayerFrozen(UUID playerId) {
        return frozen.contains(playerId);
    }

    static void playSound(Player fighter) {
        if (Main.version >= 18) {
            fighter.playSound(fighter.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        } else {
            fighter.playNote(fighter.getEyeLocation(), Instrument.PIANO, Note.sharp(0, Note.Tone.G));
        }
    }

    static void playNote(Player fighter) {
        if (Main.version >= 18) {
            fighter.playSound(fighter.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        } else {
            fighter.playNote(fighter.getEyeLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
        }
    }
}
