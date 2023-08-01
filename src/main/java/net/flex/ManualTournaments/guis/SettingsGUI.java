package net.flex.ManualTournaments.guis;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.settingsButtons.EndspawnSettingsButton;
import net.flex.ManualTournaments.factories.SettingsFactory;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static net.flex.ManualTournaments.Main.*;

public class SettingsGUI {
    private static final Collection<String> buttonConfigSet = new HashSet<>(Arrays.asList("break-blocks", "drop-items", "drop-on-death", "freeze-on-start", "friendly-fire", "kill-on-fight-end", "place-blocks"));
    public static SGMenu settingsMenu = gui.create(getPlugin().getConfig().getString("gui-settings-menu-name"), 3, "Settings");
    @SneakyThrows
    public static void settingsGUI(Player sender) {
        AtomicInteger index = new AtomicInteger(1);
        settingsMenu.setButton(0, KitGUI.director.constructButton(new EndspawnSettingsButton(sender)));
        SettingsFactory.settingsCommandMap.keySet().stream()
                .map(settingsCommand -> settingsCommand.toLowerCase().replaceAll("_", "-"))
                .forEachOrdered(buttonName -> {
                    int i = index.getAndIncrement();
                    if (buttonConfigSet.contains(buttonName)) {
                        settingsMenu.setButton(i, createButton(buttonName, settingsMenu, sender));
                    }
                });
        settingsMenu.setAutomaticPaginationEnabled(false);
        sender.openInventory(settingsMenu.getInventory());
    }


    private static Button createButton(String buttonName, SGMenu menu, Player sender) {
        ItemStack trueIs = new ItemBuilder(Material.GREEN_WOOL)
                .lore(getPlugin().getConfig().getString("gui-settings-button-true-lore"))
                .name(getPlugin().getConfig().getString("gui-settings-button-name-color") + buttonName.replaceAll("-", " "))
                .build();
        ItemStack falseIs = new ItemBuilder(Material.RED_WOOL)
                .lore(getPlugin().getConfig().getString("gui-settings-button-false-lore"))
                .name(getPlugin().getConfig().getString("gui-settings-button-name-color") + buttonName.replaceAll("-", " "))
                .build();
        Button setting = new Button(new ItemBuilder(Material.WHITE_WOOL).build());
        updateButtonIcon(setting, buttonName, trueIs, falseIs);
        setting.withListener(event -> updateButtonOnEvent(setting, buttonName, trueIs, falseIs, menu, sender));
        return setting;
    }

    private static void updateButtonIcon(Button setting, String buttonConfig, ItemStack trueIs, ItemStack falseIs) {
        boolean configValue = getPlugin().getConfig().getBoolean(buttonConfig);
        setting.setIcon(configValue ? trueIs : falseIs);
    }

    @SneakyThrows
    private static void updateButtonOnEvent(Button setting, String buttonConfig, ItemStack trueIs, ItemStack falseIs, SGMenu menu, Player sender) {
        boolean configValue = !getPlugin().getConfig().getBoolean(buttonConfig);
        getPlugin().getConfig().set(buttonConfig, configValue);
        updateButtonIcon(setting, buttonConfig, trueIs, falseIs);
        menu.refreshInventory(sender);
        getPlugin().getConfig().save(getCustomConfigFile());
    }
}
