package net.flex.ManualTournaments.buttons.arenaButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.guis.ArenaGUI;
import net.flex.ManualTournaments.guis.ArenaSettingsGUI;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.getCustomConfigFile;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class ArenaButton extends ButtonBuilder {
    public ArenaButton(Player sender, String arenaName) {
        super(sender, arenaName);
    }

    protected Button configureButton(Player sender, String name) {
        return new Button(new ItemBuilder(Material.DIRT)
                .name(config.getString("gui-arena-name-color") + name)
                .lore(config.getString("gui-arena-lore-right-click"), config.getString("gui-arena-lore-left-click"))
                .build()).withListener(event -> {
            if (event.isRightClick()) {
                config.set("current-arena", name);
                config.save(getCustomConfigFile());
                refresh(sender, name);
            } else if (event.isLeftClick()) {
                ArenaSettingsGUI.arenaSettingsGUI(sender, name);
            }
        });
    }

    private static void refresh(Player sender, String arenaName) {
        ArenaGUI.arenaMenuButtons.forEach((key, button) -> {
            ItemMeta buttonItemMeta = button.getIcon().getItemMeta();
            if (buttonItemMeta != null) {
                if (Objects.equals(key, arenaName)) {
                    addEnchantment(button);
                } else removeEnchantment(button);
                ArenaGUI.arenaMenu.refreshInventory(sender);
            }
        });
    }
}
