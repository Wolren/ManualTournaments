package net.flex.ManualTournaments;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
public class Arena implements CommandExecutor, TabCompleter {
    private final FileConfiguration ArenasConfig = Main.getPlugin().ArenaConfig;

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.conf("sender-not-a-player"));
        } else {
            Player p = ((OfflinePlayer) sender).getPlayer();
            assert p != null;
            if (args.length == 0) {
                send(p, "wrong-arguments");
            } else if (args.length == 1) {
                if (args[0].equals("list")) {
                    p.sendMessage(Main.conf("arena-list") + Main.getPlugin().arenaNames.toString());
                }
            } else if (args.length == 2) {
                getArenasConfig().load(Main.getPlugin().ArenaConfigFile);
                String a = args[0];
                String arenaName = args[1];
                String path = "Arenas." + arenaName + ".";
                if (a.equals("create")) {
                    if (!Main.getPlugin().arenaNames.contains(arenaName)) {
                        Main.getPlugin().getArenaConfig().set("Arenas", arenaName);
                        Main.getPlugin().arenaNames.add(arenaName);
                        send(p, "arena-create");
                    } else {
                        send(p, "arena-already-exists");
                    }
                } else if (a.equals("remove")) {
                    if (Main.getPlugin().arenaNames.contains(arenaName)) {
                        getArenasConfig().set("Arenas." + arenaName, null);
                        Main.getPlugin().arenaNames.remove(arenaName);
                        send(p, "arena-removed");
                    } else {
                        send(p, "arena-not-exists");
                    }
                } else if (Main.getPlugin().arenaNames.contains(arenaName)) {
                    String pathC = path + "spectator.";
                    switch (a) {
                        case "pos1":
                            String pathA = path + "pos1.";
                            getLocation(pathA, p, getArenasConfig());
                            send(p, "arena-pos1");
                            break;
                        case "pos2":
                            String pathB = path + "pos2.";
                            getLocation(pathB, p, getArenasConfig());
                            send(p, "arena-pos2");
                            break;
                        case "spectator":
                            getLocation(pathC, p, getArenasConfig());
                            send(p, "arena-spectator");
                            break;
                        case "teleport":
                            if (check(arenaName)) {
                                p.teleport(pathing(pathC, getArenasConfig()));
                            } else {
                                send(p, "arena-not-set");
                            }
                            break;
                        case "validate":
                            checkArena(p, arenaName);
                            break;
                    }
                } else {
                    send(p, "arena-not-exists");
                }
            } else {
                send(p, "wrong-arguments");
            }
            Main.getPlugin().getArenaConfig().save(Main.getPlugin().ArenaConfigFile);
        }

        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "list", "pos1", "pos2", "remove", "spectator", "teleport", "validate");
        } else if (args.length == 2) {
            String a = args[0];
            List<String> arr = new ArrayList<>();
            if (a.equals("create")) {
                arr.add("(arena name)");
            } else if (a.equals("remove") || a.equals("pos1") || a.equals("pos2") || a.equals("spectator") || a.equals("teleport") || a.equals("validate")) {
                arr.addAll(Main.getPlugin().arenaNames);
            }

            return arr;
        } else {
            return Collections.emptyList();
        }
    }

    private void checkArena(CommandSender player, String arenaName) {
        String path = "Arenas." + arenaName + ".";
        if (getArenasConfig().isSet(path + "pos1") && getArenasConfig().isSet(path + "pos2") && getArenasConfig().isSet(path + "spectator")) {
            player.sendMessage(Main.conf("arena-set-correctly"));
        } else {
            if (!getArenasConfig().isSet(path + "pos1")) {
                player.sendMessage(Main.conf("arena-lacks-pos1"));
            }
            if (!getArenasConfig().isSet(path + "pos2")) {
                player.sendMessage(Main.conf("arena-lacks-pos2"));
            }
            if (!getArenasConfig().isSet(path + "spectator")) {
                player.sendMessage(Main.conf("arena-lacks-spectator"));
            }
        }
    }

    private Boolean check(String arenaName) {
        String path = "Arenas." + arenaName + ".";
        return getArenasConfig().isSet(path + "spectator");
    }

    static void getLocation(String pathing, Player p, ConfigurationSection cfg) {
        double x = p.getLocation().getX();
        double y = p.getLocation().getY();
        double z = p.getLocation().getZ();
        float yaw = p.getLocation().getYaw();
        float pitch = p.getLocation().getPitch();
        String world = Objects.requireNonNull(p.getLocation().getWorld()).getName();

        cfg.set(pathing + "x", x);
        cfg.set(pathing + "y", y);
        cfg.set(pathing + "z", z);
        cfg.set(pathing + "yaw", yaw);
        cfg.set(pathing + "pitch", pitch);
        cfg.set(pathing + "world", world);
    }

    static Location pathing(String path, FileConfiguration cfg) {
        World world = Bukkit.getWorld(Objects.requireNonNull(cfg.get(path + "world")).toString());
        double x = cfg.getDouble(path + "x");
        double y = cfg.getDouble(path + "y");
        double z = cfg.getDouble(path + "z");
        float yaw = (float) cfg.getDouble(path + "yaw");
        float pitch = (float) cfg.getDouble(path + "pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private static void send(Player p, String s) {
        p.sendMessage(Main.conf(s));
    }

    public FileConfiguration getArenasConfig() {
        return ArenasConfig;
    }
}
