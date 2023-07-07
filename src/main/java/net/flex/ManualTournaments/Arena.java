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
    private final Main plugin = Main.getPlugin();
    private static final FileConfiguration config = Main.getPlugin().getConfig();
    private final FileConfiguration ArenaConfig = Main.getPlugin().getArenaConfig();
    private final List<String> arenas = Main.getPlugin().arenaNames;

    @SneakyThrows
    public boolean onCommand(@NotNull  CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        Optional<Player> playerOptional = Optional.ofNullable(((OfflinePlayer) sender).getPlayer());
        if (!playerOptional.isPresent() || !(sender instanceof Player)) {
            sender.sendMessage("sender-not-a-player");
            return false;
        }
        loadConfigs();
        Player player = playerOptional.get();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) player.sendMessage(Main.conf("arena-list") + arenas.toString());
            else return false;
        } else if (args.length == 2) {
            String arenaName = args[1];
            String path = "Arenas." + arenaName + ".";
            String pathSpectator = path + "spectator.";
            boolean arenaExists = arenas.contains(arenaName);
            switch (args[0].toUpperCase()) {
                case "CREATE":
                    if (!arenaExists) {
                        Objects.requireNonNull(ArenaConfig.getConfigurationSection("Arenas")).createSection(arenaName);
                        arenas.add(arenaName);
                        if (config.getString("current-arena") == null) config.set("current-arena", arenaName);
                        send(player, "arena-create");
                    } else send(player, "arena-already-exists");
                    break;
                case "REMOVE":
                    if (arenaExists) {
                        ArenaConfig.set("Arenas." + arenaName, null);
                        arenas.remove(arenaName);
                        send(player, "arena-removed");
                    } else send(player, "arena-not-exists");
                    break;
                case "POS1":
                    if (arenaExists) {
                        String pathPos1 = path + "pos1.";
                        getLocation(pathPos1, player, ArenaConfig);
                        send(player, "arena-pos1");
                    } else send(player, "arena-not-exists");
                    break;
                case "POS2":
                    if (arenaExists) {
                        String pathPos2 = path + "pos2.";
                        getLocation(pathPos2, player, ArenaConfig);
                        send(player, "arena-pos2");
                    } else send(player, "arena-not-exists");
                    break;
                case "SPECTATOR":
                    if (arenaExists) {
                        getLocation(pathSpectator, player, ArenaConfig);
                        send(player, "arena-spectator");
                    } else send(player, "arena-not-exists");
                    break;
                case "TELEPORT":
                    if (arenaExists) {
                        if (ArenaConfig.isSet("Arenas." + arenaName + "." + "spectator"))
                            player.teleport(location(pathSpectator, ArenaConfig));
                        else send(player, "arena-not-set");
                    } else send(player, "arena-not-exists");
                    break;
                case "VALIDATE":
                    if (arenaExists) checkArena(player, arenaName);
                    else send(player, "arena-not-exists");
                    break;
                default:
                    return false;
            }
            ArenaConfig.save(plugin.ArenaConfigFile);
        } else return false;
        return true;
    }

    static void getLocation(String pathing, Player player, ConfigurationSection cfg) {
        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        String world = Objects.requireNonNull(player.getLocation().getWorld()).getName();
        cfg.set(pathing + "x", x);
        cfg.set(pathing + "y", y);
        cfg.set(pathing + "z", z);
        cfg.set(pathing + "yaw", yaw);
        cfg.set(pathing + "pitch", pitch);
        cfg.set(pathing + "world", world);
    }

    static Location location(String path, FileConfiguration cfg) {
        World world = Bukkit.getWorld(Objects.requireNonNull(cfg.get(path + "world")).toString());
        double x = cfg.getDouble(path + "x");
        double y = cfg.getDouble(path + "y");
        double z = cfg.getDouble(path + "z");
        float yaw = (float) cfg.getDouble(path + "yaw");
        float pitch = (float) cfg.getDouble(path + "pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private void checkArena(Player p, String arenaName) {
        String path = "Arenas." + arenaName + ".";
        boolean pos1 = ArenaConfig.isSet(path + "pos1");
        boolean pos2 = ArenaConfig.isSet(path + "pos2");
        boolean spectator = ArenaConfig.isSet(path + "spectator");
        if (pos1 && pos2 && spectator) send(p, "arena-set-correctly");
        else {
            if (!pos1) send(p, "arena-lacks-pos1");
            if (!pos2) send(p, "arena-lacks-pos2");
            if (!spectator) send(p, "arena-lacks-spectator");
        }
    }

    @SneakyThrows
    private void loadConfigs() {
        config.load(plugin.customConfigFile);
        ArenaConfig.load(plugin.ArenaConfigFile);
    }

    private static void send(Player player, String s) {
        player.sendMessage(Main.conf(s));
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "list", "pos1", "pos2", "remove", "spectator", "teleport", "validate");
        else if (args.length == 2) {
            List<String> arrayList = new ArrayList<>();
            if (args[0].equals("create")) arrayList.add("(arena name)");
            else if (args[0].equals("remove") || args[0].equals("pos1") || args[0].equals("pos2") ||
                    args[0].equals("spectator") || args[0].equals("teleport") || args[0].equals("validate")) {
                arrayList.addAll(arenas);
            }
            return arrayList;
        } else return Collections.emptyList();
    }
}
