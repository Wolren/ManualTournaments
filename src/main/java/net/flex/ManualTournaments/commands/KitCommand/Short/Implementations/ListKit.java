package net.flex.ManualTournaments.commands.KitCommand.Short.Implementations;

import net.flex.ManualTournaments.commands.KitCommand.Short.KitCommandType;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.message;

public class ListKit implements KitCommandType {
    @Override
    public boolean kitCommand(Player player, String arg) {
        player.sendMessage(message("kit-list") + getPlugin().kitNames.toString());
        return true;
    }
}
