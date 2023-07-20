package net.flex.ManualTournaments.commands.KitCommand.Short.Implementations;

import net.flex.ManualTournaments.commands.KitCommand.Short.KitShortCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.message;

public class ListKit implements KitShortCommand {
    @Override
    public void execute(Player player, String arg) {
        player.sendMessage(message("kit-list") + getPlugin().kitNames.toString());
    }
}
