package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.Main.gui;

public class SpectatorGUI {
    public static SGMenu spectatorMenu = gui.create("Teleportation Menu", 5, "Spectator");

    public static void teleportationGUI(Player sender) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (sender != player) spectatorMenu.addButton(teleportButton(sender, player));
        });
        sender.openInventory(spectatorMenu.getInventory());
    }

    private static Button teleportButton(Player sender, Player player) {
        return new Button(new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(player)
                .name(getPlugin().getConfig().getString("gui-spectator-color") + player.getDisplayName())
                .build())
                .withListener(event -> {
                    sender.teleport(player.getLocation());
                    sender.closeInventory();
                });
    }
}
