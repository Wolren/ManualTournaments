package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.utils.gui.buttons.Button;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.Main.gui;

public class ArenaGUI {
    public static SGMenu arenaMenu = gui.create("Arena Menu", 5, "Arena");
    public static SGMenu arenaSettingsMenu = gui.create("Arena Settings", 5, "Arena Settings");
    public static void arenaGUI(Player sender) {
        getPlugin().arenaNames.forEach(arenaName -> {
            arenaMenu.addButton(createButton(arenaName, arenaMenu, sender));
        });
        sender.openInventory(arenaMenu.getInventory());
    }

    private static Button createButton(String arenaName, SGMenu menu, Player sender) {
        return new Button(new ItemBuilder(Material.MAP)
                .name(arenaName)
                .build())
                .withListener(event -> {
                    if (event.isRightClick()) {

                    }
                    else if (event.isLeftClick()) {
                        getPlugin().getConfig().set("current-arena", arenaName);
                    }
        });
    }
}
