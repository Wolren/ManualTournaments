package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static net.flex.ManualTournaments.Main.*;

public final class Reload implements CommandExecutor, TabCompleter {
    @SneakyThrows
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if ((args.length == 1 && args[0].equals("reload")) || args.length == 0) {
            getPlugin().getLogger().info("Reloading...");
            getKitConfig().load(getKitConfigFile());
            getArenaConfig().load(getArenaConfigFile());
            getCustomConfig().load(getCustomConfigFile());
            getPlugin().getLogger().info("Reloading complete");
            return true;
        } else return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return Collections.singletonList("reload");
        else return null;
    }
}
