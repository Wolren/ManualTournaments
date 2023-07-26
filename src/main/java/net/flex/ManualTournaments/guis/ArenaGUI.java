package net.flex.ManualTournaments.guis;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.factories.ArenaFactory;
import net.flex.ManualTournaments.utils.gui.buttons.Button;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.Main.gui;

public class ArenaGUI {
    public static SGMenu arenaMenu = gui.create(getPlugin().getConfig().getString("gui-arena-menu-name"), 5, "Arena");
    public static List<Button> arenaMenuButtons = new ArrayList<>();
    public static boolean opener = false;
    public static String newArenaName = "";

    public static void arenaGUI(Player sender) {
        newArenaName = "";

        Button createArena = new Button(new ItemBuilder(Material.EMERALD_BLOCK)
                .name("Create arena")
                .lore("Left click to create arena")
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        sender.closeInventory();
                        sender.sendMessage("Set the name writing: *(arena name) or cancel by writing *cancel");
                        opener = true;
                    }
                });

        arenaMenu.setToolbarBuilder((slot, page, type, menu) -> {
            if (slot == 8) return createArena;
            else return gui.getDefaultToolbarBuilder().buildToolbarButton(slot, page, type, menu);
        });

        IntStream.range(0, getPlugin().arenaNames.size()).forEach(i -> {
            Button button = createButton(getPlugin().arenaNames.get(i), sender);
            arenaMenu.setButton(i, button);
            arenaMenuButtons.add(button);
        });

        sender.openInventory(arenaMenu.getInventory());
    }

    @SneakyThrows
    private static Button createButton(String arenaName, Player sender) {
        Main.getCustomConfig().load(getPlugin().customConfigFile);
        FileConfiguration config = getPlugin().getConfig();
        ItemStack buttonItem = new ItemBuilder(Material.GRASS_BLOCK)
                .name(arenaName)
                .lore(
                        config.getString("gui-arena-lore-right-click"),
                        config.getString("gui-arena-lore-left-click"))
                .build();
        if (Objects.equals(config.getString("current-arena"), arenaName)) {
            ItemMeta buttonItemMeta = buttonItem.getItemMeta();
            if (buttonItemMeta != null) {
                buttonItemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                buttonItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            buttonItem.setItemMeta(buttonItemMeta);
            arenaMenu.refreshInventory(sender);
        }
        if (!Objects.equals(config.getString("current-arena"), arenaName)) {
            buttonItem.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
            arenaMenu.refreshInventory(sender);
        }
        Button removeArena = new Button(new ItemBuilder(Material.REDSTONE_BLOCK)
                .name("Remove arena")
                .lore("Left click to create arena")
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        sender.closeInventory();
                        ArenaFactory.getCommand("REMOVE").execute(sender, arenaName, getPlugin().arenaNames.contains(arenaName));
                    }
                });
        Button validateArena = new Button(new ItemBuilder(Material.SHEARS)
                .name("Validate arena")
                .lore("Left click to validate arena")
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        ArenaFactory.getCommand("VALIDATE").execute(sender, arenaName, getPlugin().arenaNames.contains(arenaName));
                    }
                });
        Button teleportArena = new Button(new ItemBuilder(Material.COMPASS)
                .name("Teleport to arena")
                .lore("Left click to teleport to arena")
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        sender.closeInventory();
                        ArenaFactory.getCommand("TELEPORT").execute(sender, arenaName, getPlugin().arenaNames.contains(arenaName));
                    }
                });
        return new Button(buttonItem)
                .withListener(event -> {
                    if (event.isRightClick()) {
                        getPlugin().getConfig().set("current-arena", arenaName);
                        getPlugin().getConfig().save(getPlugin().customConfigFile);
                        refresh(sender);
                    } else if (event.isLeftClick()) {
                        String name = String.format(Objects.requireNonNull(getPlugin().getConfig().getString("gui-arena-settings-menu-name")), arenaName);
                        SGMenu arenaSettingsMenu = gui.create(name, 5, name);
                        arenaSettingsMenu.setToolbarBuilder((slot, page, type, menu) -> {
                            if (slot == 8) return removeArena;
                            if (slot == 7) return validateArena;
                            if (slot == 6) return teleportArena;
                            else return new Button(new ItemBuilder(Material.AIR).build());
                        });
                        String pathPos1 = "Arenas." + arenaName + ".pos1.";
                        arenaSettingsMenu.setButton(0, new Button(new ItemBuilder(Material.MAP)
                                .name("")
                                .lore(config.getString("gui-arena-lore-color") + "x: " + config.getString("gui-arena-lore-value-color") + config.getDouble(pathPos1 + "x"),
                                        config.getString("gui-arena-lore-color") + "y: " + config.getString("gui-arena-lore-value-color") + config.getDouble(pathPos1 + "y"),
                                        config.getString("gui-arena-lore-color") + "z: " + config.getString("gui-arena-lore-value-color") + config.getDouble(pathPos1 + "z"),
                                        config.getString("gui-arena-lore-color") + "yaw: " + config.getString("gui-arena-lore-value-color") + config.getDouble(pathPos1 + "yaw"),
                                        config.getString("gui-arena-lore-color") + "pitch: " + config.getString("gui-arena-lore-value-color") + config.getDouble(pathPos1 + "pitch"),
                                        config.getString("gui-arena-lore-color") + "world: " + config.getString("gui-arena-lore-value-color") + config.getString(pathPos1 + "world"))
                                .build()).withListener(event1 -> {
                                    ArenaFactory.getCommand("POS1").execute(sender, arenaName, getPlugin().arenaNames.contains(arenaName));
                        }));
                        arenaSettingsMenu.setButton(1, new Button(new ItemBuilder(Material.MAP).build()));
                        arenaSettingsMenu.setButton(2, new Button(new ItemBuilder(Material.MAP).build()));
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
