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
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedMethods.*;

@SuppressWarnings("deprecation")
public class Spectate implements TabCompleter, CommandExecutor {
    private static final FileConfiguration config = getPlugin().getConfig();
    private final FileConfiguration ArenaConfig = getPlugin().getArenaConfig();
    public static List<UUID> spectators = new ArrayList<>();
    private static final Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    public static Team spectatorsBoard = board.registerNewTeam("spectators");
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
                    if (Main.version >= 14) {
                        spectatorsBoard.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                        player.setCollidable(false);
                    }
                    else {
                        try {
                            Class<?> spigotEntityClass = Class.forName("org.bukkit.entity.Player$Spigot");
                            Method setCollidesWithEntities = spigotEntityClass.getMethod("setCollidesWithEntities", boolean.class);
                            setCollidesWithEntities.invoke(player.spigot(), false);
                        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                                 InvocationTargetException exception) {
                            throw new RuntimeException(exception);
                        }
                    }
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
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFoodLevel(20);
            player.setHealth(20.0D);
            spectatorsBoard.addEntry(player.getName());
            if (!config.getBoolean("spectator-visibility")) {
                for (Player other : Bukkit.getServer().getOnlinePlayers()) {
                    other.hidePlayer(player);
                }
            }
            player.getInventory().clear();
            player.updateInventory();
            player.setHealth(20.0D);
            player.setFoodLevel(20);
            player.setSaturation(0);
            if (Main.version >= 22) player.setAbsorptionAmount(0);
            player.setFireTicks(0);
            for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
            ItemStack[] inventory = player.getInventory().getContents();
            ItemStack itemStack = new ItemStack(Material.RED_DYE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&c&lStop spectating"));
            itemStack.setItemMeta(itemMeta);
            inventory[8] = itemStack;
            player.getInventory().setContents(inventory);
            player.updateInventory();
            spectators.add(player.getUniqueId());
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("stop")) {
                player.setGameMode(gameMode);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.getInventory().clear();
                for (Player other : Bukkit.getServer().getOnlinePlayers()) other.showPlayer(player);
                send(player, "spectator-stopped-spectating");
                spectatorsBoard.removeEntry(player.getName());
                if (Main.version >= 14) player.setCollidable(true);
                spectators.remove(player.getUniqueId());
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
