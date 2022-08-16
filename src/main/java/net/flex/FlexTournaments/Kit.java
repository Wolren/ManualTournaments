package net.flex.FlexTournaments;

import net.flex.FlexTournaments.api.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Kit extends Command {
    public List<String> kitNames;
    public Kit() {
        super("FlexTournaments", "", "", "", new String[]{"kit"});
    }

    public boolean onExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Sender must be a player");
        } else {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + Main.getPlugin().getConfig().getString("kit-no-arguments"));
            } else if (args.length == 1) {
                Player player = ((Player) sender).getPlayer();
                if (kitNames == null || !kitNames.contains(args[0])) {
                    assert player != null;
                    player.sendMessage(ChatColor.RED + Main.getPlugin().getConfig().getString("kit-not-exists"));
                }
            }
        }


        return false;
    }
}