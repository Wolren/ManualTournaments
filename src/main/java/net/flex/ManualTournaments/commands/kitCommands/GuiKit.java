package net.flex.ManualTournaments.commands.kitCommands;

import net.flex.ManualTournaments.guis.KitGUI;
import net.flex.ManualTournaments.interfaces.KitShortCommand;
import org.bukkit.entity.Player;

public class GuiKit implements KitShortCommand {
    @Override
    public void execute(Player player, String arg) {
        KitGUI.kitGUI(player);
    }
}
