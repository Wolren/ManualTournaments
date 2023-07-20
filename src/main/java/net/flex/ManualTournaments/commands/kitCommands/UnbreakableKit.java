package net.flex.ManualTournaments.commands.kitCommands;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.KitShortCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class UnbreakableKit implements KitShortCommand {
    @Override
    public void execute(Player player, String arg) {
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
    }
}
