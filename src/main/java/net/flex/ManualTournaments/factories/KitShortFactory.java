package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.kitCommands.GuiKit;
import net.flex.ManualTournaments.commands.kitCommands.ListKit;
import net.flex.ManualTournaments.commands.kitCommands.UnbreakableKit;
import net.flex.ManualTournaments.interfaces.KitShortCommand;

import java.util.HashMap;
import java.util.Map;

import static net.flex.ManualTournaments.Main.kitNames;

public class KitShortFactory {
    private static final Map<String, KitShortCommand> kitShortCommandMap = new HashMap<String, KitShortCommand>() {{
        put("GUI", new GuiKit());
        put("LIST", new ListKit());
        put("UNBREAKABLE", new UnbreakableKit());
    }};

    public static KitShortCommand getCommand(String command) {
        return kitShortCommandMap.getOrDefault(command, (player, arg) -> KitFactory.getCommand("GIVE").execute(player, arg, kitNames.contains(arg)));
    }
}
