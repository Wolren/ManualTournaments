package net.flex.ManualTournaments.commands.settingsCommand;

import net.flex.ManualTournaments.interfaces.SettingsCommand;
import net.flex.ManualTournaments.factories.SettingsFactory;
import org.bukkit.entity.Player;

public final class KillOnFightEndSettings implements SettingsCommand {
    @Override
    public void execute(Player player, String setting, String value) {
        SettingsFactory.updateConfigAndNotify(player, "kill-on-fight-end", value);
    }
}
