package net.flex.ManualTournaments.commands.settingsCommand;

import net.flex.ManualTournaments.interfaces.SettingsCommand;
import net.flex.ManualTournaments.factories.SettingsFactory;
import org.bukkit.entity.Player;

public final class FreezeOnStartSettings implements SettingsCommand {
    @Override
    public void execute(Player player, String context, String value) {
        if (context.equals("default")) SettingsFactory.updateDefaultConfig(player, "freeze-on-start", value);
        else {
            SettingsFactory.updatePresetConfig(player, "freeze-on-start", context, value);
        }
    }
}
