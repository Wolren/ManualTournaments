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
import java.util.stream.Collectors;

import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class Queue implements TabCompleter, CommandExecutor {
    public static List<Player> playerQueue = new ArrayList<>();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        Player player = optional(sender);
        if (player == null) return true;
        if (args.length == 1 && config.getBoolean("queue-allow")) {
            if (args[0].equals("join")) {
                if (!playerQueue.contains(player)) {
                    playerQueue.add(player);
                    send(player, "queue-added");
                } else send(player, "queue-already-in");
            } else if (args[0].equals("leave")) {
                if (playerQueue.contains(player)) {
                    playerQueue.remove(player);
                    send(player, "queue-removed");
                }
            } else if (args[0].equals("list")) {
                player.sendMessage(message("queue-list") + playerQueue.stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", ")));
            }
        } else send(player, "queue-usage");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("join");
            list.add("leave");
            list.add("list");
        } else list = Collections.emptyList();
        return list;
    }
}
