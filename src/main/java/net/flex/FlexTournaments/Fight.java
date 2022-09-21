package net.flex.FlexTournaments;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Fight implements CommandExecutor {
    File FightsConfigFolder, FightsConfigFile;
    FileConfiguration FightsConfig;
    private static final FileConfiguration config = Main.getPlugin().getConfig();
    private static final ScoreboardManager manager = Bukkit.getScoreboardManager();
    private static final Scoreboard board;

    static {
        assert manager != null;
        board = manager.getNewScoreboard();
    }

    public static Team teamA = board.registerNewTeam(Main.conf("team1"));
    public static Team teamB = board.registerNewTeam(Main.conf("team2"));
    public static ArrayList<UUID> team1 = new ArrayList<>();
    public static ArrayList<UUID> team2 = new ArrayList<>();
    public static ArrayList<Player> temporary = new ArrayList<>();
    private final FileConfiguration KitsConfig = Main.getPlugin().KitsConfig;
    private final FileConfiguration ArenasConfig = Main.getPlugin().ArenaConfig;
    List<String> distinctElements = new java.util.ArrayList<>(List.of());

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        try {
            KitsConfig.load(Main.getPlugin().KitsConfigfile);
            ArenasConfig.load(Main.getPlugin().ArenaConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.conf("sender-not-a-player"));
        } else {
            Player player = ((Player) sender).getPlayer();
            assert player != null;
            if (args.length == 0 || args.length == 1) {
                player.sendMessage(Main.conf("wrong-arguments"));
            } else if (args.length <= 20) {
                if (args.length % 2 == 0) {
                    distinctElements.clear();
                    team1.clear();
                    team2.clear();
                    teamA.setPrefix(Main.conf("team1-prefix"));
                    teamB.setPrefix(Main.conf("team2-prefix"));
                    if (config.getBoolean("friendly-fire")) {
                        teamA.allowFriendlyFire();
                        teamB.allowFriendlyFire();
                    }
                    for (int i = 0; i < args.length; i++) {
                        if (!args[i].equals("null")) {
                            Player fighter = Bukkit.getPlayer(args[i]);
                            if (fighter != null) {
                                distinctElements.add(Objects.requireNonNull(fighter).toString());
                                if (i < (args.length / 2)) {
                                    teamA.addPlayer(fighter);
                                    team1.add(fighter.getUniqueId());
                                } else if (i >= (args.length / 2)) {
                                    teamB.addPlayer(fighter);
                                    team2.add(fighter.getUniqueId());
                                }
                            }
                        }
                    }
                    if (distinctElements.size() == distinctElements.stream().distinct().toList().size()) {
                        int o = config.getInt("fight-count");
                        createFightsFolder(o);
                        config.set("fight-count", ++o);
                        FightsConfig.set("ArenaName", config.getString("current-arena"));
                        for (int i = 0; i < args.length; i++) {
                            if (!args[i].equals("null")) {
                                Player fighter = Bukkit.getPlayer(args[i]);
                                if (fighter != null) {
                                    if (Main.getPlugin().arenaNames.contains(config.getString("current-arena"))) {
                                        if (Main.getPlugin().kitNames.contains(config.getString("current-kit"))) {
                                            String path1 = "Arenas." + config.getString("current-arena") + "." + "pos1" + ".";
                                            String path2 = "Arenas." + config.getString("current-arena") + "." + "pos2" + ".";
                                            if (i < (args.length / 2)) {
                                                fighter.teleport(Arena.pathing(path1, ArenasConfig));
                                            } else if (i >= (args.length / 2)) {
                                                fighter.teleport(Arena.pathing(path2, ArenasConfig));
                                            }
                                            Kit(fighter);
                                        } else {
                                            player.sendMessage(Main.conf("current-kit-not-set"));
                                            return false;
                                        }
                                    } else {
                                        player.sendMessage(Main.conf("current-arena-not-set"));
                                        return false;
                                    }
                                }
                                (new BukkitRunnable() {
                                    int i = config.getInt("countdown-time");

                                    public void run() {
                                        if (config.getBoolean("freeze-on-start")) {
                                            player.setWalkSpeed(0);
                                            temporary.add(fighter);
                                        }
                                        if (this.i == 0) {
                                            if (config.getBoolean("freeze-on-start")) {
                                                player.setWalkSpeed(0.2f);
                                                temporary.clear();
                                            }
                                            assert fighter != null;
                                            fighter.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                                            this.cancel();
                                        } else {
                                            assert fighter != null;
                                            fighter.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                                        }

                                        --this.i;
                                    }
                                }).runTaskTimer(Main.getPlugin(), 0L, 20L);
                            }
                        }
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            online.setScoreboard(board);
                        }
                        (new BukkitRunnable() {
                            int i = config.getInt("countdown-time");

                            public void run() {
                                if (this.i == 0) {
                                    Bukkit.broadcastMessage(Main.conf("fight-good-luck"));
                                    this.cancel();
                                } else {
                                    Bukkit.broadcastMessage(Main.conf("fight-will-start") + this.i + Main.conf("fight-will-start-seconds"));
                                }

                                --this.i;
                            }
                        }).runTaskTimer(Main.getPlugin(), 0L, 20L);
                    } else {
                        player.sendMessage(Main.conf("fight-duplicates"));
                    }
                } else {
                    player.sendMessage(Main.conf("wrong-arguments"));
                }
            } else {
                player.sendMessage(Main.conf("wrong-arguments"));
            }
            try {
                FightsConfig.save(FightsConfigFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private void Kit(Player x) {
        if (Main.getPlugin().getConfig().getString("current-kit") != null) {
            Kit.getInstance().giveKit(x, Main.getPlugin().getConfig().getString("current-kit"));
        }
    }

    private void createFightsFolder(int i) {
        FightsConfigFolder = new File(Main.getPlugin().getDataFolder(), "fights");
        if (!FightsConfigFolder.exists()) {
            FightsConfigFolder.mkdir();
        }

        FightsConfigFile = new File(Main.getPlugin().getDataFolder(),"fights/fight" + i + ".yml");
        FightsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(FightsConfigFile);
        if (!FightsConfigFile.exists()) {
            FightsConfigFile.getParentFile().mkdirs();
        }
    }
}

