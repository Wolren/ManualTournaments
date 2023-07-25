package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.factories.ArenaFactory;
import net.flex.ManualTournaments.factories.ArenaShortFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class Arena implements CommandExecutor, TabCompleter {
    public static final List<String> arenas = getPlugin().arenaNames;

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        getPlugin().getConfig().load(getPlugin().customConfigFile);
        getArenaConfig().load(getPlugin().ArenaConfigFile);
        if (args.length == 1) {
            ArenaShortFactory.getCommand(args[0].toUpperCase()).execute(player, args[0]);
        } else if (args.length == 2) {
            ArenaFactory.getCommand(args[0].toUpperCase()).execute(player, args[1], getPlugin().arenaNames.contains(args[1]));
        } else send(player, "arena-usage");
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "list", "pos1", "pos2", "remove", "spectator", "teleport", "validate");
        else if (args.length == 2) {
            List<String> arrayList = new ArrayList<>();
            if (args[0].equals("create")) arrayList.add("(arena name)");
            else if (args[0].equals("remove") || args[0].equals("pos1") || args[0].equals("pos2") ||
                    args[0].equals("spectator") || args[0].equals("teleport") || args[0].equals("validate")) {
                arrayList.addAll(getPlugin().arenaNames);
            }
            return arrayList;
        } else return Collections.emptyList();
    }
}
