package net.flex.ManualTournaments.commands.kitCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.KitCommand;
import org.bukkit.entity.Player;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.config;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class RemoveKit implements KitCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String kitName, boolean kitExists) {
        if (kitExists) {
            if (Objects.requireNonNull(config.getString("current-kit")).equalsIgnoreCase(kitName)) {
                config.set("current-kit", null);
                config.save(getCustomConfigFile());
            }
            getKitConfig().set("Kits." + kitName, null);
            getKitConfig().save(getKitConfigFile());
            Main.kitNames.remove(kitName);
            send(player, "kit-removed");
        } else send(player, "kit-not-exists");
    }
}
