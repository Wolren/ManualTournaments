package net.flex.ManualTournaments.guis;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.factories.SettingsFactory;
import net.flex.ManualTournaments.interfaces.SettingsCommand;
import net.flex.ManualTournaments.utils.gui.buttons.Button;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.Main.gui;

public class SettingsGUI {
    private static final Set<String> buttonConfigSet = new HashSet<>(Arrays.asList("break-blocks", "drop-items", "drop-on-death", "freeze-on-start", "friendly-fire", "kill-on-fight-end", "place-blocks"));

    @SneakyThrows
    public static void settingsGUI(Player sender) {
        SGMenu menu = gui.create("Settings Menu", 5);
        for (Map.Entry<String, SettingsCommand> entry : SettingsFactory.settingsCommandMap.entrySet()) {
            String buttonConfig = entry.getKey().toLowerCase().replaceAll("_", "-");
            if (buttonConfigSet.contains(buttonConfig)) {
                menu.addButton(createButton(buttonConfig, menu, sender));
            } else if (buttonConfig.equals("current-arena")) {

            } else if (buttonConfig.equals("current-kit")) {

            } else if (buttonConfig.equals("endspawn")) {

            }
        }
        menu.setAutomaticPaginationEnabled(false);
        sender.openInventory(menu.getInventory());
    }

    private static Button createButton(String buttonConfig, SGMenu menu, Player sender) {
        ItemStack trueIs = new ItemBuilder(Material.GREEN_WOOL).lore("&aTrue").name("&7&l" + buttonConfig.replaceAll("-", " ")).build();
        ItemStack falseIs = new ItemBuilder(Material.RED_WOOL).lore("&cFalse").name("&7&l" + buttonConfig.replaceAll("-", " ")).build();
        Button setting = new Button(new ItemBuilder(Material.WHITE_WOOL).build());
        updateButtonIcon(setting, buttonConfig, trueIs, falseIs);
        setting.setListener(event -> updateButtonOnEvent(setting, buttonConfig, trueIs, falseIs, menu, sender));
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
        getPlugin().getConfig().save(getPlugin().customConfigFile);
    }
}
