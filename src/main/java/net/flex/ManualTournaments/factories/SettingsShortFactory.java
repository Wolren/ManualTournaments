package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.settingsCommand.EndspawnSettings;
import net.flex.ManualTournaments.interfaces.SettingsShortCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class SettingsShortFactory {
    public static final Map<String, SettingsShortCommand> settingsShortCommandMap;

    static {
        settingsShortCommandMap = new ConcurrentHashMap<>();
        settingsShortCommandMap.put("ENDSPAWN", new EndspawnSettings());
    }

    public static SettingsShortCommand getCommand(String command) {
        return settingsShortCommandMap.getOrDefault(command, (player, setting) -> send(player, "settings-usage"));
    }
}
