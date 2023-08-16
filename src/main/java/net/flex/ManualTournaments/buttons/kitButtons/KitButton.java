package net.flex.ManualTournaments.buttons.kitButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.guis.KitGUI;
import net.flex.ManualTournaments.guis.KitSettingsGUI;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.getCustomConfigFile;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class KitButton extends ButtonBuilder {
    public KitButton(Player sender, String kitName) {
        super(sender, kitName);
    }

    protected Button configureButton(Player sender, String name) {
        return new Button(new ItemBuilder(Material.STICK)
                .name(config.getString("gui-kit-name-color") + name)
                .lore(config.getString("gui-kit-lore-right-click"), config.getString("gui-kit-lore-left-click"))
                .build()).withListener(event -> {
            if (event.isRightClick()) {
                config.set("current-kit", name);
                config.save(getCustomConfigFile());
                refresh(sender, name);
            } else if (event.isLeftClick()) {
                KitSettingsGUI.kitSettingsGUI(sender, name);
            }
        });
    }

    public static void refresh(Player sender, String kitName) {
        KitGUI.kitMenuButtons.forEach((key, button) -> {
            ItemStack buttonItem = button.getIcon();
            ItemMeta buttonItemMeta = buttonItem.getItemMeta();
            if (buttonItemMeta != null) {
                if (Objects.equals(key, kitName)) {
                    addEnchantment(button);
                } else removeEnchantment(button);
                KitGUI.kitMenu.refreshInventory(sender);
            }
        });
    }
}
