package net.flex.ManualTournaments.commands.SettingsCommand.Implementations;

import net.flex.ManualTournaments.commands.SettingsCommand.SettingsCommand;
import net.flex.ManualTournaments.commands.SettingsCommand.SettingsFactory;
import org.bukkit.entity.Player;

public class FreezeOnStartSettings implements SettingsCommand {
    @Override
    public void execute(Player player, String setting, String value) {
        SettingsFactory.updateConfigAndNotify(player, "freeze-on-start", value);
    }
}
