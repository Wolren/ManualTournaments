package net.flex.ManualTournaments.commands.kitCommands;

import net.flex.ManualTournaments.interfaces.KitShortCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.message;

public final class ListKit implements KitShortCommand {
    @Override
    public void execute(Player player, String arg) {
        player.sendMessage(message("kit-list") + String.join(", ", getPlugin().kitNames));
    }
}
