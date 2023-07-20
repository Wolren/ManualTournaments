package net.flex.ManualTournaments.commands.SettingsCommand.Short;

import net.flex.ManualTournaments.commands.SettingsCommand.Short.Implementations.EndspawnSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class SettingsShortFactory {
    private static final Map<String, SettingsShortCommand> settingsShortCommandMap;

    static {
        settingsShortCommandMap = new ConcurrentHashMap<>();
        settingsShortCommandMap.put("BREAK_BLOCKS", new EndspawnSettings());
    }

    public static SettingsShortCommand getCommand(String command) {
        return settingsShortCommandMap.getOrDefault(command, (player, setting) -> send(player, "settings-usage"));
    }
}
