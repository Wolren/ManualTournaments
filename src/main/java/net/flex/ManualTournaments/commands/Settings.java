package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.factories.SettingsFactory;
import net.flex.ManualTournaments.factories.SettingsShortFactory;
import net.flex.ManualTournaments.guis.SettingsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.optional;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class Settings implements TabCompleter, CommandExecutor {
    private static final FileConfiguration config = getPlugin().getConfig();
    Player player = null;

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        config.load(getPlugin().customConfigFile);
        if (args.length == 0) {
            SettingsGUI.settingsGUI(player);
        } else if (args.length == 1) {
            SettingsShortFactory.getCommand(args[0].toUpperCase()).execute(player, args[0]);
        } else if (args.length == 2) {
            SettingsFactory.getCommand(args[0].toUpperCase()).execute(player, args[0], args[1]);
        } else send(player, "settings-usage");
        return true;
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
