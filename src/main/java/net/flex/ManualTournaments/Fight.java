package net.flex.ManualTournaments;

import lombok.SneakyThrows;
import org.bukkit.*;
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

public class Fight implements CommandExecutor {
    private final Main plugin = Main.getPlugin();
    private static final FileConfiguration config = Main.getPlugin().getConfig();
    private final FileConfiguration KitsConfig = Main.getPlugin().getKitsConfig();
    private final FileConfiguration ArenaConfig = Main.getPlugin().getArenaConfig();
    static File FightsConfigFile;
    static FileConfiguration FightsConfig;
    static final List<UUID> team1 = new ArrayList<>();
    static final List<UUID> team2 = new ArrayList<>();
    private static final Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    static final Team team1Board = board.registerNewTeam(Main.conf("team1"));
    static final Team team2Board = board.registerNewTeam(Main.conf("team2"));
    private static final Collection<String> team1String = new ArrayList<>();
    private static final Collection<String> team2String = new ArrayList<>();
    private final Collection<String> distinctElements = new java.util.ArrayList<>(Collections.emptyList());
    static final List<Player> temporary = new ArrayList<>();
    private final String currentArena = config.getString("current-arena");
    private final String currentKit = config.getString("current-kit");
    static int duration;
    private static int fightCount = config.getInt("fight-count");
    private static final float WALK_SPEED_ZERO = 0.0F;
    private static final float WALK_SPEED_NORMAL = 0.2F;

    @SneakyThrows
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String string, @NotNull final String[] args) {
        Optional<Player> playerOptional = Optional.ofNullable(((OfflinePlayer) sender).getPlayer());
        if (!playerOptional.isPresent() || !(sender instanceof Player)) {
            sender.sendMessage("sender-not-a-player");
            return false;
        }
        loadConfigs();
        Player player = playerOptional.get();
        distinctElements.clear();
        for (final String arg : args) {
            final Player fighter = Bukkit.getPlayer(arg);
            if (fighter != null) distinctElements.add(fighter.toString());
        }
        if (args.length > 1 && args.length % 2 == 0 && distinctElements.stream().distinct().count() == args.length) {
            team1.clear();
            team2.clear();
            team1String.clear();
            team2String.clear();
            team1Board.setPrefix(Main.conf("team1-prefix"));
            team2Board.setPrefix(Main.conf("team2-prefix"));
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
            for (final Player online : Bukkit.getOnlinePlayers()) online.setScoreboard(board);
            for (int i = 0; i < args.length; i++) {
                final Player fighter = Bukkit.getPlayer(args[i]);
                if (fighter != null) {
                    if (plugin.arenaNames.contains(currentArena)) {
                        if (plugin.kitNames.contains(currentKit)) {
                            final String pathPos1 = "Arenas." + currentArena + "." + "pos1" + ".";
                            final String pathPos2 = "Arenas." + currentArena + "." + "pos2" + ".";
                            if (i < (args.length / 2)) {
                                team1Board.addEntry(fighter.getDisplayName());
                                team1.add(fighter.getUniqueId());
                                fighter.teleport(Arena.location(pathPos1, ArenaConfig));
                                fighter.setGameMode(GameMode.SURVIVAL);
                            } else if (i >= (args.length / 2)) {
                                team2Board.addEntry(fighter.getDisplayName());
                                team2.add(fighter.getUniqueId());
                                fighter.teleport(Arena.location(pathPos2, ArenaConfig));
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
                                    if (Main.version >= 18)
                                        fighter.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                                    else
                                        fighter.playNote(player.getEyeLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
                                }

                                --i;
                            }
                        }).runTaskTimer(plugin, 0L, 20L);
                    }
                } else {
                    send(player, "fighter-error");
                    return false;
                }
            }
            if (config.getBoolean("count-fights")) config.set("fight-count", ++fightCount);
            if (config.getBoolean("create-fights-folder")) {
                duration = 0;
                MyListener.stopper = 0;
                new BukkitRunnable() {
                    public void run() {
                        if (MyListener.stopper == 1) cancel();
                        duration++;
                    }
                }.runTaskTimer(plugin, 0L, 20L);
                createFightsFolder(fightCount);
                FightsConfig.set("ArenaName", currentArena);
                FightsConfig.set("KitName", currentKit);
                FightsConfig.set("Team1", teamList(team1, team1String));
                FightsConfig.set("Team2", teamList(team2, team2String));
                FightsConfig.set("Fight-duration", 0);
            }
            if (config.getBoolean("freeze-on-start")) {
                (new BukkitRunnable() {
                    int i = config.getInt("countdown-time");

                    public void run() {
                        if (i == 0) {
                            Bukkit.broadcastMessage(Main.conf("fight-good-luck"));
                            cancel();
                        } else
                            Bukkit.broadcastMessage(Main.conf("fight-will-start") + i + Main.conf("fight-will-start-seconds"));
                        --i;
                    }
                }).runTaskTimer(plugin, 0L, 20L);
            } else Bukkit.broadcastMessage(Main.conf("fight-good-luck"));
        } else {
            send(player, "fight-wrong-arguments");
            return false;
        }
        if (config.getBoolean("create-fights-folder")) FightsConfig.save(FightsConfigFile);
        config.save(plugin.customConfigFile);
        return true;
    }

    private void createFightsFolder(final int i) {
        final File fightsConfigFolder = new File(plugin.getDataFolder(), "fights");
        if (!fightsConfigFolder.exists()) fightsConfigFolder.mkdir();
        FightsConfigFile = new File(plugin.getDataFolder(), "fights/fight" + i + ".yml");
        FightsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(FightsConfigFile);
    }

    private void Kit(final Player p) {
        if (config.getString("current-kit") != null) {
            Kit.setKit(p, config.getString("current-kit"));
        }
    }

    static String teamList(final Iterable<UUID> team, final Collection<String> teamString) {
        for (final UUID uuid : team) teamString.add(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName());
        return String.join(", ", teamString);
    }

    @SneakyThrows
    private void loadConfigs() {
        KitsConfig.load(plugin.KitsConfigfile);
        config.load(plugin.customConfigFile);
        ArenaConfig.load(plugin.ArenaConfigFile);
    }

    private static void send(final Player player, final String s) {
        player.sendMessage(Main.conf(s));
    }
}
