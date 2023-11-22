package net.flex.ManualTournaments.guis;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.settingsButtons.EndspawnSettingsButton;
import net.flex.ManualTournaments.factories.SettingsFactory;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.config;

public class SettingsPresetGUI {
    private static final Collection<String> buttonConfigSet = new HashSet<>(Arrays.asList("break-blocks", "drop-items", "drop-on-death", "freeze-on-start", "friendly-fire", "kill-on-fight-end", "place-blocks"));

    @SneakyThrows
    public static void settingsPresetGUI(Player sender, String context) {
        String name = String.format(Objects.requireNonNull(config.getString("gui-preset-settings-menu-name")), context);
        Menu settingsMenu = gui.create(name, 3);
        AtomicInteger index = new AtomicInteger(1);
        settingsMenu.setButton(0, KitGUI.director.constructButton(new EndspawnSettingsButton(sender, context)));
        SettingsFactory.settingsCommandMap.keySet().stream()
                .map(settingsCommand -> settingsCommand.toLowerCase().replaceAll("_", "-"))
                .forEachOrdered(buttonName -> {
                    if (buttonConfigSet.contains(buttonName)) {
                        int i = index.getAndIncrement();
                        settingsMenu.setButton(i, createButton(buttonName, settingsMenu, sender, context));
                    }
                });
        settingsMenu.setAutomaticPaginationEnabled(false);
        sender.openInventory(settingsMenu.getInventory());
    }


    private static Button createButton(String buttonName, Menu menu, Player sender, String context) {
        ItemStack trueIs = new ItemBuilder(Material.GREEN_WOOL)
                .lore(config.getString("gui-settings-button-true-lore"))
                .name(config.getString("gui-settings-button-name-color") + buttonName.replaceAll("-", " "))
                .build();
        ItemStack falseIs = new ItemBuilder(Material.RED_WOOL)
                .lore(config.getString("gui-settings-button-false-lore"))
                .name(config.getString("gui-settings-button-name-color") + buttonName.replaceAll("-", " "))
                .build();
        Button setting = new Button(new ItemBuilder(Material.WHITE_WOOL).build());
        updateButtonIcon(setting, buttonName, trueIs, falseIs, context);
        setting.withListener(event -> updateButtonOnEvent(setting, buttonName, trueIs, falseIs, menu, sender, context));
        return setting;
    }

    private static void updateButtonIcon(Button setting, String buttonConfig, ItemStack trueIs, ItemStack falseIs, String context) {
        boolean configValue = false;
        String path = "Presets." + context + "." + buttonConfig;
        if (context.equals("default")) {
            configValue = config.getBoolean(buttonConfig);
        } else if (Main.presetNames.contains(context)) {
            configValue = Main.getPresetConfig().getBoolean(path);
        }
        setting.setIcon(configValue ? trueIs : falseIs);
    }

    @SneakyThrows
    private static void updateButtonOnEvent(Button setting, String buttonConfig, ItemStack trueIs, ItemStack falseIs, Menu menu, Player sender, String context) {
        if (context.equals("default")) {
            boolean configValue = !config.getBoolean(buttonConfig);
            config.set(buttonConfig, configValue);
            updateButtonIcon(setting, buttonConfig, trueIs, falseIs, context);
            menu.refreshInventory(sender);
            config.save(getCustomConfigFile());
        } else if (Main.presetNames.contains(context)) {
            String path = "Presets." + context + "." + buttonConfig;
            boolean configValue = !Main.getPresetConfig().getBoolean(path);
            getPresetConfig().set(path, configValue);
            updateButtonIcon(setting, buttonConfig, trueIs, falseIs, context);
            menu.refreshInventory(sender);
            getPresetConfig().save(getPresetConfigFile());
        }
    }
}
