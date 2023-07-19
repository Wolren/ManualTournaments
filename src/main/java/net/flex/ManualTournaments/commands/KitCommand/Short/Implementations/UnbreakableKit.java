package net.flex.ManualTournaments.commands.KitCommand.Short.Implementations;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.KitCommand.Short.KitCommandType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class UnbreakableKit implements KitCommandType {
    @Override
    public boolean kitCommand(Player player, String arg) {
        if (Main.version >= 17) {
            for (ItemStack im : player.getInventory().getContents()) {
                if (im != null && im.getType().getMaxDurability() != 0) {
                    ItemMeta unbreakable = im.getItemMeta();
                    if (unbreakable != null) unbreakable.setUnbreakable(true);
                    im.setItemMeta(unbreakable);
                }
            }
            player.updateInventory();
            send(player, "kit-set-unbreakable");
        } else send(player, "not-supported");
        return true;
    }
}
