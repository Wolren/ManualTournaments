package net.flex.ManualTournaments.utils;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.commands.Fight;
import net.flex.ManualTournaments.commands.fightCommands.TeamFight;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.commands.Fight.teams;

public class SharedComponents {
    public static FileConfiguration config = getPlugin().getConfig();
    public static Player player = null;

    public static String message(String s) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getPlugin().getConfig().getString(s)));
    }

    public static void send(Player player, String s) {
        player.sendMessage(message(s));
    }

    public static Player optional(CommandSender sender) {
        Optional<Player> playerOptional = Optional.ofNullable(((OfflinePlayer) sender).getPlayer());
        if (!playerOptional.isPresent() || !(sender instanceof Player)) {
            sender.sendMessage("sender-not-a-player");
            return null;
        } else return playerOptional.get();
    }

    public static void sendNotExists(Player player) {
        player.sendMessage(message("arena-not-exists"));
    }

    @SneakyThrows
    public static void getLocation(String pathing, Player player, ConfigurationSection cfg) {
        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        String world = Objects.requireNonNull(player.getLocation().getWorld()).getName();
        cfg.set(pathing + "x", x);
        cfg.set(pathing + "y", y);
        cfg.set(pathing + "z", z);
        cfg.set(pathing + "yaw", yaw);
        cfg.set(pathing + "pitch", pitch);
        cfg.set(pathing + "world", world);
        getArenaConfig().save(getArenaConfigFile());
    }

    public static void collidableReflection(Player fighter, boolean value) {
        try {
            Class<?> spigotEntityClass = Class.forName("org.bukkit.entity.Player$Spigot");
            Method setCollidesWithEntities = spigotEntityClass.getMethod("setCollidesWithEntities", boolean.class);
            setCollidesWithEntities.invoke(fighter.spigot(), value);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Location location(String path, FileConfiguration cfg) {
        World world = Bukkit.getWorld(Objects.requireNonNull(cfg.get(path + "world")).toString());
        double x = cfg.getDouble(path + "x");
        double y = cfg.getDouble(path + "y");
        double z = cfg.getDouble(path + "z");
        float yaw = (float) cfg.getDouble(path + "yaw");
        float pitch = (float) cfg.getDouble(path + "pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static String teamList(String teamName) {
        Set<String> teamString = new HashSet<>();
        for (Map.Entry<Team, Set<UUID>> entry : Fight.teams.entrySet()) {
            if (entry.getKey().getName().equals(teamName)) {
                for (UUID uuid : entry.getValue()) {
                    teamString.add(Objects.requireNonNull(Bukkit.getOfflinePlayer(uuid)).getName());
                }
            }
        }
        return String.join(", ", teamString);
    }

    public static void clear(Player player) {
        player.getInventory().clear();
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setSaturation(0);
        player.setFireTicks(0);
        if (Main.version >= 22) player.setAbsorptionAmount(0);
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
    }

    public static void removeEntry(Player player) {
        for (Map.Entry<Team, Set<UUID>> entry : teams.entrySet()) {
            Team team = entry.getKey();
            Set<UUID> playerUUIDs = entry.getValue();
            if (playerUUIDs.contains(player.getUniqueId())) {
                if (team.getName().equals("1")) {
                    TeamFight.team1.removeEntry(player.getName());
                } else if (team.getName().equals("2")) {
                    TeamFight.team2.removeEntry(player.getName());
                }
                break;
            }
        }

    }

    public static boolean playerIsInTeam(UUID player) {
        return teams.values().stream().anyMatch(list -> list.contains(player));
    }

    public static void addEnchantment(Button button) {
        ItemMeta meta = button.getIcon().getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            button.getIcon().setItemMeta(meta);
        }
    }

    public static void removeEnchantment(Button button) {
        button.getIcon().removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
    }

    public static void saveLodestoneLocation(Location location, String path) {
        getKitConfig().set(path + "world", Objects.requireNonNull(location.getWorld()).getName());
        getKitConfig().set(path + "x", location.getX());
        getKitConfig().set(path + "y", location.getY());
        getKitConfig().set(path + "z", location.getZ());
    }

    public static Location lodestoneLocation(String path) {
        World world = Bukkit.getWorld(Objects.requireNonNull(getKitConfig().get(path + "world")).toString());
        double x = getKitConfig().getDouble(path + "x");
        double y = getKitConfig().getDouble(path + "y");
        double z = getKitConfig().getDouble(path + "z");
        return new Location(world, x, y, z);
    }

    private static final java.util.regex.Pattern VALID_KEY = java.util.regex.Pattern.compile("[a-z0-9/._-]+");

    @Nullable
    public static NamespacedKey fromString(@NotNull String string, @Nullable Plugin defaultNamespace) {
        Preconditions.checkArgument(!string.isEmpty(), "Input string must not be empty or null");
        String[] components = string.split(":", 3);
        if (components.length > 2) {
            return null;
        } else {
            String key = components.length == 2 ? components[1] : "";
            if (components.length == 1) {
                String value = components[0];
                if (!value.isEmpty() && VALID_KEY.matcher(value).matches()) {
                    return defaultNamespace != null ? new NamespacedKey(defaultNamespace, value) : NamespacedKey.minecraft(value);
                } else return null;
            } else if (components.length == 2 && !VALID_KEY.matcher(key).matches()) {
                return null;
            } else {
                String namespace = components[0];
                if (namespace.isEmpty()) {
                    return defaultNamespace != null ? new NamespacedKey(defaultNamespace, key) : NamespacedKey.minecraft(key);
                } else {
                    return !VALID_KEY.matcher(namespace).matches() ? null : new NamespacedKey(namespace, key);
                }
            }
        }
    }

    @Nullable
    public static NamespacedKey fromString(@NotNull String key) {
        return fromString(key, null);
    }
}
