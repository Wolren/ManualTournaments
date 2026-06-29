package net.flex.ManualTournaments.commands.fightCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.Spectate;
import net.flex.ManualTournaments.commands.kitCommands.GiveKit;
import net.flex.ManualTournaments.factories.FightFactory;
import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.listeners.TeamFightListener;
import net.flex.ManualTournaments.listeners.TemporaryListener;
import net.flex.ManualTournaments.utils.FightContext;
import net.flex.ManualTournaments.utils.SharedComponents;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class FfaFight implements FightType {
    private FightContext context;
    private TeamFightListener listener;

    @SneakyThrows
    @Override
    public void startFight(Player player, List<Player> fighters, String arenaName, Map<Team, Set<UUID>> teams, Scoreboard board) {
        context = new FightContext(teams, board);
        FightFactory.registerFight(this);
        listener = new TeamFightListener(this, context);
        TemporaryListener temporaryListener = new TemporaryListener(context.frozen);
        Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin());
        Bukkit.getPluginManager().registerEvents(temporaryListener, Main.getPlugin());
        Main.getPlugin().addFightListener(listener);
        Main.getPlugin().addTemporaryListener(temporaryListener);

        context.distinctFighters.addAll(fighters);
        if (context.distinctFighters.size() == fighters.size()) {
            clearBeforeFight(board);
            IntStream.range(0, fighters.size()).forEach(i -> {
                Player fighter = fighters.get(i);
                setupFighter(fighter, i, fighters.size(), teams, board);
            });
            if (config.getBoolean("count-fights")) countFights();
            if (config.getBoolean("freeze-on-start")) countdownBeforeFight(fighters);
            else if (config.getBoolean("fight-good-luck-enabled")) {
                Bukkit.broadcastMessage(message("fight-good-luck"));
            }
        }
    }

    private void clearBeforeFight(Scoreboard board) {
        board.getTeams().forEach(Team::unregister);
        context.cancelled.set(false);
        context.distinctFighters.clear();
    }

    @SneakyThrows
    private void setupFighter(Player fighter, int index, int total, Map<Team, Set<UUID>> teams, Scoreboard board) {
        Team team = board.registerNewTeam("Team " + fighter.getName());
        setBoard(team, fighter, board);
        teams.put(team, new HashSet<>(Collections.singleton(fighter.getUniqueId())));
        fighter.setGameMode(GameMode.SURVIVAL);
        Spectate.stopWithoutKill(fighter);
            getPlugin().reloadConfig();
            config = getPlugin().getConfig();
        Location center = location("Arenas." + config.getString("current-arena") + ".pos1.", getArenaConfig());
        double radius = 3.0;
        double angle = 2 * Math.PI * index / total;
        double newX = center.getX() + radius * Math.cos(angle);
        double newZ = center.getZ() + radius * Math.sin(angle);
        Location newLocation = new Location(center.getWorld(), newX, center.getY(), newZ);
        fighter.teleport(newLocation);
        GiveKit.setKit(fighter, config.getString("current-kit"));
        if (config.getBoolean("freeze-on-start")) {
            freezeOnStart(fighter, fighter.getUniqueId());
        }
    }


    private void setBoard(Team team, Player fighter, Scoreboard board) {
        if (Main.version >= 14) {
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        Bukkit.getOnlinePlayers().forEach(online -> online.setScoreboard(board));
        team.addEntry(fighter.getName());
    }

    @Override
    public void stopFight() {
        Bukkit.getServer().getOnlinePlayers().forEach(context::removeEntry);
        context.cancelled.set(true);
        Bukkit.getServer().getOnlinePlayers().stream().filter(online -> context.playerIsInTeam(online.getUniqueId())).forEach(online -> {
            online.setWalkSpeed(0.2F);
            if (version <= 13) collidableReflection(online, true);
            if (config.getBoolean("kill-on-fight-end")) online.setHealth(0);
            else if (!config.getBoolean("kill-on-fight-end")) {
                String path = "fight-end-spawn.";
                clear(online);
                online.teleport(location(path, config));
            }
        });
        if (listener != null) listener.triggerBlockResetAsync();
        FightFactory.unregisterFight(this);
    }

    @Override
    public boolean canStartFight(String type, Player sender) {
        if (Main.kitNames.contains(config.getString("current-kit"))) {
            if (Main.arenaNames.contains(config.getString("current-arena"))) {
                if (type.equalsIgnoreCase("ffa")) {
                        String path = "Arenas." + config.getString("current-arena") + ".";
                        boolean pos1 = getArenaConfig().isSet(path + "pos1");
                        boolean spectator = getArenaConfig().isSet(path + "spectator");
                        if (pos1 && spectator) return true;
                        else {
                            if (!pos1) send(sender, "arena-lacks-pos1");
                            if (!spectator) send(sender, "arena-lacks-spectator");
                        }
                }
            } else send(sender, "current-arena-not-set");
        } else send(sender, "current-kit-not-set");
        return false;
    }

    private void countdownBeforeFight(List<Player> fighters) {
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
                } else if (context.cancelled.get()) {
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
    private static void countFights() {
        int fightCount = config.getInt("fight-count");
        config.set("fight-count", ++fightCount);
        getPlugin().saveConfig();
    }

    private void freezeOnStart(Player fighter, UUID fighterId) {
        new BukkitRunnable() {
            int countdownTime = config.getInt("countdown-time");

            public void run() {
                context.frozen.add(fighterId);
                fighter.setWalkSpeed(0.0F);
                if (countdownTime == 0) {
                    context.frozen.remove(fighterId);
                    fighter.setWalkSpeed(0.2F);
                    playSound(fighter);
                    cancel();
                } else if (context.cancelled.get()) {
                    context.frozen.clear();
                    fighter.setWalkSpeed(0.2F);
                    cancel();
                } else playNote(fighter);

                countdownTime--;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
    }

    @Override
    public FightContext getContext() {
        return context;
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
