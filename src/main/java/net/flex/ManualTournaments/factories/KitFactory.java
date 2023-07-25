package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.kitCommands.CreateKit;
import net.flex.ManualTournaments.commands.kitCommands.GiveKit;
import net.flex.ManualTournaments.commands.kitCommands.RemoveKit;
import net.flex.ManualTournaments.interfaces.KitCommand;

import java.util.HashMap;
import java.util.Map;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class KitFactory {
    private static final Map<String, KitCommand> kitCommandMap = new HashMap<String, KitCommand>() {{
        put("CREATE", new CreateKit());
        put("REMOVE", new RemoveKit());
        put("GIVE", new GiveKit());
    }};

    public static KitCommand getCommand(String command) {
        return kitCommandMap.getOrDefault(command, (player, kitName, kitExists) -> send(player, "kit-usage"));
    }
}
