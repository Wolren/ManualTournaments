package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.utils.gui.buttonManaging.Button;
import net.flex.ManualTournaments.utils.gui.buttonManaging.ButtonDirector;
import net.flex.ManualTournaments.utils.gui.buttonManaging.buttons.ArenaButton;
import net.flex.ManualTournaments.utils.gui.buttonManaging.buttons.CreateArenaButton;
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

public class ArenaGUI {
    public static SGMenu arenaMenu = gui.create(getPlugin().getConfig().getString("gui-arena-menu-name"), 5, "Arena");
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
            Button button = createButton(arenaName, sender);
            arenaMenu.setButton(i, button);
            arenaMenuButtons.put(arenaName, button);
            if (Objects.equals(getPlugin().getConfig().getString("current-arena"), arenaName)) {
                addEnchantment(button);
            } else removeEnchantment(button);
        });
        sender.openInventory(arenaMenu.getInventory());
    }

    private static Button createButton(String arenaName, Player sender) {
        return new ArenaButton(sender, arenaName).buildButton();
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
