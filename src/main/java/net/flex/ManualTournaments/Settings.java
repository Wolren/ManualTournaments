package net.flex.ManualTournaments;

import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Settings implements TabCompleter, CommandExecutor {
    private static final FileConfiguration config = Main.getPlugin().getConfig();

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        config.load(Main.getPlugin().customConfigFile);
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.conf("sender-not-a-player"));
        } else {
            Player player = ((OfflinePlayer) sender).getPlayer();
            assert player != null;
            if (args.length == 0) {
                player.sendMessage(Main.conf(config.getString("wrong-arguments")));
            } else if (args.length == 1) {
                if (args[0].equals("endspawn")) {
                    String path = "fight-end-spawn.";
                    Arena.getLocation(path, player, config);
                    player.sendMessage(Main.conf("config-updated-successfully"));
                }
            } else if (args.length == 2) {
                switch (args[0]) {
                    case "drop_items":
                        if (args[1].equals("true")) {
                            config.set("drop-items", true);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else if (args[1].equals("false")) {
                            config.set("drop-items", false);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else {
                            player.sendMessage(Main.conf("config-options"));
                        }
                        break;
                    case "break_blocks":
                        if (args[1].equals("true")) {
                            config.set("break-blocks", true);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else if (args[1].equals("false")) {
                            config.set("break-blocks", false);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else {
                            player.sendMessage(Main.conf("config-options"));
                        }
                        break;
                    case "friendly_fire":
                        if (args[1].equals("true")) {
                            config.set("friendly-fire", true);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else if (args[1].equals("false")) {
                            config.set("friendly-fire", false);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else {
                            player.sendMessage(Main.conf("config-options"));
                        }
                        break;
                    case "drop_on_death":
                        if (args[1].equals("true")) {
                            config.set("drop-on-death", true);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else if (args[1].equals("false")) {
                            config.set("drop-on-death", false);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else {
                            player.sendMessage(Main.conf("config-options"));
                        }
                        break;
                    case "kill_on_fight_end":
                        if (args[1].equals("true")) {
                            config.set("kill-on-fight-end", true);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else if (args[1].equals("false")) {
                            config.set("kill-on-fight-end", false);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else {
                            player.sendMessage(Main.conf("config-options"));
                        }
                        break;
                    case "freeze_on_start":
                        if (args[1].equals("true")) {
                            config.set("freeze-on-start", true);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else if (args[1].equals("false")) {
                            config.set("freeze-on-start", false);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else {
                            player.sendMessage(Main.conf("config-options"));
                        }
                        break;
                    case "current_arena":
                        if (Main.getPlugin().arenaNames.contains(args[1])) {
                            config.set("current-arena", args[1]);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else {
                            player.sendMessage(Main.conf("arena-not-exists"));
                        }
                        break;
                    case "current_kit":
                        if (Main.getPlugin().kitNames.contains(args[1])) {
                            config.set("current-kit", args[1]);
                            player.sendMessage(Main.conf("config-updated-successfully"));
                        } else {
                            player.sendMessage(Main.conf("kit-not-exists"));
                        }
                        break;
                }
            } else {
                player.sendMessage(Main.conf("wrong-arguments"));
            }
            try {
                config.save(Main.getPlugin().customConfigFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("break_blocks", "current_arena", "current_kit", "drop_items", "drop_on_death", "endspawn", "freeze_on_start", "friendly_fire", "kill_on_fight_end");
        } else if (args.length == 2) {
            switch (args[0]) {
                case "break_blocks":
                case "drop_items":
                case "drop_on_death":
                case "freeze_on_start":
                case "friendly_fire":
                case "kill_on_fight_end":
                    return Arrays.asList("true", "false");
                case "current_arena":
                    return new ArrayList<>(Main.getPlugin().arenaNames);
                case "current_kit":
                    return new ArrayList<>(Main.getPlugin().kitNames);
            }
        }
        return Collections.emptyList();
    }
}
