package net.flex.ManualTournaments.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class Queue implements TabCompleter, CommandExecutor {
    public static List<Player> playerQueue = new ArrayList<>();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        if (args.length == 0) {
            if (!playerQueue.contains(player)) {
                playerQueue.add(player);
                send(player, "queue-added");
            } else send(player, "queue-already-in");
        } else send(player, "queue-usage");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
