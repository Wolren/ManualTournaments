package net.flex.ManualTournaments.commands.settingsCommand;

import net.flex.ManualTournaments.guis.SettingsGUI;
import net.flex.ManualTournaments.interfaces.SettingsShortCommand;
import org.bukkit.entity.Player;

public class GuiSettings implements SettingsShortCommand {
    @Override
    public void execute(Player player) {
        SettingsGUI.settingsGUI(player);
    }
}
