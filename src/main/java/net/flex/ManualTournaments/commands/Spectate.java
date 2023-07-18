package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedMethods.*;

@SuppressWarnings("deprecation")
public class Spectate implements TabCompleter, CommandExecutor {
    private static final FileConfiguration config = getPlugin().getConfig();
    private final FileConfiguration ArenaConfig = getPlugin().getArenaConfig();
    public static List<Player> spectators = new ArrayList<>();
    GameMode gameMode = Bukkit.getServer().getDefaultGameMode();
    Player player = null;

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        loadConfigs();
        if (args.length == 0) {
            if (getPlugin().arenaNames.contains(config.getString("current-arena"))) {
                String path = "Arenas." + config.getString("current-arena") + "." + "spectator" + ".";
                if (ArenaConfig.isSet(path)) {
                    spectators.add(player);
                    for (Player p : Bukkit.getOnlinePlayers()) p.hidePlayer(player);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.setCollidable(false);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.teleport(location(path, ArenaConfig));
                    send(player, "spectator-started-spectating");
                } else {
                    send(player, "arena-not-set");
                    return true;
                }
            } else {
                send(player, "current-arena-not-set");
                return true;
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("stop")) {
                if (config.getBoolean("kill-on-fight-end")) {
                    player.setGameMode(gameMode);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.damage(10000);
                    send(player, "spectator-stopped-spectating");
                } else {
                    player.setGameMode(gameMode);
                    for (Player other : Bukkit.getServer().getOnlinePlayers()) other.showPlayer(player);
                    player.teleport(location("fight-end-spawn.", config));
                    send(player, "spectator-stopped-spectating");
                }
                spectators.remove(player);
            } else send(player, "not-allowed");
        } else return false;
        return true;
    }

    @SneakyThrows
    private void loadConfigs() {
        config.load(getPlugin().customConfigFile);
        ArenaConfig.load(getPlugin().ArenaConfigFile);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) return Collections.singletonList("stop");
        return null;
    }
}
