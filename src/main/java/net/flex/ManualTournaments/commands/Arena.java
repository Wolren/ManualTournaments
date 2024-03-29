package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.factories.ArenaFactory;
import net.flex.ManualTournaments.factories.ArenaShortFactory;
import net.flex.ManualTournaments.guis.ArenaGUI;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class Arena implements CommandExecutor, TabCompleter {
    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null && !(sender instanceof ConsoleCommandSender)) return false;
        else player = optional(sender);
        config.load(getCustomConfigFile());
        getArenaConfig().load(getArenaConfigFile());
        if (args.length == 0) {
            new ArenaGUI().arenaGUI(player);
        } else if (args.length == 1) {
            ArenaShortFactory.getCommand(args[0].toUpperCase()).execute(player, args[0]);
        } else if (args.length == 2) {
            ArenaFactory.getCommand(args[0].toUpperCase()).execute(player, args[1], Main.arenaNames.contains(args[1]));
        } else send(player, "arena-usage");
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "gui", "list", "pos1", "pos2", "remove", "spectator", "teleport", "validate");
        else if (args.length == 2) {
            List<String> list = new ArrayList<>();
            if (args[0].equals("create")) list.add("(arena name)");
            else if (args[0].equals("remove") || args[0].equals("pos1") || args[0].equals("pos2") ||
                    args[0].equals("spectator") || args[0].equals("teleport") || args[0].equals("validate")) {
                list.addAll(Main.arenaNames);
            }
            return list;
        } else return Collections.emptyList();
    }
}
