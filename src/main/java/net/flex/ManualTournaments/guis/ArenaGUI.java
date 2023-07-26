package net.flex.ManualTournaments.guis;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.utils.gui.buttons.Button;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.Main.gui;

public class ArenaGUI {
    public static SGMenu arenaMenu = gui.create(getPlugin().getConfig().getString("gui-arena-menu-name"), 5, "Arena");
    public static List<Button> arenaMenuButtons = new ArrayList<>();
    public static SGMenu arenaSettingsMenu = gui.create(getPlugin().getConfig().getString("gui-arena-settings-menu-name"), 5, "Arena Settings");
    public static void arenaGUI(Player sender) {
        IntStream.range(0, getPlugin().arenaNames.size()).forEach(i -> {
            Button button = createButton(getPlugin().arenaNames.get(i), arenaMenu, sender);
            arenaMenu.setButton(i, button);
            arenaMenuButtons.add(button);
        });
        arenaMenu.setToolbarBuilder((slot, page, defaultType, menu) -> {
            if (slot == 0) {
                return new Button (new ItemBuilder(Material.EMERALD_BLOCK)
                        .name("")
                        .lore("")
                        .build())
                        .withListener(event -> {
                            sender.closeInventory();

                });
            }
            return gui.getDefaultToolbarBuilder().buildToolbarButton(slot, page, defaultType, menu);
        });
        sender.openInventory(arenaMenu.getInventory());
    }

    @SneakyThrows
    private static Button createButton(String arenaName, SGMenu menu, Player sender) {
        Main.getCustomConfig().load(getPlugin().customConfigFile);
        FileConfiguration config = getPlugin().getConfig();
        ItemStack buttonItem = new ItemBuilder(Material.DIRT)
                .name(arenaName)
                .lore(
                        config.getString("gui-arena-lore-right-click"),
                        config.getString("gui-arena-lore-left-click"))
                .build();
        if (Objects.equals(config.getString("current-arena"), arenaName)) {
            ItemMeta buttonItemMeta = buttonItem.getItemMeta();
            if (buttonItemMeta != null) {
                buttonItemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                buttonItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            buttonItem.setItemMeta(buttonItemMeta);
            arenaMenu.refreshInventory(sender);
        }
        if (!Objects.equals(config.getString("current-arena"), arenaName)) {
            buttonItem.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
            arenaMenu.refreshInventory(sender);
        }
        return new Button(buttonItem)
                .withListener(event -> {
                    if (event.isRightClick()) {
                        getPlugin().getConfig().set("current-arena", arenaName);
                        getPlugin().getConfig().save(getPlugin().customConfigFile);
                        refresh(sender);
                    }
                    else if (event.isLeftClick()) {
                        sender.openInventory(arenaSettingsMenu.getInventory());
                    }
        });
    }

    private static void refresh(Player sender) {
        for (Button button : arenaMenuButtons) {
            ItemMeta buttonItemMeta = button.getIcon().getItemMeta();
            if (buttonItemMeta != null) {
                if (Objects.equals(getPlugin().getConfig().getString("current-arena"), buttonItemMeta.getDisplayName())) {
                    buttonItemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                    buttonItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                } else if (!Objects.equals(getPlugin().getConfig().getString("current-arena"), buttonItemMeta.getDisplayName())) {
                    buttonItemMeta.removeEnchant(Enchantment.PROTECTION_ENVIRONMENTAL);
                }
                button.getIcon().setItemMeta(buttonItemMeta);
                arenaMenu.refreshInventory(sender);
            }
        }
    }
}
