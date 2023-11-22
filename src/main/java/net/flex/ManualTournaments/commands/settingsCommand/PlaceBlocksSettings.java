package net.flex.ManualTournaments.commands.settingsCommand;

import net.flex.ManualTournaments.interfaces.SettingsCommand;
import net.flex.ManualTournaments.factories.SettingsFactory;
import org.bukkit.entity.Player;

public final class PlaceBlocksSettings implements SettingsCommand {
    @Override
    public void execute(Player player, String context, String value) {
        if (context.equals("default")) SettingsFactory.updateDefaultConfig(player, "place-blocks", value);
        else {
            SettingsFactory.updatePresetConfig(player, "place-blocks", context, value);
        }
    }
}
