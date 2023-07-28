package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.kitButtons.BackKitButton;
import net.flex.ManualTournaments.buttons.kitButtons.GiveKitButton;
import net.flex.ManualTournaments.buttons.kitButtons.RemoveKitButton;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.Main.gui;

public class KitSettingsGUI {
    public static void kitSettingsGUI(Player sender, String kitName) {
        String name = String.format(Objects.requireNonNull(getPlugin().getConfig().getString("gui-kit-settings-menu-name")), kitName);
        SGMenu kitSetttingsMenu = gui.create(name, 0, name);
        kitSetttingsMenu.setBlockDefaultInteractions(true);
        kitSetttingsMenu.setToolbarBuilder((slot, page, type, menu) -> {
            if (slot == 8) return KitGUI.director.constructButton(new RemoveKitButton(sender, kitName));
            if (slot == 7) return KitGUI.director.constructButton(new GiveKitButton(sender, kitName));
            if (slot == 6) return KitGUI.director.constructButton(new BackKitButton(sender));
            else return new Button(new ItemBuilder(Material.AIR).build());
        });
        sender.openInventory(kitSetttingsMenu.getInventory());
    }
}
