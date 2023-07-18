package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedMethods.*;

public class Arena implements CommandExecutor, TabCompleter {
    public static final List<String> arenas = getPlugin().arenaNames;

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        config.load(getPlugin().customConfigFile);
        getArenaConfig().load(getPlugin().ArenaConfigFile);
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                player.sendMessage(message("arena-list") + arenas.toString());
            } else return false;
        } else if (args.length == 2) {
            ArenaCommandFactory.getCommand(args[0].toUpperCase()).execute(player, args[1], arenas.contains(args[1]));
            getArenaConfig().save(getPlugin().ArenaConfigFile);
        } else return false;
        return true;
    }

    public static class ArenaCommandFactory {
        private static final Map<String, ArenaCommand> arenaCommandMap;

        static {
            arenaCommandMap = new ConcurrentHashMap<>();
            arenaCommandMap.put("CREATE", new CreateArenaCommand());
            arenaCommandMap.put("POS1", new Pos1ArenaCommand());
            arenaCommandMap.put("POS2", new Pos2ArenaCommand());
            arenaCommandMap.put("REMOVE", new RemoveArenaCommand());
            arenaCommandMap.put("SPECTATOR", new SpectatorArenaCommand());
            arenaCommandMap.put("TELEPORT", new TeleportArenaCommand());
            arenaCommandMap.put("VALIDATE", new ValidateArenaCommand());
        }

        public static ArenaCommand getCommand(String command) {
            return arenaCommandMap.getOrDefault(command, (player, arenaName, arenaExists) -> {
            });
        }
    }

    public interface ArenaCommand {
        void execute(Player player, String arenaName, boolean arenaExists);
    }

    public static class CreateArenaCommand implements ArenaCommand {
        @SneakyThrows
        @Override
        public void execute(Player player, String arenaName, boolean arenaExists) {
            if (!arenaExists) {
                if (config.getString("current-arena") == null) config.set("current-arena", arenaName);
                config.save(getPlugin().customConfigFile);
                getArenaConfig().set("Arenas." + arenaName, null);
                getArenaConfig().save(getPlugin().ArenaConfigFile);
                arenas.add(arenaName);
                send(player, "arena-create");
            } else sendNotExists(player);
        }
    }

    public static class Pos1ArenaCommand implements ArenaCommand {
        @Override
        public void execute(Player player, String arenaName, boolean arenaExists) {
            if (arenaExists) {
                String pathPos1 = "Arenas." + arenaName + ".pos1.";
                getLocation(pathPos1, player, getArenaConfig());
                send(player, "arena-pos1");
            } else sendNotExists(player);
        }
    }

    public static class Pos2ArenaCommand implements ArenaCommand {
        @Override
        public void execute(Player player, String arenaName, boolean arenaExists) {
            if (arenaExists) {
                String pathPos2 = "Arenas." + arenaName + ".pos2.";
                getLocation(pathPos2, player, getArenaConfig());
                send(player, "arena-pos2");
            } else sendNotExists(player);
        }
    }

    public static class RemoveArenaCommand implements ArenaCommand {
        @Override
        public void execute(Player player, String arenaName, boolean arenaExists) {
            if (arenaExists) {
                getArenaConfig().set("Arenas." + arenaName, null);
                arenas.remove(arenaName);
                send(player, "arena-removed");
            } else sendNotExists(player);
        }
    }

    public static class SpectatorArenaCommand implements ArenaCommand {
        @Override
        public void execute(Player player, String arenaName, boolean arenaExists) {
            if (arenaExists) {
                getLocation("Arenas." + arenaName + ".spectator.", player, getArenaConfig());
                send(player, "arena-spectator");
            } else sendNotExists(player);
        }
    }

    public static class TeleportArenaCommand implements ArenaCommand {
        @Override
        public void execute(Player player, String arenaName, boolean arenaExists) {
            if (arenaExists) {
                if (getArenaConfig().isSet("Arenas." + arenaName + ".spectator")) {
                    player.teleport(location("Arenas." + arenaName + "." + "spectator.", getArenaConfig()));
                } else send(player, "arena-not-set");
            } else sendNotExists(player);
        }
    }

    public static class ValidateArenaCommand implements ArenaCommand {
        @Override
        public void execute(Player player, String arenaName, boolean arenaExists) {
            if (arenaExists) {
                String path = "Arenas." + arenaName + ".";
                boolean pos1 = getArenaConfig().isSet(path + "pos1");
                boolean pos2 = getArenaConfig().isSet(path + "pos2");
                boolean spectator = getArenaConfig().isSet(path + "spectator");
                if (pos1 && pos2 && spectator) send(player, "arena-set-correctly");
                else {
                    if (!pos1) send(player, "arena-lacks-pos1");
                    if (!pos2) send(player, "arena-lacks-pos2");
                    if (!spectator) send(player, "arena-lacks-spectator");
                }
            } else sendNotExists(player);
        }
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
