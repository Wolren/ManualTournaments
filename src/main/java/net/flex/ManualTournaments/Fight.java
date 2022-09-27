package net.flex.ManualTournaments;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Fight implements CommandExecutor {
    static final List<UUID> team1 = new ArrayList<>();
    static final List<UUID> team2 = new ArrayList<>();
    static final List<Player> temporary = new ArrayList<>();
    private static final Collection<String> team1String = new ArrayList<>();
    private static final Collection<String> team2String = new ArrayList<>();
    private static final FileConfiguration config = Main.getPlugin().getConfig();
    private static final Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    static final Team teamA = board.registerNewTeam(Main.conf("team1"));
    static final Team teamB = board.registerNewTeam(Main.conf("team2"));
    static File FightsConfigFile;
    static FileConfiguration FightsConfig;
    static int l;
    private static int o = config.getInt("fight-count");
    private final FileConfiguration KitsConfig = Main.getPlugin().KitsConfig;
    private final FileConfiguration ArenasConfig = Main.getPlugin().ArenaConfig;
    private final Collection<String> distinctElements = new java.util.ArrayList<>(Arrays.asList());

    static String teamList(Iterable<UUID> team, Collection<String> teamString) {
        for (UUID uuid : team) {
            teamString.add(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName());
        }
        return String.join(", ", teamString);
    }

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Main plugin = Main.getPlugin();
        config.load(plugin.customConfigFile);
        KitsConfig.load(plugin.KitsConfigfile);
        ArenasConfig.load(plugin.ArenaConfigFile);
        if (sender instanceof Player) {
            Player p = ((OfflinePlayer) sender).getPlayer();
            assert p != null;
            if (args.length == 0 || args.length == 1) {
                p.sendMessage(Main.conf("wrong-arguments"));
            } else if (args.length <= 100) {
                if (args.length % 2 == 0) {
                    distinctElements.clear();
                    team1.clear();
                    team2.clear();
                    team1String.clear();
                    team2String.clear();
                    teamA.setPrefix(Main.conf("team1-prefix"));
                    teamB.setPrefix(Main.conf("team2-prefix"));
                    teamA.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                    teamB.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                    if (!config.getBoolean("friendly-fire")) {
                        teamA.setAllowFriendlyFire(false);
                        teamB.setAllowFriendlyFire(false);
                    } else if (config.getBoolean("friendly-fire")) {
                        teamA.setAllowFriendlyFire(true);
                        teamB.setAllowFriendlyFire(true);
                    }
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.setScoreboard(board);
                    }
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] != null) {
                            Player fighter = Bukkit.getPlayer(args[i]);
                            if (fighter != null) {
                                distinctElements.add(Objects.requireNonNull(fighter).toString());
                                if (i < (args.length / 2)) {
                                    teamA.addEntry(fighter.getDisplayName());
                                    team1.add(fighter.getUniqueId());
                                } else if (i >= (args.length / 2)) {
                                    teamB.addEntry(fighter.getDisplayName());
                                    team2.add(fighter.getUniqueId());
                                }
                            }
                        }
                    }
                    if (distinctElements.size() == distinctElements.stream().distinct().collect(Collectors.toList()).size()) {
                        for (int i = 0; i < args.length; i++) {
                            if (args[i] != null) {
                                Player fighter = Bukkit.getPlayer(args[i]);
                                if (fighter != null) {
                                    if (plugin.arenaNames.contains(config.getString("current-arena"))) {
                                        if (plugin.kitNames.contains(config.getString("current-kit"))) {
                                            String path1 = "Arenas." + config.getString("current-arena") + "." + "pos1" + ".";
                                            String path2 = "Arenas." + config.getString("current-arena") + "." + "pos2" + ".";
                                            if (i < (args.length / 2)) {
                                                fighter.teleport(Arena.pathing(path1, ArenasConfig));
                                            } else if (i >= (args.length / 2)) {
                                                fighter.teleport(Arena.pathing(path2, ArenasConfig));
                                            }
                                            Kit(fighter);
                                        } else {
                                            p.sendMessage(Main.conf("current-kit-not-set"));
                                            return false;
                                        }
                                    } else {
                                        p.sendMessage(Main.conf("current-arena-not-set"));
                                        return false;
                                    }
                                    (new BukkitRunnable() {
                                        int i = config.getInt("countdown-time");

                                        public void run() {
                                            if (config.getBoolean("freeze-on-start")) {
                                                p.setWalkSpeed(0.0f);
                                                temporary.add(fighter);
                                            }
                                            if (i == 0) {
                                                if (config.getBoolean("freeze-on-start")) {
                                                    p.setWalkSpeed(0.2f);
                                                    temporary.clear();
                                                }
                                                fighter.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                                                cancel();
                                            } else {
                                                fighter.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                                            }

                                            --i;
                                        }
                                    }).runTaskTimer(plugin, 0L, 20L);
                                }
                            }
                        }
                        if (config.getBoolean("count-fights")) {
                            config.set("fight-count", ++o);
                        }
                        if (config.getBoolean("create-fights-folder")) {
                            l = 0;
                            MyListener.y = 0;
                            new BukkitRunnable() {

                                public void run() {
                                    if (MyListener.y == 1) {
                                        cancel();
                                    }
                                    l++;
                                }
                            }.runTaskTimer(plugin, 0L, 20L);
                            createFightsFolder(o);
                            FightsConfig.set("ArenaName", config.getString("current-arena"));
                            FightsConfig.set("KitName", config.getString("current-kit"));
                            String listString = teamList(team1, team1String);
                            FightsConfig.set("Team1", listString);
                            String listString2 = teamList(team2, team2String);
                            FightsConfig.set("Team2", listString2);
                            FightsConfig.set("Fight-duration", 0);
                        }
                        (new BukkitRunnable() {
                            int i = config.getInt("countdown-time");

                            public void run() {
                                if (i == 0) {
                                    Bukkit.broadcastMessage(Main.conf("fight-good-luck"));
                                    cancel();
                                } else {
                                    Bukkit.broadcastMessage(Main.conf("fight-will-start") + i + Main.conf("fight-will-start-seconds"));
                                }

                                --i;
                            }
                        }).runTaskTimer(plugin, 0L, 20L);
                    } else {
                        p.sendMessage(Main.conf("fight-duplicates"));
                        return false;
                    }
                } else {
                    p.sendMessage(Main.conf("wrong-arguments"));
                    return false;
                }
            } else {
                p.sendMessage(Main.conf("wrong-arguments"));
                return false;
            }
            if (config.getBoolean("create-fights-folder")) {
                FightsConfig.save(FightsConfigFile);
            }
            config.save(plugin.customConfigFile);
        } else {
            sender.sendMessage(Main.conf("sender-not-a-player"));
        }
        return false;
    }

    private void Kit(Player x) {
        if (Main.getPlugin().getConfig().getString("current-kit") != null) {
            Kit.getInstance().giveKit(x, Main.getPlugin().getConfig().getString("current-kit"));
        }
    }

    private void createFightsFolder(int i) {
        File fightsConfigFolder = new File(Main.getPlugin().getDataFolder(), "fights");
        if (!fightsConfigFolder.exists()) {
            fightsConfigFolder.mkdir();
        }
        FightsConfigFile = new File(Main.getPlugin().getDataFolder(), "fights/fight" + i + ".yml");
        FightsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(FightsConfigFile);
    }
}
