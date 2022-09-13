package net.flex.FlexTournaments;

import net.flex.FlexTournaments.api.Command;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Fight extends Command {
    public Fight() {
        super("ft_fight", "", "", "", "");
    }

    private static final FileConfiguration config = Main.getPlugin().getConfig();
    private final FileConfiguration KitsConfig = Main.getPlugin().KitsConfig;
    private final FileConfiguration ArenasConfig = Main.getPlugin().ArenaConfig;
    private static final ScoreboardManager manager = Bukkit.getScoreboardManager();
    private static final Scoreboard board;

    static {
        assert manager != null;
        board = manager.getNewScoreboard();
    }

    public static Team teamA = board.registerNewTeam((ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("team1")))));
    public static Team teamB = board.registerNewTeam((ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("team2")))));
    List<String> distinctElements = new java.util.ArrayList<>(List.of());

    public static ArrayList<UUID> players = new ArrayList<>();

    public static ArrayList<UUID> team1 = new ArrayList<>();

    public static ArrayList<UUID> team2 = new ArrayList<>();

    public boolean onExecute(CommandSender sender, String[] args) {
        try {
            KitsConfig.load(Main.getPlugin().KitsConfigfile);
            ArenasConfig.load(Main.getPlugin().ArenaConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("sender-not-a-player"))));
        } else {
            Player player = ((Player) sender).getPlayer();
            assert player != null;
            if (args.length == 0 || args.length == 1) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("wrong-arguments"))));
            } else if (args.length <= 20) {
                if (args.length % 2 == 0) {
                    distinctElements.clear();
                    teamA.setPrefix((ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("team1-prefix")))));
                    teamB.setPrefix((ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("team2-prefix")))));
                    for (int i = 0; i < args.length; i++) {
                        if (!args[i].equals("null")) {
                            Player fighter = Bukkit.getPlayer(args[i]);
                            if (fighter != null) {
                                players.add(fighter.getUniqueId());
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
                        for (int i = 0; i < args.length; i++) {
                            if (!args[i].equals("null")) {
                                Player fighter = Bukkit.getPlayer(args[i]);
                                if (fighter != null) {
                                    String path1 = "Arenas." + config.getString("current-arena") + "." + "pos1" + ".";
                                    String path2 = "Arenas." + config.getString("current-arena") + "." + "pos2" + ".";
                                    Kit(fighter);
                                    if (i < (args.length / 2)) {
                                        fighter.teleport(Arena.pathing(path1, ArenasConfig));
                                    } else if (i >= (args.length / 2)) {
                                        fighter.teleport(Arena.pathing(path2, ArenasConfig));
                                    }
                                    (new BukkitRunnable() {
                                        int i = config.getInt("countdown-time");
                                        public void run() {
                                            if (this.i == 0) {
                                                fighter.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                                                this.cancel();
                                            } else {
                                                fighter.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                                            }

                                            --this.i;
                                        }
                                    }).runTaskTimer(Main.getPlugin(), 0L, 20L);
                                }
                            }
                        }
                        for(Player online : Bukkit.getOnlinePlayers()){
                            online.setScoreboard(board);
                        }
                        (new BukkitRunnable() {
                            int i = config.getInt("countdown-time");

                            public void run() {
                                if (this.i == 0) {
                                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("fight-good-luck"))));
                                    this.cancel();
                                } else {
                                    Bukkit.broadcastMessage((ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("fight-will-start")))) + this.i + (ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("fight-will-start-seconds")))));
                                }

                                --this.i;
                            }
                        }).runTaskTimer(Main.getPlugin(), 0L, 20L);

                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("fight-duplicates"))));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("wrong-arguments"))));
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("wrong-arguments"))));
            }
        }
        return false;
    }

    private void Kit(Player x) {
        Kit.getInstance().giveKit(x, Main.getPlugin().getConfig().getString("current-kit"));
    }
}
