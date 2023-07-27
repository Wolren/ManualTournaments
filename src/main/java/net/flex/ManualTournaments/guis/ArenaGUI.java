package net.flex.ManualTournaments.guis;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.utils.gui.buttonManaging.Button;
import net.flex.ManualTournaments.utils.gui.buttonManaging.ButtonDirector;
import net.flex.ManualTournaments.utils.gui.buttonManaging.buttons.*;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.*;

public class ArenaGUI {
    public static SGMenu arenaMenu = gui.create(getPlugin().getConfig().getString("gui-arena-menu-name"), 5, "Arena");
    public static Collection<Button> arenaMenuButtons = new HashSet<>();
    public static boolean opener = false;
    static FileConfiguration config = getPlugin().getConfig();
    static ButtonDirector director = new ButtonDirector();

    public static void arenaGUI(Player sender) {
        arenaMenu.setToolbarBuilder((slot, page, type, menu) -> {
            if (slot == 8) return director.constructButton(new CreateArenaButton(sender));
            else return gui.getDefaultToolbarBuilder().buildToolbarButton(slot, page, type, menu);
        });
        arenaMenu.clearAllButStickiedSlots();
        IntStream.range(0, arenaNames.size()).forEach(i -> {
            Button button = createButton(new ArrayList<>(arenaNames).get(i), sender);
            arenaMenu.setButton(i, button);
            arenaMenuButtons.add(button);
        });
        sender.openInventory(arenaMenu.getInventory());
    }

    @SneakyThrows
    private static Button createButton(String arenaName, Player sender) {
        Main.getCustomConfig().load(getPlugin().customConfigFile);
        Button arena = director.constructButton(new ArenaButton(sender, arenaName));
        ItemStack arenaIcon = arena.getIcon();
        if (Objects.equals(config.getString("current-arena"), arenaName)) {
            ItemMeta buttonItemMeta = arenaIcon.getItemMeta();
            if (buttonItemMeta != null) {
                buttonItemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                buttonItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            arenaIcon.setItemMeta(buttonItemMeta);
            arena.setIcon(arenaIcon);
            arenaMenu.refreshInventory(sender);
        }
        if (!Objects.equals(config.getString("current-arena"), arenaName)) {
            arenaIcon.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
            arena.setIcon(arenaIcon);
            arenaMenu.refreshInventory(sender);
        }

        return arena.withListener(event -> {
                    if (event.isRightClick()) {
                        getPlugin().getConfig().set("current-arena", arenaName);
                        getPlugin().getConfig().save(getPlugin().customConfigFile);
                        refresh(sender);
                    } else if (event.isLeftClick()) {
                        String name = String.format(Objects.requireNonNull(getPlugin().getConfig().getString("gui-arena-settings-menu-name")), arenaName);
                        SGMenu arenaSettingsMenu = gui.create(name, 2, name);
                        arenaSettingsMenu.setToolbarBuilder((slot, page, type, menu) -> {
                            if (slot == 8) return director.constructButton(new RemoveArenaButton(sender, arenaName));
                            if (slot == 7) return director.constructButton(new ValidateArenaButton(sender, arenaName));
                            if (slot == 6) return director.constructButton(new TeleportArenaButton(sender, arenaName));
                            if (slot == 4) return director.constructButton(new BackButton(sender));
                            else return new Button(new ItemBuilder(Material.AIR).build());
                        });
                        arenaSettingsMenu.setButton(0, director.constructButton(new Pos1ArenaButton(sender, arenaName, arenaSettingsMenu)));
                        arenaSettingsMenu.setButton(1, director.constructButton(new Pos2ArenaButton(sender, arenaName, arenaSettingsMenu)));
                        arenaSettingsMenu.setButton(2, director.constructButton(new SpectatorArenaButton(sender, arenaName, arenaSettingsMenu)));
                        sender.openInventory(arenaSettingsMenu.getInventory());
                    }
                });
    }

    private static void refresh(Player sender) {
        for (Button button : arenaMenuButtons) {
            ItemMeta buttonItemMeta = button.getIcon().getItemMeta();
            if (buttonItemMeta != null) {
                if (Objects.equals(getPlugin().getConfig().getString("current-arena"), buttonItemMeta.getDisplayName())) {
                    buttonItemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                    buttonItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                } else if (!Objects.equals(getPlugin().getConfig().getString("current-arena"), buttonItemMeta.getDisplayName())) {
                    buttonItemMeta.removeEnchant(Enchantment.PROTECTION_ENVIRONMENTAL);
                }
                button.getIcon().setItemMeta(buttonItemMeta);
                arenaMenu.refreshInventory(sender);
            }
        }
    }
}
