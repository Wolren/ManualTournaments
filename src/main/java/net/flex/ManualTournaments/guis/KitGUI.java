package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.Main.gui;

public class KitGUI {
    public static SGMenu KitMenu = gui.create(getPlugin().getConfig().getString("gui-kit-menu-name"), 5, "Kit");
    public static void kitGUI(Player sender) {

    }
}
