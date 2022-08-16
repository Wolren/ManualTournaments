package net.flex.FlexTournaments.api;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

import java.util.HashMap;

public class CommandManager {
    private static final HashMap<String, Command> commands = new HashMap();
    private static final Reflection.FieldAccessor<SimpleCommandMap> f = Reflection.getField(SimplePluginManager.class, "commandMap", SimpleCommandMap.class);
    private static CommandMap cmdMap = (CommandMap)f.get(Bukkit.getServer().getPluginManager());

    public CommandManager() {
    }

    public static void register(Command cmd) {
        if (cmdMap == null) {
            cmdMap = (CommandMap)f.get(Bukkit.getServer().getPluginManager());
        }

        cmdMap.register(cmd.getName(), cmd);
        commands.put(cmd.getName(), cmd);
    }
}
