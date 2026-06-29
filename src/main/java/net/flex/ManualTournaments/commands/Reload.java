package net.flex.ManualTournaments.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static net.flex.ManualTournaments.Main.*;

public final class Reload implements CommandExecutor, TabCompleter {
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String string, @Nonnull String[] args) {
        try {
            if ((args.length == 1 && args[0].equals("reload")) || args.length == 0) {
                getPlugin().getLogger().info("Reloading...");
                getKitConfig().load(getKitConfigFile());
                getArenaConfig().load(getArenaConfigFile());
                getCustomConfig().load(getCustomConfigFile());
                getPlugin().reloadConfig();
                getPlugin().getLogger().info("Reloading complete");
                return true;
            } else return false;
        } catch (IOException | InvalidConfigurationException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Failed to reload configs", e);
            return false;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (args.length == 1) return Collections.singletonList("reload");
        else return null;
    }
}
