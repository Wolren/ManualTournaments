package net.flex.FlexTournaments;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class KitTab implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> a =  Main.getPlugin().kitNames;
            a.add("list");
            return Main.getPlugin().kitNames;
        }

        return null;
    }
}
