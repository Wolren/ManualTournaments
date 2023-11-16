package net.flex.ManualTournaments.commands.settingsCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.SettingsCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class CreatePreset implements SettingsCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String setting, String presetName) {
        if (!Main.presetNames.contains(presetName)) {
            getPresetConfig().set("Presets." + presetName, "");
            getPresetConfig().save(getArenaConfigFile());
            presetNames.add(presetName);
            send(player, "preset-create");
        }
    }
}
