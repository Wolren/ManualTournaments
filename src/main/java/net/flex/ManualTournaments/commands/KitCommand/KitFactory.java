package net.flex.ManualTournaments.commands.KitCommand;

import net.flex.ManualTournaments.commands.KitCommand.Implementations.CreateKit;
import net.flex.ManualTournaments.commands.KitCommand.Implementations.GiveKit;
import net.flex.ManualTournaments.commands.KitCommand.Implementations.RemoveKit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KitFactory {
    private static final Map<String, KitCommand> kitCommandMap;

    static {
        kitCommandMap = new ConcurrentHashMap<>();
        kitCommandMap.put("CREATE", new CreateKit());
        kitCommandMap.put("REMOVE", new RemoveKit());
        kitCommandMap.put("GIVE", new GiveKit());
    }

    public static KitCommand getCommand(String command) {
        return kitCommandMap.getOrDefault(command, new KitCommand() {
            @Override
            public void execute(Player player, String kitName, boolean kitExists) {
                // Default implementation for execute(Player player, String kitName, boolean kitExists)
            }

        });
    }
}
