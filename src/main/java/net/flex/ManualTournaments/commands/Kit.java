package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.factories.KitFactory;
import net.flex.ManualTournaments.factories.KitShortFactory;
import net.flex.ManualTournaments.guis.KitGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class Kit implements TabCompleter, CommandExecutor {
    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        config.load(getCustomConfigFile());
        getKitConfig().load(getKitConfigFile());
        if (args.length == 0) {
            KitGUI.kitGUI(player);
        } else if (args.length == 1) {
            KitShortFactory.getCommand(args[0].toUpperCase()).execute(player, args[0]);
        } else if (args.length == 2) {
            KitFactory.getCommand(args[0].toUpperCase()).execute(player, args[1], Main.kitNames.contains(args[1]));
        } else send(player, "kit-usage");
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) return new ArrayList<>(Arrays.asList("create", "give", "gui", "list", "remove", "unbreakable"));
        else if (args.length == 2) {
            List<String> list = new ArrayList<>();
            if (args[0].equals("create")) list.add("[name]");
            else if (args[0].equals("remove") || args[0].equals("give")) list.addAll(kitNames);
            return list;
        }
        return Collections.emptyList();
    }
}
