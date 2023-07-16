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

import java.util.*;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.Shared.getLocation;
import static net.flex.ManualTournaments.utils.Shared.send;

public final class Settings implements TabCompleter, CommandExecutor {
    private static final FileConfiguration config = getPlugin().getConfig();

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        config.load(getPlugin().customConfigFile);
        Optional<Player> playerOptional = Optional.ofNullable(((OfflinePlayer) sender).getPlayer());
        if (!playerOptional.isPresent() || !(sender instanceof Player)) {
            sender.sendMessage("sender-not-a-player");
            return false;
        }
        Player player = playerOptional.get();
        if (args.length == 1 && args[0].equalsIgnoreCase("endspawn")) {
            getLocation("fight-end-spawn.", player, config);
            send(player, "config-updated-successfully");
        } else if (args.length == 2) {
            switch (args[0].toUpperCase()) {
                case "DROP_ITEMS":
                    updateConfigAndNotify(player, "drop-items", args[1]);
                    break;
                case "BREAK_BLOCKS":
                    updateConfigAndNotify(player, "break-blocks", args[1]);
                    break;
                case "PLACE_BLOCKS":
                    updateConfigAndNotify(player, "place_blocks", args[1]);
                    break;
                case "FRIENDLY_FIRE":
                    updateConfigAndNotify(player, "friendly-fire", args[1]);
                    break;
                case "DROP_ON_DEATH":
                    updateConfigAndNotify(player, "drop-on-death", args[1]);
                    break;
                case "KILL_ON_FIGHT_END":
                    updateConfigAndNotify(player, "kill-on-fight-end", args[1]);
                    break;
                case "FREEZE_ON_START":
                    updateConfigAndNotify(player, "freeze-on-start", args[1]);
                    break;
                case "CURRENT_ARENA":
                    if (getPlugin().arenaNames.contains(args[1])) {
                        config.set("current-arena", args[1]);
                        send(player, "config-updated-successfully");
                    } else {
                        send(player, "arena-not-exists");
                    }
                    break;
                case "CURRENT_KIT":
                    if (getPlugin().kitNames.contains(args[1])) {
                        config.set("current-kit", args[1]);
                        send(player, "config-updated-successfully");
                    } else {
                        send(player, "kit-not-exists");
                    }
                    break;
                default:
                    return false;
            }
        } else return false;
        config.save(getPlugin().customConfigFile);
        return true;
    }

    private void updateConfigAndNotify(Player player, String configKey, String value) {
        if (value.equals("true")) {
            config.set(configKey, true);
            send(player, "config-updated-successfully");
        } else if (value.equals("false")) {
            config.set(configKey, false);
            send(player, "config-updated-successfully");
        } else {
            send(player, "config-options");
        }
    }

    public @NotNull List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
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
                    return new ArrayList<>(getPlugin().arenaNames);
                case "current_kit":
                    return new ArrayList<>(getPlugin().kitNames);
            }
        }
        return Collections.emptyList();
    }
}
