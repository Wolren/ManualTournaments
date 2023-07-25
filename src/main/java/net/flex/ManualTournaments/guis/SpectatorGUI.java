package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.utils.SpiGUI.buttons.SGButton;
import net.flex.ManualTournaments.utils.SpiGUI.buttons.SGButtonListener;
import net.flex.ManualTournaments.utils.SpiGUI.menu.SGMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import static net.flex.ManualTournaments.Main.getCustomConfig;
import static net.flex.ManualTournaments.Main.gui;

public class SpectatorGUI {
    public void teleportationGUI(Player sender) {
        SGMenu sgMenu = gui.create("Player Teleportation Menu", 5);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (sender != player) {
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                if (skullMeta != null) {
                    skullMeta.setOwningPlayer(player);
                    skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getCustomConfig().getString("spectator-color") + player.getDisplayName()));
                }
                playerHead.setItemMeta(skullMeta);
                SGButton teleportButton = new SGButton(playerHead);
                SGButtonListener listener = event -> {
                    sender.teleport(player.getLocation());
                    sender.closeInventory();
                };
                teleportButton.setListener(listener);
                sgMenu.addButton(teleportButton);
                sender.openInventory(sgMenu.getInventory());
            }
        }
    }
}
