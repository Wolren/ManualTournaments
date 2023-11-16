package net.flex.ManualTournaments.commands.arenaCommands;

import net.flex.ManualTournaments.guis.ArenaGUI;
import net.flex.ManualTournaments.interfaces.ArenaShortCommand;
import org.bukkit.entity.Player;

public class GuiArena implements ArenaShortCommand {
    @Override
    public void execute(Player player, String arg) {
        new ArenaGUI().arenaGUI(player);
    }
}
