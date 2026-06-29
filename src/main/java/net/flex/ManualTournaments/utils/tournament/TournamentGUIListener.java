package net.flex.ManualTournaments.utils.tournament;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for bracket GUI interactions.
 * Clicking a match item shows info; clicking a COMPASS teleports to spectate.
 */
public class TournamentGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("§8Bracket: §e")) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        String displayName = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "";

        // Teleport to match if compass or iron sword clicked
        if (item.getType() == Material.COMPASS || item.getType() == Material.IRON_SWORD) {
            // Extract tournament name from title
            String tName = ChatColor.stripColor(title).replace("Bracket: ", "").trim();
            Tournament t = TournamentManager.getInstance().getTournament(tName);
            if (t != null && t.getPhase() == Tournament.Phase.IN_PROGRESS) {
                player.chat("/tournament spectate " + tName);
            } else {
                player.sendMessage("§6Tournament is not in progress.");
            }
        } else if (item.getType() == Material.GOLDEN_SWORD) {
            // Winner sword — show winner info
            String stripped = ChatColor.stripColor(displayName);
            player.sendMessage("§6Match result: §e" + stripped);
        } else if (item.getType() == Material.PAPER) {
            // Info paper — tell player to use /tournament info
            String[] lore = item.getItemMeta() != null && item.getItemMeta().getLore() != null
                    ? item.getItemMeta().getLore().toArray(new String[0]) : new String[0];
            for (String line : lore) {
                player.sendMessage(line);
            }
        }
    }
}
