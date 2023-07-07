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
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String string, @NotNull final String[] args) {
        Optional<Player> playerOptional = Optional.ofNullable(((OfflinePlayer) sender).getPlayer());
        if (!playerOptional.isPresent() || !(sender instanceof Player)) {
            sender.sendMessage("sender-not-a-player");
            return false;
        }
        loadConfigs();
        Player player = playerOptional.get();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("LIST")) player.sendMessage(Main.conf("arena-list") + arenas.toString());
            else return false;
        } else if (args.length == 2) {
            final String arenaName = args[1];
            final String path = "Arenas." + arenaName + ".";
            final String pathSpectator = path + "spectator.";
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
                        final String pathPos1 = path + "pos1.";
                        getLocation(pathPos1, player, ArenaConfig);
                        send(player, "arena-pos1");
                    } else send(player, "arena-not-exists");
                    break;
                case "POS2":
                    if (arenaExists) {
                        final String pathPos2 = path + "pos2.";
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

    static void getLocation(final String pathing, final Player player, final ConfigurationSection cfg) {
        final double x = player.getLocation().getX();
        final double y = player.getLocation().getY();
        final double z = player.getLocation().getZ();
        final float yaw = player.getLocation().getYaw();
        final float pitch = player.getLocation().getPitch();
        final String world = Objects.requireNonNull(player.getLocation().getWorld()).getName();
        cfg.set(pathing + "x", x);
        cfg.set(pathing + "y", y);
        cfg.set(pathing + "z", z);
        cfg.set(pathing + "yaw", yaw);
        cfg.set(pathing + "pitch", pitch);
        cfg.set(pathing + "world", world);
    }

    static Location location(final String path, final FileConfiguration cfg) {
        final World world = Bukkit.getWorld(Objects.requireNonNull(cfg.get(path + "world")).toString());
        final double x = cfg.getDouble(path + "x");
        final double y = cfg.getDouble(path + "y");
        final double z = cfg.getDouble(path + "z");
        final float yaw = (float) cfg.getDouble(path + "yaw");
        final float pitch = (float) cfg.getDouble(path + "pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private void checkArena(final Player p, final String arenaName) {
        final String path = "Arenas." + arenaName + ".";
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

    private static void send(final Player player, final String s) {
        player.sendMessage(Main.conf(s));
    }

    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, final String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "list", "pos1", "pos2", "remove", "spectator", "teleport", "validate");
        else if (args.length == 2) {
            final List<String> arrayList = new ArrayList<>();
            if (args[0].equals("create")) arrayList.add("(arena name)");
            else if (args[0].equals("remove") || args[0].equals("pos1") || args[0].equals("pos2") ||
                    args[0].equals("spectator") || args[0].equals("teleport") || args[0].equals("validate")) {
                arrayList.addAll(arenas);
            }
            return arrayList;
        } else return Collections.emptyList();
    }
}
