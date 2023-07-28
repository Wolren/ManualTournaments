package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonDirector;
import net.flex.ManualTournaments.buttons.kitButtons.CreateKitButton;
import net.flex.ManualTournaments.buttons.kitButtons.KitButton;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.*;

public class KitGUI {
    public static SGMenu kitMenu = gui.create(getPlugin().getConfig().getString("gui-kit-menu-name"), 5, "Kit");
    public static Map<String, Button> kitMenuButtons = new HashMap<>();
    public static boolean opener = false;
    public static ButtonDirector director = new ButtonDirector();

    public static void kitGUI(Player sender) {
        kitMenu.setToolbarBuilder((slot, page, type, menu) -> {
            if (slot == 8) return director.constructButton(new CreateKitButton(sender));
            else return gui.getDefaultToolbarBuilder().buildToolbarButton(slot, page, type, menu);
        });
        kitMenu.clearAllButStickiedSlots();
        IntStream.range(0, kitNames.size()).forEach(i -> {
            String kitName = new ArrayList<>(kitNames).get(i);
            Button button = new KitButton(sender, kitName).buildButton();
            kitMenu.setButton(i, button);
            kitMenuButtons.put(kitName, button);
            if (Objects.equals(getPlugin().getConfig().getString("current-kit"), kitName)) {
                addEnchantment(button);
            } else removeEnchantment(button);
        });
        sender.openInventory(kitMenu.getInventory());
    }

    public static void addEnchantment(Button button) {
        ItemMeta meta = button.getIcon().getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            button.getIcon().setItemMeta(meta);
        }
    }

    public static void removeEnchantment(Button button) {
        button.getIcon().removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
    }
}
