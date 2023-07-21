package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class Spectate implements TabCompleter, CommandExecutor {
    private static final FileConfiguration config = getPlugin().getConfig();
    public static List<UUID> spectators = new ArrayList<>();
    private static final Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    public static Team spectatorsBoard = board.registerNewTeam("spectators");
    Player player = null;

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        config.load(getPlugin().customConfigFile);
        getArenaConfig().load(getPlugin().ArenaConfigFile);
        if (args.length == 0) setSpectator(player);
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("stop")) {
                stopSpectator(player);
            } else send(player, "not-allowed");
        } else send(player, "spectator-usage");
        return true;
    }

    private void setSpectator(Player player) {
        if (getPlugin().arenaNames.contains(config.getString("current-arena"))) {
            String path = "Arenas." + config.getString("current-arena") + "." + "spectator" + ".";
            if (getArenaConfig().isSet(path)) {
                if (Main.version >= 14) {
                    spectatorsBoard.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                    player.setCollidable(false);
                } else collidableReflection(player);
                player.teleport(location(path, getArenaConfig()));
                send(player, "spectator-started-spectating");
            } else send(player, "arena-not-set");
        } else send(player, "current-arena-not-set");
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFoodLevel(20);
        player.setHealth(20.0D);
        spectatorsBoard.addEntry(player.getName());
        if (!config.getBoolean("spectator-visibility")) {
            for (Player other : Bukkit.getServer().getOnlinePlayers()) {
                other.hidePlayer(getPlugin(), player);
            }
        }
        clear(player);
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack itemStack = new ItemStack(Material.RED_DYE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null)
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lStop spectating"));
        itemStack.setItemMeta(itemMeta);
        inventory[8] = itemStack;
        player.getInventory().setContents(inventory);
        spectators.add(player.getUniqueId());
    }

    public static void stopSpectator(Player player) {
        player.setGameMode(Bukkit.getServer().getDefaultGameMode());
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getInventory().clear();
        for (Player other : Bukkit.getServer().getOnlinePlayers()) other.showPlayer(getPlugin(), player);
        send(player, "spectator-stopped-spectating");
        spectatorsBoard.removeEntry(player.getName());
        if (Main.version >= 14) player.setCollidable(true);
        spectators.remove(player.getUniqueId());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) return Collections.singletonList("stop");
        return null;
    }
}
