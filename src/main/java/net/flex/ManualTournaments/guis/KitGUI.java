package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonDirector;
import net.flex.ManualTournaments.buttons.kitButtons.CreateKitButton;
import net.flex.ManualTournaments.buttons.kitButtons.KitButton;
import net.flex.ManualTournaments.utils.gui.menu.Menu;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class KitGUI {
    public static Menu kitMenu = gui.create(getPlugin().getConfig().getString("gui-kit-menu-name"), 5);
    public static Map<String, Button> kitMenuButtons = new HashMap<>();
    public static boolean opener = false;
    public static ButtonDirector director = new ButtonDirector();

    public static void kitGUI(Player sender) {
        kitMenu.setToolbarBuilder((slot, page, type, menu) -> {
            if (slot == 8) return director.constructButton(new CreateKitButton(sender));
            else return gui.getDefaultToolbarBuilder().buildToolbarButton(slot, page, type, menu);
        });
        kitMenu.clearAllButStickiedSlots();
        IntStream.range(0, kitNames.size()).forEachOrdered(i -> {
            String kitName = new ArrayList<>(kitNames).get(i);
            Button button = new KitButton(sender, kitName).buildButton();
            kitMenu.setButton(i, button);
            kitMenuButtons.put(kitName, button);
            if (Objects.equals(config.getString("current-kit"), kitName)) {
                addEnchantment(button);
            } else removeEnchantment(button);
        });
        sender.openInventory(kitMenu.getInventory());
    }
}
