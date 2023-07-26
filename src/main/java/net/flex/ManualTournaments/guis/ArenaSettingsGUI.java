package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.Main.gui;

public class ArenaSettingsGUI {
    public static SGMenu arenaSettingsMenu = gui.create(getPlugin().getConfig().getString("gui-arena-settings-menu-name"), 5, "Arena Settings");

    public static void arenaSettingsGUI(Player sender) {

    }
}
