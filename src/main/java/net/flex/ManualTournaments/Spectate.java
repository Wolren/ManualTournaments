package net.flex.ManualTournaments;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.Shared.*;

@SuppressWarnings("deprecation")
public class Spectate implements TabCompleter, CommandExecutor {
    private static final FileConfiguration config = getPlugin().getConfig();
    private final FileConfiguration ArenaConfig = getPlugin().getArenaConfig();
    static List<Player> spectators = new ArrayList<>();
    GameMode gameMode = Bukkit.getServer().getDefaultGameMode();

    public Spectate() {
    }

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Optional<Player> playerOptional = Optional.ofNullable(((OfflinePlayer) sender).getPlayer());
        if (!playerOptional.isPresent() || !(sender instanceof Player)) {
            sender.sendMessage("sender-not-a-player");
            return false;
        }
        loadConfigs();
        Player player = playerOptional.get();
        if (args.length == 0) {
            if (getPlugin().arenaNames.contains(config.getString("current-arena"))) {
                String path = "Arenas." + config.getString("current-arena") + "." + "spectator" + ".";
                if (ArenaConfig.isSet(path)) {
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
            if (!config.getBoolean("spectator-visibility")) {
                for (Player other : Bukkit.getServer().getOnlinePlayers()) other.hidePlayer(player);
            } else {
                for (Player other : Bukkit.getServer().getOnlinePlayers()) other.showPlayer(player);
            }
            if (Objects.equals(config.getString("spectator-gamemode"), "spectator"))
                player.setGameMode(GameMode.SPECTATOR);
            else if (Objects.equals(config.getString("spectator-gamemode"), "adventure"))
                player.setGameMode(GameMode.ADVENTURE);
            else if (Objects.equals(config.getString("spectator-gamemode"), "survival"))
                player.setGameMode(GameMode.SURVIVAL);
            else if (Objects.equals(config.getString("spectator-gamemode"), "creative"))
                player.setGameMode(GameMode.CREATIVE);
            else send(player, "spectator-wrong-arguments");
            spectators.add(player);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("stop")) {
                if (config.getBoolean("kill-on-fight-end")) {
                    player.setGameMode(gameMode);
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
