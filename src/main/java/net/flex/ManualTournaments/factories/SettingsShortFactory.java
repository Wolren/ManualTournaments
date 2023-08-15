package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.settingsCommand.EndspawnSettings;
import net.flex.ManualTournaments.commands.settingsCommand.GuiSettings;
import net.flex.ManualTournaments.interfaces.SettingsShortCommand;

import java.util.HashMap;
import java.util.Map;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class SettingsShortFactory {
    public static final Map<String, SettingsShortCommand> settingsShortCommandMap = new HashMap<String, SettingsShortCommand>() {{
        put("ENDSPAWN", new EndspawnSettings());
        put("GUI", new GuiSettings());
    }};

    public static SettingsShortCommand getCommand(String command) {
        return settingsShortCommandMap.getOrDefault(command, (player) -> send(player, "settings-usage"));
    }
}
