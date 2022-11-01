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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Settings implements TabCompleter, CommandExecutor {
    private static final FileConfiguration config = Main.getPlugin().getConfig();

    @SneakyThrows
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {
        config.load(Main.getPlugin().customConfigFile);
        if (!(sender instanceof Player)) sender.sendMessage(Main.conf("sender-not-a-player"));
        else {
            final Player p = ((OfflinePlayer) sender).getPlayer();
            assert p != null;
            if (args.length == 0) return false;
            else if (args.length == 1) {
                if (args[0].equals("endspawn")) {
                    final String path = "fight-end-spawn.";
                    Arena.getLocation(path, p, config);
                    send(p, "config-updated-successfully");
                }
            } else if (args.length == 2) {
                switch (args[0]) {
                    case "drop_items":
                        if (args[1].equals("true")) {
                            config.set("drop-items", true);
                            send(p, "config-updated-successfully");
                        } else if (args[1].equals("false")) {
                            config.set("drop-items", false);
                            send(p, "config-updated-successfully");
                        } else send(p, "config-options");
                        break;
                    case "break_blocks":
                        if (args[1].equals("true")) {
                            config.set("break-blocks", true);
                            send(p, "config-updated-successfully");
                        } else if (args[1].equals("false")) {
                            config.set("break-blocks", false);
                            send(p, "config-updated-successfully");
                        } else send(p, "config-options");
                        break;
                    case "place_blocks":
                        if (args[1].equals("true")) {
                            config.set("place_blocks", true);
                            send(p, "config-updated-successfully");
                        } else if (args[1].equals("false")) {
                            config.set("place_blocks", false);
                            send(p, "config-updated-successfully");
                        } else send(p, "config-options");
                        break;
                    case "friendly_fire":
                        if (args[1].equals("true")) {
                            config.set("friendly-fire", true);
                            send(p, "config-updated-successfully");
                        } else if (args[1].equals("false")) {
                            config.set("friendly-fire", false);
                            send(p, "config-updated-successfully");
                        } else send(p, "config-options");
                        break;
                    case "drop_on_death":
                        if (args[1].equals("true")) {
                            config.set("drop-on-death", true);
                            send(p, "config-updated-successfully");
                        } else if (args[1].equals("false")) {
                            config.set("drop-on-death", false);
                            send(p, "config-updated-successfully");
                        } else send(p, "config-options");
                        break;
                    case "kill_on_fight_end":
                        if (args[1].equals("true")) {
                            config.set("kill-on-fight-end", true);
                            send(p, "config-updated-successfully");
                        } else if (args[1].equals("false")) {
                            config.set("kill-on-fight-end", false);
                            send(p, "config-updated-successfully");
                        } else send(p, "config-options");
                        break;
                    case "freeze_on_start":
                        if (args[1].equals("true")) {
                            config.set("freeze-on-start", true);
                            send(p, "config-updated-successfully");
                        } else if (args[1].equals("false")) {
                            config.set("freeze-on-start", false);
                            send(p, "config-updated-successfully");
                        } else send(p, "config-options");
                        break;
                    case "current_arena":
                        if (Main.getPlugin().arenaNames.contains(args[1])) {
                            config.set("current-arena", args[1]);
                            send(p, "config-updated-successfully");
                        } else send(p, "arena-not-exists");
                        break;
                    case "current_kit":
                        if (Main.getPlugin().kitNames.contains(args[1])) {
                            config.set("current-kit", args[1]);
                            send(p, "config-updated-successfully");
                        } else send(p, "kit-not-exists");
                        break;
                    default:
                        return false;
                }
            } else return false;
            config.save(Main.getPlugin().customConfigFile);
        }
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {
        if (args.length == 1) {
            return Arrays.asList("break_blocks", "current_arena", "current_kit", "drop_items", "drop_on_death", "endspawn", "freeze_on_start", "friendly_fire", "kill_on_fight_end", "place_blocks");
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

    private static void send(final Player p, final String s) {
        p.sendMessage(Main.conf(s));
    }
}
