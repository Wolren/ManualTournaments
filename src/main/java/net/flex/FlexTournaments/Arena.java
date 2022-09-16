package net.flex.FlexTournaments;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Arena implements TabCompleter, CommandExecutor {

    FileConfiguration config = Main.getPlugin().getConfig();
    private final FileConfiguration ArenasConfig = Main.getPlugin().ArenaConfig;

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        try {
            ArenasConfig.load(Main.getPlugin().ArenaConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.conf("sender-not-a-player"));
        } else {
            Player player = ((Player) sender).getPlayer();
            assert player != null;
            if (args.length == 0) {
                player.sendMessage(Main.conf("wrong-arguments"));
            } else if (args.length == 1) {
                if (args[0].equals("list")) {
                    player.sendMessage(Main.conf("arena-list") + Main.getPlugin().arenaNames.toString());
                }
            } else if (args.length == 2) {
                String arenaName = args[1];
                String path = "Arenas." + arenaName + ".";
                if (args[0].equals("create")) {
                    if (!Main.getPlugin().arenaNames.contains(args[1])) {
                        Main.getPlugin().getArenaConfig().set("Arenas", arenaName);
                        Main.getPlugin().arenaNames.add(arenaName);
                        player.sendMessage(Main.conf("arena-create"));
                    } else {
                        player.sendMessage(Main.conf("arena-already-exists"));
                    }
                } else if (args[0].equals("remove")) {
                    if (Main.getPlugin().arenaNames.contains(args[1])) {
                        ArenasConfig.set("Arenas." + arenaName, null);
                        Main.getPlugin().arenaNames.remove(arenaName);
                        player.sendMessage(Main.conf("arena-removed"));
                    } else {
                        player.sendMessage(Main.conf("arena-not-exists"));
                    }
                } else if (Main.getPlugin().arenaNames.contains(args[1])) {
                    switch (args[0]) {
                        case "pos1" -> {
                            String pathing = path + "pos1.";
                            getLocation(pathing, player, ArenasConfig);
                            player.sendMessage(Main.conf("arena-pos1"));
                        }
                        case "pos2" -> {
                            String pathing = path + "pos2.";
                            getLocation(pathing, player, ArenasConfig);
                            player.sendMessage(Main.conf("arena-pos2"));
                        }
                        case "spectator" -> {
                            String pathing = path + "spectator.";
                            getLocation(pathing, player, ArenasConfig);
                            player.sendMessage(Main.conf("arena-spectator"));
                        }
                        case "teleport" -> {
                            String l = path + "spectator.";
                            if (check(args[1])) {
                                player.teleport(pathing(l, ArenasConfig));
                            } else {
                                player.sendMessage(Main.conf("arena-not-set"));
                            }
                        }
                        case "validate" -> checkArena(player, args[1]);
                    }
                } else {
                    player.sendMessage(Main.conf("arena-not-exists"));
                }
            } else {
                player.sendMessage(Main.conf("wrong-arguments"));
            }
            try {
                Main.getPlugin().getArenaConfig().save(Main.getPlugin().ArenaConfigFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }

    public static void getLocation(String pathing, Player player, FileConfiguration cfg) {
        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        float yaw =  player.getLocation().getYaw();
        float pitch =  player.getLocation().getPitch();
        String world = Objects.requireNonNull(player.getLocation().getWorld()).getName();

        cfg.set(pathing + "x", x);
        cfg.set(pathing + "y", y);
        cfg.set(pathing + "z", z);
        cfg.set(pathing + "yaw", yaw);
        cfg.set(pathing + "pitch", pitch);
        cfg.set(pathing + "world", world);
    }

    public static Location pathing(String path, FileConfiguration cfg) {
        World world = Bukkit.getWorld(Objects.requireNonNull(cfg.get(path + "world")).toString());
        double x = cfg.getDouble(path + "x");
        double y = cfg.getDouble(path + "y");
        double z = cfg.getDouble(path + "z");
        float yaw = (float) cfg.getDouble(path + "yaw");
        float pitch = (float) cfg.getDouble(path + "pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private void checkArena(Player player, String arenaName) {
        String path = "Arenas." + arenaName + ".";
        if (ArenasConfig.isSet(path + "pos1") & ArenasConfig.isSet(path + "pos2")
                & ArenasConfig.isSet(path + "spectator")) {
            player.sendMessage(Main.conf("arena-set-correctly"));
        } else {
            if (!ArenasConfig.isSet(path + "pos1")) {
                player.sendMessage(Main.conf("arena-lacks-pos1"));
            }
            if (!ArenasConfig.isSet(path + "pos2")) {
                player.sendMessage(Main.conf("arena-lacks-pos2"));
            }
            if (!ArenasConfig.isSet(path + "spectator")) {
                player.sendMessage(Main.conf("arena-lacks-spectator"));
            }
        }
    }

    private Boolean check(String arenaName) {
        String path = "Arenas." + arenaName + ".";
        return ArenasConfig.isSet(path + "spectator");
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(List.of("create", "list", "pos1", "pos2", "remove", "spectator", "teleport", "validate"));
        } else if (args.length == 2) {
            List<String> b = new ArrayList<>();
            if (args[0].equals("create")) {
                b.add("(arena name)");
            } else if (args[0].equals("remove") || args[0].equals("pos1") || args[0].equals("pos2") || args[0].equals("spectator") || args[0].equals("teleport") || args[0].equals("validate")) {
                b.addAll(Main.getPlugin().arenaNames);
            }

            return b;
        } else {
            return List.of();
        }
    }
}


