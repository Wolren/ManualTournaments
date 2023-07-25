package net.flex.ManualTournaments.guis;

import net.flex.ManualTournaments.utils.gui.buttons.Button;
import net.flex.ManualTournaments.utils.gui.buttons.ButtonListener;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import static net.flex.ManualTournaments.Main.*;

public class SpectatorGUI {
    public static void teleportationGUI(Player sender) {
        SGMenu sgMenu = gui.create("Teleportation Menu", 5);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (sender != player) {
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                if (skullMeta != null) {
                    skullMeta.setOwningPlayer(player);
                    skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getPlugin().getConfig().getString("spectator-gui-color") + player.getDisplayName()));
                }
                playerHead.setItemMeta(skullMeta);
                Button teleportButton = new Button(playerHead);
                ButtonListener listener = event -> {
                    sender.teleport(player.getLocation());
                    sender.closeInventory();
                };
                teleportButton.setListener(listener);
                sgMenu.addButton(teleportButton);
            }
            sender.openInventory(sgMenu.getInventory());
        }
    }
}
