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
import net.flex.ManualTournaments.utils.SqlMethods;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class TeamArenaFight implements FightType {
    private FightContext context;
    private TeamFightListener listener;

    @SneakyThrows
    public void startFight(Player player, List<Player> fighters, String arenaName, Map<Team, Set<UUID>> teams, Scoreboard board) {
        context = new FightContext(teams, board);
        FightFactory.registerFight(this);
        listener = new TeamFightListener(this, context);
        TemporaryListener temporaryListener = new TemporaryListener(context.frozen);
        Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin());
        Bukkit.getPluginManager().registerEvents(temporaryListener, Main.getPlugin());
        Main.getPlugin().addFightListener(listener);
        Main.getPlugin().addTemporaryListener(temporaryListener);

        if (fighters.stream().anyMatch(Objects::isNull)) {
            send(player, "fighter-error");
            return;
        }
        context.distinctFighters.addAll(fighters);
        if (context.distinctFighters.size() == fighters.size() && fighters.size() % 2 == 0 && canStartHelper(arenaName, player)) {
            clearBeforeFight(board);
            context.team1 = board.registerNewTeam("1");
            context.team2 = board.registerNewTeam("2");
            setBoard(context.team1);
            setBoard(context.team2);
            getPlugin().reloadConfig();
            config = getPlugin().getConfig();
            fighters.forEach(fighter -> setupFighter(fighter, fighters, arenaName, teams));
            if (config.getBoolean("count-fights")) countFights();
            if (config.getBoolean("create-fights-folder")) setFightsFolder(arenaName);
            if (config.getBoolean("mysql-enabled")) SqlMethods.sqlFights(context.teamList("1"), context.teamList("2"), context.duration, arenaName, config.getString("current-kit"));
            if (config.getBoolean("freeze-on-start")) countdownBeforeFight(fighters);
            else if (config.getBoolean("fight-good-luck-enabled")) {
                Bukkit.broadcastMessage(message("fight-good-luck"));
            }
            Bukkit.getOnlinePlayers().forEach(online -> online.setScoreboard(board));
        }
    }

    private void clearBeforeFight(Scoreboard board) {
        board.getTeams().forEach(Team::unregister);
        context.cancelled.set(false);
        context.distinctFighters.clear();
    }

    @SneakyThrows
    private void setupFighter(Player fighter, List<Player> fighters, String arenaName, Map<Team, Set<UUID>> teams) {
        fighter.setGameMode(GameMode.SURVIVAL);
        Spectate.stopWithoutKill(fighter);
        Team team = fighters.indexOf(fighter) < (fighters.size() / 2) ? context.team1 : context.team2;
        team.addEntry(fighter.getName());
        teams.computeIfAbsent(team, k -> new HashSet<>()).add(fighter.getUniqueId());
        fighter.teleport(location("Arenas." + arenaName + ".pos" + (team == context.team1 ? "1" : "2") + ".", getArenaConfig()));
        GiveKit.setKit(fighter, config.getString("current-kit"));
        if (config.getBoolean("freeze-on-start")) {
            freezeOnStart(fighter, fighter.getUniqueId());
        }
    }

    private void setBoard(Team team) {
        if (team.getName().equals("1")) team.setPrefix(message("team1-prefix"));
        else if (team.getName().equals("2")) team.setPrefix(message("team2-prefix"));
        if (Main.version >= 14) team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        if (!config.getBoolean("friendly-fire")) team.setAllowFriendlyFire(false);
        else if (config.getBoolean("friendly-fire")) team.setAllowFriendlyFire(true);
    }

    @SneakyThrows
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
        if (config.getBoolean("create-fights-folder") && context.fightsConfig != null && !context.fightsConfig.isSet("cancelled")) {
            context.fightsConfig.set("cancelled", true);
            context.fightsConfig.save(context.fightsConfigFile);
        }
        if (listener != null) listener.triggerBlockResetAsync();
        FightFactory.unregisterFight(this);
    }

    public boolean canStartHelper(String arenaName, Player sender) {
        String path = "Arenas." + arenaName + ".";
        boolean pos1 = getArenaConfig().isSet(path + "pos1");
        boolean pos2 = getArenaConfig().isSet(path + "pos2");
        boolean spectator = getArenaConfig().isSet(path + "spectator");
        if (pos1 && pos2 && spectator) return true;
        else {
            if (!pos1) send(sender, "arena-lacks-pos1");
            if (!pos2) send(sender, "arena-lacks-pos2");
            if (!spectator) send(sender, "arena-lacks-spectator");
        }
        return false;
    }

    @Override
    public boolean canStartFight(String type, Player sender) {
        if (Main.kitNames.contains(config.getString("current-kit"))) {
            if (type.equalsIgnoreCase("team_arena")) {
                return true;
            }
        } else send(sender, "current-kit-not-set");
        return false;
    }

    @SneakyThrows
    private void setFightsFolder(String arenaName) {
        context.duration = 0;
        context.stopper = true;
        new BukkitRunnable() {
            public void run() {
                if (!context.stopper || context.cancelled.get()) cancel();
                else context.duration++;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
        context.createFightsFolder();
        context.fightsConfig.set("team1", context.teamList("1"));
        context.fightsConfig.set("team2", context.teamList("2"));
        context.fightsConfig.set("damageTeam1", 0);
        context.fightsConfig.set("damageTeam2", 0);
        context.fightsConfig.set("regeneratedTeam1", 0);
        context.fightsConfig.set("regeneratedTeam2", 0);
        context.fightsConfig.set("arena", arenaName);
        context.fightsConfig.set("kit", config.getString("current-kit"));
        context.fightsConfig.set("duration", 0);
        context.fightsConfig.set("winners", "");
        context.fightsConfig.save(context.fightsConfigFile);
    }


    @Override
    public FightContext getContext() {
        return context;
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
                        String soundName = "captcha_" + countdownTime;
                        fighter.playSound(fighter.getLocation(), soundName, SoundCategory.PLAYERS, 1f, 1f);
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

    static void playSound(Player fighter, int countdownTime) {
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
