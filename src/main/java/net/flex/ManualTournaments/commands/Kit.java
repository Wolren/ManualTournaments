package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.KitCommand.KitFactory;
import net.flex.ManualTournaments.commands.KitCommand.Short.KitShortFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.flex.ManualTournaments.Main.getKitsConfig;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.optional;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class Kit implements TabCompleter, CommandExecutor {
    private static final FileConfiguration config = getPlugin().getConfig();
    private final List<String> kits = getPlugin().kitNames;
    Player player = null;

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        config.load(getPlugin().customConfigFile);
        getKitsConfig().load(getPlugin().KitsConfigfile);
        if (args.length == 1) {
            KitShortFactory.getCommand(args[0].toUpperCase()).execute(player, args[0]);
        } else if (args.length == 2) {
            KitFactory.getCommand(args[0].toUpperCase()).execute(player, args[1], getPlugin().kitNames.contains(args[1]));
        } else send(player, "kit-usage");
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) return new ArrayList<>(Arrays.asList("create", "give", "list", "remove", "unbreakable"));
        else if (args.length == 2) {
            List<String> arr = new ArrayList<>();
            if (args[0].equals("create")) arr.add("[name]");
            else if (args[0].equals("remove") || args[0].equals("give")) arr.addAll(kits);
            return arr;
        }
        return Collections.emptyList();
    }
}
