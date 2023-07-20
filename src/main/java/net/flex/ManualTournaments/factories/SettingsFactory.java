package net.flex.ManualTournaments.factories;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.settingsCommand.*;
import net.flex.ManualTournaments.interfaces.SettingsCommand;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class SettingsFactory {
    private static final Map<String, SettingsCommand> settingsCommandMap;

    static {
        settingsCommandMap = new ConcurrentHashMap<>();
        settingsCommandMap.put("BREAK_BLOCKS", new BreakBlocksSettings());
        settingsCommandMap.put("CURRENT_ARENA", new CurrentArenaSettings());
        settingsCommandMap.put("CURRENT_KIT", new CurrentKitSettings());
        settingsCommandMap.put("DROP_ITEMS", new DropItemsSettings());
        settingsCommandMap.put("DROP_ON_DEATH", new DropOnDeathSettings());
        settingsCommandMap.put("FREEZE_ON_START", new FreezeOnStartSettings());
        settingsCommandMap.put("FRIENDLY_FIRE", new FriendlyFireSettings());
        settingsCommandMap.put("KILL_ON_FIGHT_END", new KillOnFightEndSettings());
        settingsCommandMap.put("PLACE_BLOCKS", new PlaceBlocksSettings());
    }

    public static SettingsCommand getCommand(String command) {
        return settingsCommandMap.getOrDefault(command, (player, setting, value) -> send(player, "settings-usage"));
    }

    @SneakyThrows
    public static void updateConfigAndNotify(Player player, String configKey, String value) {
        if (value.equals("true") || value.equals("false")) {
            getPlugin().getConfig().set(configKey, true);
            send(player, "config-updated-successfully");
            getPlugin().getConfig().save(getPlugin().customConfigFile);
        } else send(player, "config-options");
    }
}
