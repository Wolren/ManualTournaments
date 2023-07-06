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
    private static final FileConfiguration config = Main.getPlugin().getConfig();
    private final FileConfiguration ArenaConfig = Main.getPlugin().getArenaConfig();

    @SneakyThrows
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {
        loadConfigs();
        if (!(sender instanceof Player)) {
            sender.sendMessage("sender-not-a-player");
            return false;
        }
        Optional<Player> playerOptional = Optional.ofNullable(((OfflinePlayer) sender).getPlayer());
        if (!playerOptional.isPresent()) {
            return false;
        }
        Player p = playerOptional.get();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("LIST")) {
                p.sendMessage(Main.conf("arena-list") + Main.getPlugin().arenaNames.toString());
            } else return false;
        } else if (args.length == 2) {
            final String arenaName = args[1];
            final String path = "Arenas." + arenaName + ".";
            final String pathC = path + "spectator.";
            boolean arenaExists = Main.getPlugin().arenaNames.contains(arenaName);
            switch (args[0].toUpperCase()) {
                case "CREATE":
                    if (!arenaExists) {
                        Objects.requireNonNull(ArenaConfig.getConfigurationSection("Arenas")).createSection(arenaName);
                        Main.getPlugin().arenaNames.add(arenaName);
                        send(p, "arena-create");
                    } else send(p, "arena-already-exists");
                    break;
                case "REMOVE":
                    if (arenaExists) {
                        ArenaConfig.set("Arenas." + arenaName, null);
                        Main.getPlugin().arenaNames.remove(arenaName);
                        send(p, "arena-removed");
                    } else send(p, "arena-not-exists");
                    break;
                case "POS1":
                    if (arenaExists) {
                        final String pathA = path + "pos1.";
                        getLocation(pathA, p, ArenaConfig);
                        send(p, "arena-pos1");
                    } else send(p, "arena-not-exists");
                    break;
                case "POS2":
                    if (arenaExists) {
                        final String pathB = path + "pos2.";
                        getLocation(pathB, p, ArenaConfig);
                        send(p, "arena-pos2");
                    } else send(p, "arena-not-exists");
                    break;
                case "SPECTATOR":
                    if (arenaExists) {
                        getLocation(pathC, p, ArenaConfig);
                        send(p, "arena-spectator");
                    } else send(p, "arena-not-exists");
                    break;
                case "TELEPORT":
                    if (arenaExists) {
                        if (check(arenaName)) p.teleport(pathing(pathC, ArenaConfig));
                        else send(p, "arena-not-set");
                    } else send(p, "arena-not-exists");
                    break;
                case "VALIDATE":
                    if (arenaExists) checkArena(p, arenaName);
                    else send(p, "arena-not-exists");
                    break;
                default:
                    return false;
            }
            ArenaConfig.save(Main.getPlugin().ArenaConfigFile);
        } else return false;
        return true;
    }

    private void checkArena(final Player p, final String arenaName) {
        final String path = "Arenas." + arenaName + ".";
        if (ArenaConfig.isSet(path + "pos1") && ArenaConfig.isSet(path + "pos2") && ArenaConfig.isSet(path + "spectator")) {
            send(p, "arena-set-correctly");
        } else {
            if (!ArenaConfig.isSet(path + "pos1")) send(p, "arena-lacks-pos1");
            if (!ArenaConfig.isSet(path + "pos2")) send(p, "arena-lacks-pos2");
            if (!ArenaConfig.isSet(path + "spectator")) send(p, "arena-lacks-spectator");
        }
    }

    private Boolean check(final String arenaName) {
        return ArenaConfig.isSet("Arenas." + arenaName + "." + "spectator");
    }

    static void getLocation(final String pathing, final Player p, final ConfigurationSection cfg) {
        final double x = p.getLocation().getX();
        final double y = p.getLocation().getY();
        final double z = p.getLocation().getZ();
        final float yaw = p.getLocation().getYaw();
        final float pitch = p.getLocation().getPitch();
        final String world = Objects.requireNonNull(p.getLocation().getWorld()).getName();
        cfg.set(pathing + "x", x);
        cfg.set(pathing + "y", y);
        cfg.set(pathing + "z", z);
        cfg.set(pathing + "yaw", yaw);
        cfg.set(pathing + "pitch", pitch);
        cfg.set(pathing + "world", world);
    }

    @SneakyThrows
    private void loadConfigs() {
        config.load(Main.getPlugin().customConfigFile);
        ArenaConfig.load(Main.getPlugin().ArenaConfigFile);
    }

    static Location pathing(final String path, final FileConfiguration cfg) {
        final World world = Bukkit.getWorld(Objects.requireNonNull(cfg.get(path + "world")).toString());
        final double x = cfg.getDouble(path + "x");
        final double y = cfg.getDouble(path + "y");
        final double z = cfg.getDouble(path + "z");
        final float yaw = (float) cfg.getDouble(path + "yaw");
        final float pitch = (float) cfg.getDouble(path + "pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private static void send(final Player p, final String s) {
        p.sendMessage(Main.conf(s));
    }

    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, final String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "list", "pos1", "pos2", "remove", "spectator", "teleport", "validate");
        else if (args.length == 2) {
            final String a = args[0];
            final List<String> arr = new ArrayList<>();
            if (a.equals("create")) arr.add("(arena name)");
            else if (a.equals("remove") || a.equals("pos1") || a.equals("pos2") || a.equals("spectator") || a.equals("teleport") || a.equals("validate")) {
                arr.addAll(Main.getPlugin().arenaNames);
            }
            return arr;
        } else {
            return Collections.emptyList();
        }
    }
}
