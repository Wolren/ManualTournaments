package net.flex.ManualTournaments.commands.KitCommand.Short.Implementations;

import net.flex.ManualTournaments.commands.KitCommand.KitFactory;
import net.flex.ManualTournaments.commands.KitCommand.Short.KitCommandType;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;

public class GiveShortKit implements KitCommandType {
    @Override
    public boolean kitCommand(Player player, String arg) {
        KitFactory.getCommand("GIVE").execute(player, arg, getPlugin().kitNames.contains(arg));
        return true;
    }
}
