package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.utils.gui.buttons.Button;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.Main.gui;

public class SpectatorGUI {
    public static void teleportationGUI(Player sender) {
        SGMenu menu = gui.create("Teleportation Menu", 5);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (sender != player) {
                Button teleportButton = new Button(new ItemBuilder(Material.PLAYER_HEAD)
                        .skullOwner(player)
                        .name(getPlugin().getConfig().getString("gui-spectator-color") + player.getDisplayName())
                        .build())
                        .withListener(event -> {
                            sender.teleport(player.getLocation());
                            sender.closeInventory();
                        });
                menu.addButton(teleportButton);
            }
            sender.openInventory(menu.getInventory());
        }
    }
}
