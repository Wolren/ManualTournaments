package net.flex.ManualTournaments.utils.gui.buttonManaging.buttons;

import net.flex.ManualTournaments.guis.ArenaGUI;
import net.flex.ManualTournaments.guis.ArenaSettingsGUI;
import net.flex.ManualTournaments.utils.gui.buttonManaging.Button;
import net.flex.ManualTournaments.utils.gui.buttonManaging.ButtonBuilder;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.getPlugin;

public class ArenaButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();

    public ArenaButton(Player sender, String arenaName) {
        super(sender, arenaName);
    }

    protected Button configureButton(Player sender, String arenaName) {
        return new Button(new ItemBuilder(Material.GRASS_BLOCK)
                .name(config.getString("gui-arena-name-color") + arenaName)
                .lore(
                        config.getString("gui-arena-lore-right-click"),
                        config.getString("gui-arena-lore-left-click"))
                .build())
                .withListener(event -> {
                    if (event.isRightClick()) {
                        getPlugin().getConfig().set("current-arena", arenaName);
                        getPlugin().getConfig().save(getPlugin().customConfigFile);
                        refresh(sender, arenaName);
                    } else if (event.isLeftClick()) {
                        ArenaSettingsGUI.arenaSettingsGUI(sender, arenaName);
                    }
                });
    }

    public static void refresh(Player sender, String arenaName) {
        ArenaGUI.arenaMenuButtons.forEach((key, button) -> {
            ItemStack buttonItem = button.getIcon();
            ItemMeta buttonItemMeta = buttonItem.getItemMeta();
            if (buttonItemMeta != null) {
                if (Objects.equals(key, arenaName)) {
                    addEnchantment(button);
                } else removeEnchantment(button);
                ArenaGUI.arenaMenu.refreshInventory(sender);
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
