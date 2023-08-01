package net.flex.ManualTournaments.commands.kitCommands;

import net.flex.ManualTournaments.interfaces.KitCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getKitConfig;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class RemoveKit implements KitCommand {
    @Override
    public void execute(Player player, String kitName, boolean kitExists) {
        if (kitExists) {
            getKitConfig().set("Kits." + kitName, null);
            getPlugin().kitNames.remove(kitName);
            send(player, "kit-removed");
        } else send(player, "kit-not-exists");
    }
}
