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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class Spectate implements TabCompleter, CommandExecutor {
    public static Collection<UUID> spectators = new HashSet<>();
    private static final Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    public static Team spectatorsBoard = board.registerNewTeam("spectators");

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        config.load(getCustomConfigFile());
        getArenaConfig().load(getArenaConfigFile());
        if (args.length == 0) setSpectator(player);
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("stop")) {
                stopSpectator(player);
            } else send(player, "not-allowed");
        } else send(player, "spectator-usage");
        return true;
    }

    private void setSpectator(Player player) {
        if (Main.arenaNames.contains(config.getString("current-arena"))) {
            String path = "Arenas." + config.getString("current-arena") + ".spectator.";
            if (Main.getArenaConfig().isSet(path)) {
                if (Main.version >= 14) {
                    spectatorsBoard.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                    player.setCollidable(false);
                } else collidableReflection(player, false);

                player.setGameMode(GameMode.ADVENTURE);
                player.setFoodLevel(20);
                player.setHealth(20.0D);
                spectatorsBoard.addEntry(player.getName());
                Bukkit.getServer().getOnlinePlayers().forEach(other -> other.hidePlayer(player));
                clear(player);
                spectators.add(player.getUniqueId());
                player.teleport(location(path, Main.getArenaConfig()));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setAllowFlight(true);
                    }
                }.runTaskLater(getPlugin(), 6L);

                ItemStack redstoneBlock = new ItemStack(Material.REDSTONE_BLOCK);
                ItemMeta redstoneBlockMeta = redstoneBlock.getItemMeta();
                if (redstoneBlockMeta != null) {
                    redstoneBlockMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lStop spectating"));
                }
                redstoneBlock.setItemMeta(redstoneBlockMeta);

                ItemStack compass = new ItemStack(Material.COMPASS);
                ItemMeta compassMeta = compass.getItemMeta();
                if (compassMeta != null) {
                    compassMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7&lTeleportation menu"));
                }
                compass.setItemMeta(compassMeta);

                ItemStack[] inventory = player.getInventory().getContents();
                inventory[8] = redstoneBlock;
                inventory[0] = compass;
                player.getInventory().setContents(inventory);

                send(player, "spectator-started-spectating");
            } else send(player, "arena-spectator-not-set");
        } else send(player, "current-arena-not-set");
    }

    public static void stopSpectator(Player player) {
        player.setGameMode(Bukkit.getServer().getDefaultGameMode());
        if (Main.version <= 13) collidableReflection(player, true);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getInventory().clear();
        if (config.getBoolean("kill-on-fight-end")) player.setHealth(0);
        else {
            String path = "fight-end-spawn.";
            if (config.isSet(path)) {
                player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                clear(player);
                player.teleport(location(path, config));
            }
        }
        Bukkit.getServer().getOnlinePlayers().forEach(other -> other.showPlayer(player));
        send(player, "spectator-stopped-spectating");
        spectatorsBoard.removeEntry(player.getName());
        if (Main.version >= 14) player.setCollidable(true);
        spectators.remove(player.getUniqueId());
    }

    public static void stopWithoutKill(Player player) {
        if (Main.version <= 13) collidableReflection(player, true);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getInventory().clear();
        Bukkit.getServer().getOnlinePlayers().forEach(other -> other.showPlayer(player));
        send(player, "spectator-stopped-spectating");
        spectatorsBoard.removeEntry(player.getName());
        if (Main.version >= 14) player.setCollidable(true);
        spectators.remove(player.getUniqueId());
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (args.length == 1) return Collections.singletonList("stop");
        return null;
    }
}
