package net.flex.ManualTournaments.factories;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.settingsCommand.*;
import net.flex.ManualTournaments.interfaces.SettingsCommand;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class SettingsFactory {
    public static Map<String, SettingsCommand> settingsCommandMap = new HashMap<String, SettingsCommand>() {{
        put("BREAK_BLOCKS", new BreakBlocksSettings());
        put("CREATE", new CreatePreset());
        put("CURRENT_ARENA", new CurrentArenaSettings());
        put("CURRENT_KIT", new CurrentKitSettings());
        put("DROP_ITEMS", new DropItemsSettings());
        put("DROP_ON_DEATH", new DropOnDeathSettings());
        put("FREEZE_ON_START", new FreezeOnStartSettings());
        put("FRIENDLY_FIRE", new FriendlyFireSettings());
        put("KILL_ON_FIGHT_END", new KillOnFightEndSettings());
        put("PLACE_BLOCKS", new PlaceBlocksSettings());
    }};

    public static SettingsCommand getCommand(String command) {
        return settingsCommandMap.getOrDefault(command, (player, context, value) -> send(player, "settings-usage"));
    }

    @SneakyThrows
    public static void updateDefaultConfig(Player player, String configKey, String value) {
        if (value.equals("true") || value.equals("false")) {
            getPlugin().getConfig().set(configKey, value);
            send(player, "config-updated-successfully");
            getPlugin().getConfig().save(getCustomConfigFile());
        } else send(player, "config-options");
    }

    @SneakyThrows
    public static void updatePresetConfig(Player player, String configKey, String context, String value) {
        String path = "Presets." + context + "." + configKey;
        if (value.equals("true") || value.equals("false")) {
            getPresetConfig().set(path, value);
            send(player, "config-updated-successfully");
            getPresetConfig().save(getPresetConfigFile());
        } else send(player, "config-options");
    }
}



