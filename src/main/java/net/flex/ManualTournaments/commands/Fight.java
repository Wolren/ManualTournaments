package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.MyListener;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedMethods.*;
import static net.flex.ManualTournaments.utils.SqlMethods.sqlFights;

public class Fight implements CommandExecutor, TabCompleter {
    private static final FileConfiguration config = getPlugin().getConfig();
    private static final Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    public static final Collection<String> team1String = new ArrayList<>();
    public static final Collection<String> team2String = new ArrayList<>();
    private static final float WALK_SPEED_ZERO = 0.0F;
    private static final float WALK_SPEED_NORMAL = 0.2F;
    public static List<UUID> team1 = new ArrayList<>();
    public static List<UUID> team2 = new ArrayList<>();
    public static Team team1Board = board.registerNewTeam(message("team1"));
    public static Team team2Board = board.registerNewTeam(message("team2"));
    public static File FightsConfigFile;
    public static FileConfiguration FightsConfig;
    public static List<Player> temporary = new ArrayList<>();
    public static int duration;
    public static boolean cancelled = false;
    public static int fightCount = config.getInt("fight-count");
    private final FileConfiguration KitsConfig = getPlugin().getKitsConfig();
    private final FileConfiguration ArenaConfig = getPlugin().getArenaConfig();
    private final Collection<String> distinctElements = new java.util.ArrayList<>(Collections.emptyList());
    private final String currentArena = config.getString("current-arena");
    private final String currentKit = config.getString("current-kit");
    Player player = null;

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        loadConfigs();
        distinctElements.clear();
        for (String arg : args) {
            Player fighter = Bukkit.getPlayer(arg);
            if (fighter != null) distinctElements.add(fighter.toString());
        }
        if (args.length == 1 && args[0].equals("stop")) {
            player.setWalkSpeed(WALK_SPEED_NORMAL);
            removeEntries();
            cancelled = true;
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (team1.contains(online.getUniqueId()) || team2.contains(online.getUniqueId())) {
                    online.setHealth(0);
                } else if (!config.getBoolean("kill-on-fight-end")) {
                    String path = "fight-end-spawn.";
                    clear(online);
                    online.teleport(location(path, config));
                }
            }
            team1.clear();
            team2.clear();
            team1String.clear();
            team2String.clear();
            return true;
        } else if (args.length > 1 && args.length % 2 == 0 && distinctElements.stream().distinct().count() == args.length) {
            if (Fight.team1.isEmpty() && Fight.team2.isEmpty()) {
                team1String.clear();
                team2String.clear();
                cancelled = false;
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
                for (int i = 0; i < args.length; i++) {
                    Player fighter = Bukkit.getPlayer(args[i]);
                    if (fighter != null) {
                        if (getPlugin().arenaNames.contains(currentArena)) {
                            if (getPlugin().kitNames.contains(currentKit)) {
                                String pathPos1 = "Arenas." + currentArena + "." + "pos1" + ".";
                                String pathPos2 = "Arenas." + currentArena + "." + "pos2" + ".";
                                if (i < (args.length / 2)) {
                                    team1Board.addEntry(fighter.getDisplayName());
                                    team1.add(fighter.getUniqueId());
                                    fighter.teleport(location(pathPos1, ArenaConfig));
                                    fighter.setGameMode(GameMode.SURVIVAL);
                                } else if (i >= (args.length / 2)) {
                                    team2Board.addEntry(fighter.getDisplayName());
                                    team2.add(fighter.getUniqueId());
                                    fighter.teleport(location(pathPos2, ArenaConfig));
                                    fighter.setGameMode(GameMode.SURVIVAL);
                                }
                                Kit(fighter);
                            } else {
                                send(player, "current-kit-not-set");
                                return true;
                            }
                        } else {
                            send(player, "current-arena-not-set");
                            return true;
                        }
                        if (config.getBoolean("freeze-on-start")) {
                            (new BukkitRunnable() {
                                int i = config.getInt("countdown-time");

                                public void run() {
                                    temporary.add(fighter);
                                    player.setWalkSpeed(WALK_SPEED_ZERO);
                                    if (i == 0) {
                                        temporary.clear();
                                        player.setWalkSpeed(WALK_SPEED_NORMAL);
                                        if (Main.version >= 18)
                                            fighter.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                                        else
                                            fighter.playNote(player.getEyeLocation(), Instrument.PIANO, Note.sharp(0, Note.Tone.G));
                                        cancel();
                                    } else {
                                        if (cancelled) {
                                            temporary.clear();
                                            player.setWalkSpeed(WALK_SPEED_NORMAL);
                                            cancel();
                                        } else {
                                            if (Main.version >= 18)
                                                fighter.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                                            else
                                                fighter.playNote(player.getEyeLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
                                        }
                                    }

                                    --i;
                                }
                            }).runTaskTimer(getPlugin(), 0L, 20L);
                        }
                    } else {
                        send(player, "fighter-error");
                        return false;
                    }
                }
                if (config.getBoolean("count-fights")) config.set("fight-count", ++fightCount);
                if (config.getBoolean("mysql-enabled")) sqlFights();
                if (config.getBoolean("create-fights-folder")) {
                    duration = 0;
                    MyListener.stopper = 0;
                    new BukkitRunnable() {
                        public void run() {
                            if (MyListener.stopper == 1 || cancelled) cancel();
                            else duration++;
                        }
                    }.runTaskTimer(getPlugin(), 0L, 20L);
                    createFightsFolder(fightCount);
                    FightsConfig.set("arena-name", currentArena);
                    FightsConfig.set("kit-name", currentKit);
                    FightsConfig.set("team1", teamList(team1, team1String));
                    FightsConfig.set("team2", teamList(team2, team2String));
                    FightsConfig.set("Fight-duration", 0);
                }
                if (config.getBoolean("freeze-on-start")) {
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
                } else if (config.getBoolean("fight-good-luck-enabled"))
                    Bukkit.broadcastMessage(message("fight-good-luck"));
            } else send(player, "fight-concurrent-arenas");
        } else {
            send(player, "fight-wrong-arguments");
            return false;
        }
        if (config.getBoolean("create-fights-folder")) FightsConfig.save(FightsConfigFile);
        config.save(getPlugin().customConfigFile);
        return true;
    }

    private void createFightsFolder(int i) {
        File fightsConfigFolder = new File(getPlugin().getDataFolder(), "fights");
        if (!fightsConfigFolder.exists()) {
            boolean create = fightsConfigFolder.mkdir();
            if (!create) getPlugin().getLogger().log(Level.SEVERE, "Failed to create fights directory");
        }

        FightsConfigFile = new File(getPlugin().getDataFolder(), "fights/fight" + i + ".yml");
        FightsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(FightsConfigFile);
    }

    private void Kit(Player p) {
        if (config.getString("current-kit") != null) {
            Kit.setKit(p, config.getString("current-kit"));
        }
    }

    @SneakyThrows
    private void loadConfigs() {
        KitsConfig.load(getPlugin().KitsConfigfile);
        config.load(getPlugin().customConfigFile);
        ArenaConfig.load(getPlugin().ArenaConfigFile);
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("stop");
            for (Player online : Bukkit.getOnlinePlayers()) list.add(online.getDisplayName());
        } else for (Player online : Bukkit.getOnlinePlayers()) list.add(online.getDisplayName());
        return list;
    }
}
