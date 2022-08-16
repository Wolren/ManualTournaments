package net.flex.FlexTournaments;

import net.flex.FlexTournaments.api.Command;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Kit extends Command {
    String kitNames = "";

    public Kit() {
        super("FlexTournaments", "", "", "", "kit");
    }

    public boolean onExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Sender must be a player");
        } else {
            Player player = ((Player) sender).getPlayer();
            FileConfiguration config = Main.getPlugin().getConfig();
            assert player != null;
            PlayerInventory inv = player.getInventory();
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + Main.getPlugin().getConfig().getString("kit-no-arguments"));
            } else if (args.length == 1) {
                if (kitNames == null || !kitNames.contains(args[0])) {
                    sender.sendMessage(ChatColor.RED + Main.getPlugin().getConfig().getString("kit-not-exists"));
                } else if (args[0].equals("list")) {
                    sender.sendMessage(kitNames);
                }
            } else if (args.length == 2 & args[0].equals("create")) {
                if (kitNames != null) {
                    if (kitNames.contains(args[1])) {
                        sender.sendMessage(ChatColor.RED + Main.getPlugin().getConfig().getString("kit-already-exists"));
                        return false;
                    } else {
                        kitNames = (kitNames + ", " + args[1]);
                        for (ItemStack item : player.getInventory()) {
                            if (item != null) {
                                player.sendMessage(item.toString());
                            }
                        }
                    }
                } else {
                    kitNames = (args[1]);
                    for (ItemStack itemStack : player.getInventory()) {
                        player.sendMessage(itemStack.toString());
                    }
                }
            }
        }


        return false;
    }
}