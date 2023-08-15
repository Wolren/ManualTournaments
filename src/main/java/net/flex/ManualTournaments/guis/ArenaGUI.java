package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonDirector;
import net.flex.ManualTournaments.buttons.arenaButtons.ArenaButton;
import net.flex.ManualTournaments.buttons.arenaButtons.CreateArenaButton;
import net.flex.ManualTournaments.utils.gui.menu.Menu;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class ArenaGUI {
    public static Menu arenaMenu = gui.create(getPlugin().getConfig().getString("gui-arena-menu-name"), 5);
    public static Map<String, Button> arenaMenuButtons = new HashMap<>();
    public static boolean opener = false;
    public static ButtonDirector director = new ButtonDirector();

    public static void arenaGUI(Player sender) {
        arenaMenu.setToolbarBuilder((slot, page, type, menu) -> {
            if (slot == 8) return director.constructButton(new CreateArenaButton(sender));
            else return gui.getDefaultToolbarBuilder().buildToolbarButton(slot, page, type, menu);
        });
        arenaMenu.clearAllButStickiedSlots();
        IntStream.range(0, arenaNames.size()).forEach(i -> {
            String arenaName = new ArrayList<>(arenaNames).get(i);
            Button button = new ArenaButton(sender, arenaName).buildButton();
            arenaMenu.setButton(i, button);
            arenaMenuButtons.put(arenaName, button);
            if (Objects.equals(config.getString("current-arena"), arenaName)) {
                addEnchantment(button);
            } else removeEnchantment(button);
        });
        sender.openInventory(arenaMenu.getInventory());
    }

    public static List<String> getLore(String path) {
        return Arrays.asList(
                config.getString("gui-arena-settings-lore-color") + "x: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(path + "x"),
                config.getString("gui-arena-settings-lore-color") + "y: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(path + "y"),
                config.getString("gui-arena-settings-lore-color") + "z: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(path + "z"),
                config.getString("gui-arena-settings-lore-color") + "yaw: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(path + "yaw"),
                config.getString("gui-arena-settings-lore-color") + "pitch: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(path + "pitch"),
                config.getString("gui-arena-settings-lore-color") + "world: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getString(path + "world")
        );
    }
}
