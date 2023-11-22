package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonDirector;
import net.flex.ManualTournaments.buttons.settingsButtons.CreatePresetButton;
import net.flex.ManualTournaments.buttons.settingsButtons.PresetButton;
import net.flex.ManualTournaments.utils.gui.menu.Menu;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.gui;
import static net.flex.ManualTournaments.Main.presetNames;
import static net.flex.ManualTournaments.utils.SharedComponents.config;

public class SettingsGUI {
    public static Menu settingsMenu = gui.create(config.getString("gui-settings-menu-name"), 5);
    public static Map<String, Button> settingsMenuButtons = new HashMap<>();
    public static boolean isOpenerActive = false;
    private static final ButtonDirector director = new ButtonDirector();
    public static void settingsGUI(Player sender) {
        settingsMenu.setToolbarBuilder((slot, page, type, menu) -> {
            if (slot == 8) return director.constructButton(new CreatePresetButton(sender));
            else return gui.getDefaultToolbarBuilder().buildToolbarButton(slot, page, type, menu);
        });
        settingsMenu.clearAllButStickiedSlots();
        Button defaultButton = new PresetButton(sender, "default").buildButton();
        settingsMenu.setButton(0, defaultButton);
        settingsMenuButtons.put("default", defaultButton);
        IntStream.range(1, presetNames.size()).forEach(i -> {
            String presetName = new ArrayList<>(presetNames).get(i);
            Button button = new PresetButton(sender, presetName).buildButton();
            settingsMenu.setButton(i, button);
            settingsMenuButtons.put(presetName, button);
        });
        sender.openInventory(settingsMenu.getInventory());
    }
}
