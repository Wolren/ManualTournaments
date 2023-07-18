package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedMethods.*;

public class Arena implements CommandExecutor, TabCompleter {
    private static final FileConfiguration config = getPlugin().getConfig();
    private final FileConfiguration ArenaConfig = getPlugin().getArenaConfig();
    private final List<String> arenas = getPlugin().arenaNames;
    Player player = null;

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        loadConfigs();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) player.sendMessage(message("arena-list") + arenas.toString());
            else return false;
        } else if (args.length == 2) {
            String arenaName = args[1];
            String path = "Arenas." + arenaName + ".";
            boolean arenaExists = arenas.contains(arenaName);
            switch (args[0].toUpperCase()) {
                case "CREATE":
                    if (!arenaExists) {
                        ArenaConfig.set("Arenas" + arenaName, null);
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
                        getLocation(path + "spectator.", player, ArenaConfig);
                        send(player, "arena-spectator");
                    } else send(player, "arena-not-exists");
                    break;
                case "TELEPORT":
                    if (arenaExists) {
                        if (ArenaConfig.isSet("Arenas." + arenaName + "." + "spectator"))
                            player.teleport(location(path + "spectator.", ArenaConfig));
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
            ArenaConfig.save(getPlugin().ArenaConfigFile);
        } else return false;
        return true;
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
        config.load(getPlugin().customConfigFile);
        ArenaConfig.load(getPlugin().ArenaConfigFile);
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
