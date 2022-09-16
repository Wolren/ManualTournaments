package net.flex.FlexTournaments;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class Settings implements TabCompleter, CommandExecutor {
    private static final FileConfiguration config = Main.getPlugin().getConfig();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        try {
            config.load(Main.getPlugin().customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.conf("sender-not-a-player"));
        } else {
            Player player = ((Player) sender).getPlayer();
            assert player != null;
            if (args.length == 0) {
                player.sendMessage(Main.conf(config.getString("wrong-arguments")));
            } else if (args.length == 1) {
                if (args[0].equals("endspawn")) {
                    String path = "fight-end-spawn.";
                    Arena.getLocation(path, player, config);
                }
            } else if (args.length == 2) {

            } else {
                player.sendMessage(Main.conf("wrong-arguments"));
            }
            try {
                config.save(Main.getPlugin().customConfigFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}
