package net.flex.ManualTournaments.commands.FightCommand.Implementations;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.MyListener;
import net.flex.ManualTournaments.commands.FightCommand.FightType;
import net.flex.ManualTournaments.commands.KitCommand.Implementations.GiveKit;
import net.flex.ManualTournaments.utils.SqlMethods;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class TeamFight implements FightType {
    public static List<UUID> team1 = new ArrayList<>(), team2 = new ArrayList<>(), temporary = new ArrayList<>();
    public static Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    public static Team team1Board = board.registerNewTeam(message("team1")), team2Board = board.registerNewTeam(message("team2"));
    public static final Collection<String> team1String = new ArrayList<>(), team2String = new ArrayList<>();
    public static File FightsConfigFile;
    public static FileConfiguration FightsConfig;
    public static int duration, fightCount = config.getInt("fight-count");
    public static boolean cancelled = false;

    @SneakyThrows
    public boolean startFight(List<Player> fighters) {
        clearBeforeFight();
        setBoard();
        for (int i = 0; i < fighters.toArray().length; i++) {
            Player fighter = fighters.get(i);
            if (fighter == null) {
                send(player, "fighter-error");
                return false;
            }
            UUID fighterId = fighter.getUniqueId();
            fighter.setGameMode(GameMode.SURVIVAL);
            if (Main.version <= 13) collidableReflection(fighter);
            if (i < (fighters.toArray().length / 2)) {
                team1Board.addEntry(fighter.getDisplayName());
                team1.add(fighterId);
                fighter.teleport(location("Arenas." + currentArena + ".pos1.", getArenaConfig()));
            } else if (i >= (fighters.toArray().length / 2)) {
                team2Board.addEntry(fighter.getDisplayName());
                team2.add(fighterId);
                fighter.teleport(location("Arenas." + currentArena + ".pos2.", getArenaConfig()));
            }
            GiveKit.setKit(fighter, config.getString("current-kit"));
            if (config.getBoolean("freeze-on-start")) freezeOnStart(fighter, fighterId);
        }
        if (config.getBoolean("count-fights")) countFights();
        if (config.getBoolean("create-fights-folder")) createFightsFolder();
        if (config.getBoolean("mysql-enabled")) SqlMethods.sqlFights();
        if (config.getBoolean("freeze-on-start")) countdownBeforeFight();
        else if (config.getBoolean("fight-good-luck-enabled"))
            Bukkit.broadcastMessage(message("fight-good-luck"));
        return true;
    }

    public boolean stopFight() {
        player.setWalkSpeed(0.2F);
        removeEntries();
        cancelled = true;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (team1.contains(online.getUniqueId()) || team2.contains(online.getUniqueId())) {
                if (config.getBoolean("kill-on-fight-end")) online.setHealth(0);
                else if (!config.getBoolean("kill-on-fight-end")) {
                    String path = "fight-end-spawn.";
                    clear(online);
                    online.teleport(location(path, config));
                }
            }
        }
        team1.clear();
        team2.clear();
        team1String.clear();
        team2String.clear();
        return true;
    }

    private static void clearBeforeFight() {
        team1String.clear();
        team2String.clear();
        cancelled = false;
    }

    private void setBoard() {
        team1Board.setPrefix(message("team1-prefix"));
        team2Board.setPrefix(message("team2-prefix"));
        if (Main.version >= 14) {
            team1Board.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team2Board.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        if (!config.getBoolean("friendly-fire")) {
            team1Board.setAllowFriendlyFire(false);
            team2Board.setAllowFriendlyFire(false);
        } else if (config.getBoolean("friendly-fire")) {
            team1Board.setAllowFriendlyFire(true);
            team2Board.setAllowFriendlyFire(true);
        }
        for (Player online : Bukkit.getOnlinePlayers()) online.setScoreboard(board);
    }

    private void freezeOnStart(Player fighter, UUID fighterId) {
        (new BukkitRunnable() {
            int i = config.getInt("countdown-time");

            public void run() {
                temporary.add(fighterId);
                player.setWalkSpeed(0.0F);
                if (i == 0) {
                    temporary.clear();
                    player.setWalkSpeed(0.2F);
                    playSound(fighter);
                    cancel();
                } else {
                    if (cancelled) {
                        temporary.clear();
                        player.setWalkSpeed(0.2F);
                        cancel();
                    } else {
                        playNote(fighter);
                    }
                }

                --i;
            }
        }).runTaskTimer(getPlugin(), 0L, 20L);
    }

    @SneakyThrows
    private void countFights() {
        config.set("fight-count", ++fightCount);
        config.save(getPlugin().customConfigFile);
    }

    @SneakyThrows
    private void createFightsFolder() {
        duration = 0;
        MyListener.stopper = 0;
        new BukkitRunnable() {
            public void run() {
                if (MyListener.stopper == 1 || cancelled) cancel();
                else duration++;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
        createFightsFolder(fightCount);
        FightsConfig.set("team1", teamList(team1, team1String));
        FightsConfig.set("team2", teamList(team2, team2String));
        FightsConfig.set("damageTeam1", 0);
        FightsConfig.set("damageTeam2", 0);
        FightsConfig.set("regeneratedTeam1", 0);
        FightsConfig.set("regeneratedTeam2", 0);
        FightsConfig.set("arena", currentArena);
        FightsConfig.set("kit", currentKit);
        FightsConfig.set("duration", 0);
        FightsConfig.set("winners", "");
        FightsConfig.save(FightsConfigFile);
    }

    private void playSound(Player fighter) {
        if (Main.version >= 18)
            fighter.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        else
            fighter.playNote(player.getEyeLocation(), Instrument.PIANO, Note.sharp(0, Note.Tone.G));
    }

    private void playNote(Player fighter) {
        if (Main.version >= 18)
            fighter.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        else
            fighter.playNote(player.getEyeLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
    }

    private void countdownBeforeFight() {
        (new BukkitRunnable() {
            int i = config.getInt("countdown-time");

            public void run() {
                if (i == 0) {
                    if (config.getBoolean("fight-good-luck-enabled"))
                        Bukkit.broadcastMessage(message("fight-good-luck"));
                    cancel();
                } else if (cancelled) cancel();
                else
                    Bukkit.broadcastMessage(message("fight-will-start") + i + message("fight-will-start-seconds"));
                --i;
            }
        }).runTaskTimer(getPlugin(), 0L, 20L);
    }


    private static void createFightsFolder(int i) {
        File fightsConfigFolder = new File(getPlugin().getDataFolder(), "fights");
        if (!fightsConfigFolder.exists()) {
            boolean create = fightsConfigFolder.mkdir();
            if (!create) getPlugin().getLogger().log(Level.SEVERE, "Failed to create fights directory");
        }

        FightsConfigFile = new File(getPlugin().getDataFolder(), "fights/fight" + i + ".yml");
        FightsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(FightsConfigFile);
    }
}
