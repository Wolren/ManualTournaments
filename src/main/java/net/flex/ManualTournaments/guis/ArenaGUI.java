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

import static net.flex.ManualTournaments.Main.*;

public class ArenaGUI {
    public static SGMenu arenaMenu = gui.create(getPlugin().getConfig().getString("gui-arena-menu-name"), 5, "Arena");
    public static List<Button> arenaMenuButtons = new ArrayList<>();
    public static boolean opener = false;
    public static String newArenaName = "";
    static FileConfiguration config = getPlugin().getConfig();

    public static void arenaGUI(Player sender) {
        newArenaName = "";

        Button createArena = new Button(new ItemBuilder(Material.EMERALD_BLOCK)
                .name(config.getString("gui-arena-create-name"))
                .lore(config.getString("gui-arena-create-lore"))
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
        ItemStack buttonItem = new ItemBuilder(Material.GRASS_BLOCK)
                .name(config.getString("gui-arena-name-color") + arenaName)
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
                .name(config.getString("gui-arena-settings-remove-name"))
                .lore(config.getString("gui-arena-settings-remove-lore"))
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        sender.closeInventory();
                        ArenaFactory.getCommand("REMOVE").execute(sender, arenaName, getPlugin().arenaNames.contains(arenaName));
                    }
                });
        Button validateArena = new Button(new ItemBuilder(Material.SHEARS)
                .name(config.getString("gui-arena-settings-validate-name"))
                .lore(config.getString("gui-arena-settings-validate-lore"))
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        ArenaFactory.getCommand("VALIDATE").execute(sender, arenaName, getPlugin().arenaNames.contains(arenaName));
                    }
                });
        Button teleportArena = new Button(new ItemBuilder(Material.COMPASS)
                .name(config.getString("gui-arena-settings-teleport-name"))
                .lore(config.getString("gui-arena-settings-teleport-lore"))
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        sender.closeInventory();
                        ArenaFactory.getCommand("TELEPORT").execute(sender, arenaName, getPlugin().arenaNames.contains(arenaName));
                    }
                });
        Button backButton = new Button(new ItemBuilder(Material.ARROW)
                .name("&e&lGo back")
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        sender.openInventory(arenaMenu.getInventory());
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
                        SGMenu arenaSettingsMenu = gui.create(name, 2, name);
                        arenaSettingsMenu.setToolbarBuilder((slot, page, type, menu) -> {
                            if (slot == 8) return removeArena;
                            if (slot == 7) return validateArena;
                            if (slot == 6) return teleportArena;
                            if (slot == 4) return backButton;
                            else return new Button(new ItemBuilder(Material.AIR).build());
                        });
                        String pathPos1 = "Arenas." + arenaName + ".pos1.";
                        String pathPos2 = "Arenas." + arenaName + ".pos2.";
                        String pathSpectator = "Arenas." + arenaName + ".spectator.";
                        arenaSettingsMenu.setButton(0, new Button(new ItemBuilder(Material.MAP)
                                .name(config.getString("gui-arena-settings-pos1-name"))
                                .lore(config.getString("gui-arena-settings-lore-color") + "x: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathPos1 + "x"),
                                        config.getString("gui-arena-settings-lore-color") + "y: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathPos1 + "y"),
                                        config.getString("gui-arena-settings-lore-color") + "z: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathPos1 + "z"),
                                        config.getString("gui-arena-settings-lore-color") + "yaw: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathPos1 + "yaw"),
                                        config.getString("gui-arena-settings-lore-color") + "pitch: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathPos1 + "pitch"),
                                        config.getString("gui-arena-settings-lore-color") + "world: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getString(pathPos1 + "world"))
                                .build())
                                .withListener(event1 -> {
                                    ArenaFactory.getCommand("POS1").execute(sender, arenaName, getPlugin().arenaNames.contains(arenaName));
                                    arenaSettingsMenu.refreshInventory(sender);
                                }));
                        arenaSettingsMenu.setButton(1, new Button(new ItemBuilder(Material.MAP)
                                .name(config.getString("gui-arena-settings-pos2-name"))
                                .lore(config.getString("gui-arena-settings-lore-color") + "x: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathPos2 + "x"),
                                        config.getString("gui-arena-settings-lore-color") + "y: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathPos2 + "y"),
                                        config.getString("gui-arena-settings-lore-color") + "z: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathPos2 + "z"),
                                        config.getString("gui-arena-settings-lore-color") + "yaw: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathPos2 + "yaw"),
                                        config.getString("gui-arena-settings-lore-color") + "pitch: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathPos2 + "pitch"),
                                        config.getString("gui-arena-settings-lore-color") + "world: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getString(pathPos2 + "world"))
                                .build()).withListener(event1 -> {
                                    ArenaFactory.getCommand("POS2").execute(sender, arenaName, getPlugin().arenaNames.contains(arenaName));
                                    arenaSettingsMenu.refreshInventory(sender);
                        }));
                        arenaSettingsMenu.setButton(2, new Button(new ItemBuilder(Material.MAP)
                                .name(config.getString("gui-arena-settings-spectator-name"))
                                .lore(config.getString("gui-arena-settings-lore-color") + "x: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathSpectator + "x"),
                                        config.getString("gui-arena-settings-lore-color") + "y: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathSpectator + "y"),
                                        config.getString("gui-arena-settings-lore-color") + "z: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathSpectator + "z"),
                                        config.getString("gui-arena-settings-lore-color") + "yaw: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathSpectator + "yaw"),
                                        config.getString("gui-arena-settings-lore-color") + "pitch: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(pathSpectator + "pitch"),
                                        config.getString("gui-arena-settings-lore-color") + "world: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getString(pathSpectator + "world"))
                                .build()).withListener(event1 -> {
                                    ArenaFactory.getCommand("SPECTATOR").execute(sender, arenaName, getPlugin().arenaNames.contains(arenaName));
                                    arenaSettingsMenu.refreshInventory(sender);
                                }));
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
