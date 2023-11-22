package net.flex.ManualTournaments.commands.settingsCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.SettingsCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.config;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class CreatePreset implements SettingsCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String context, String presetName) {
        if (!Main.presetNames.contains(presetName) && !presetName.equals("default")) {
            getPresetConfig().set("Presets." + presetName, "");
            String path = "Presets." + presetName + ".";
            getPresetConfig().set(path + "break-blocks", config.get("break-blocks"));
            getPresetConfig().set(path + "drop-items", config.get("drop-items"));
            getPresetConfig().set(path + "drop-on-death", config.get("drop-on-death"));
            getPresetConfig().set(path + "freeze-on-start", config.get("freeze-on-start"));
            getPresetConfig().set(path + "friendly-fire", config.get("friendly-fire"));
            getPresetConfig().set(path + "kill-on-fight-end", config.get("kill-on-fight-end"));
            getPresetConfig().set(path + "place-blocks", config.get("place-blocks"));
            getPresetConfig().save(getPresetConfigFile());
            presetNames.add(presetName);
            send(player, "preset-create");
        }
    }
}
