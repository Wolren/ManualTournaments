package net.flex.ManualTournaments.buttons.kitButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.guis.KitGUI;
import net.flex.ManualTournaments.guis.KitSettingsGUI;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.getCustomConfigFile;
import static net.flex.ManualTournaments.Main.getPlugin;

public class KitButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();

    public KitButton(Player sender, String kitName) {
        super(sender, kitName);
    }

    protected Button configureButton(Player sender, String name) {
        return new Button(new ItemBuilder(Material.GRASS_BLOCK)
                .name(config.getString("gui-kit-name-color") + name)
                .lore(
                        config.getString("gui-kit-lore-right-click"),
                        config.getString("gui-kit-lore-left-click"))
                .build())
                .withListener(event -> {
                    if (event.isRightClick()) {
                        getPlugin().getConfig().set("current-kit", name);
                        getPlugin().getConfig().save(getCustomConfigFile());
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

    private static void addEnchantment(Button button) {
        ItemMeta meta = button.getIcon().getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            button.getIcon().setItemMeta(meta);
        }
    }

    private static void removeEnchantment(Button button) {
        button.getIcon().removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
    }
}
