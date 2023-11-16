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
    public static Menu arenaMenu = gui.create(config.getString("gui-arena-menu-name"), 5);
    public static Map<String, Button> arenaMenuButtons = new HashMap<>();
    public static boolean isOpenerActive = false;
    public static ButtonDirector director = new ButtonDirector();

    public void arenaGUI(Player sender) {
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

    public List<String> getLore(String path) {
        List<String> lore = new ArrayList<>();
        String[] keys = {"x", "y", "z", "yaw", "pitch", "world"};
        for (String key : keys) {
            String value = config.getString("gui-arena-settings-lore-color") + key + ": " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().get(path + key);
            lore.add(value);
        }
        return lore;
    }
}
