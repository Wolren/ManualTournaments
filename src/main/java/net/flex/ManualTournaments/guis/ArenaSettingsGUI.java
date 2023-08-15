package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.arenaButtons.*;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.gui;
import static net.flex.ManualTournaments.utils.SharedComponents.config;

public class ArenaSettingsGUI {
    public static void arenaSettingsGUI(Player sender, String arenaName) {
        String name = String.format(Objects.requireNonNull(config.getString("gui-arena-settings-menu-name")), arenaName);
        Menu arenaSettingsMenu = gui.create(name, 2);
        arenaSettingsMenu.setBlockDefaultInteractions(true);
        arenaSettingsMenu.setToolbarBuilder((slot, page, type, menu) -> {
            if (slot == 8) return ArenaGUI.director.constructButton(new RemoveArenaButton(sender, arenaName));
            if (slot == 7) return ArenaGUI.director.constructButton(new ValidateArenaButton(sender, arenaName));
            if (slot == 6) return ArenaGUI.director.constructButton(new TeleportArenaButton(sender, arenaName));
            if (slot == 4) return ArenaGUI.director.constructButton(new BackArenaButton(sender));
            else return new Button(new ItemBuilder(Material.AIR).build());
        });
        Button pos1Button = ArenaGUI.director.constructButton(new Pos1ArenaButton(sender, arenaName, arenaSettingsMenu));
        arenaSettingsMenu.setButton(0, pos1Button);
        Button pos2Button = ArenaGUI.director.constructButton(new Pos2ArenaButton(sender, arenaName, arenaSettingsMenu));
        arenaSettingsMenu.setButton(1, pos2Button);
        Button spectatorButton = ArenaGUI.director.constructButton(new SpectatorArenaButton(sender, arenaName, arenaSettingsMenu));
        arenaSettingsMenu.setButton(2, spectatorButton);
        sender.openInventory(arenaSettingsMenu.getInventory());
    }
}
