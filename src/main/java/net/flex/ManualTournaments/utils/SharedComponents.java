package net.flex.ManualTournaments.utils;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.FightCommand.Implementations.TeamFight;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.Main.getPlugin;

public final class SharedComponents {

    public static final FileConfiguration config = getPlugin().getConfig();
    public static Player player = null;
    public static final String currentArena = config.getString("current-arena");
    public static final String currentKit = config.getString("current-kit");

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
        getArenaConfig().save(getPlugin().ArenaConfigFile);
    }

    public static void collidableReflection(Player fighter) {
        try {
            Class<?> spigotEntityClass = Class.forName("org.bukkit.entity.Player$Spigot");
            Method setCollidesWithEntities = spigotEntityClass.getMethod("setCollidesWithEntities", boolean.class);
            setCollidesWithEntities.invoke(fighter.spigot(), false);
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

    public static String teamList(final Iterable<UUID> team, final Collection<String> teamString) {
        for (final UUID uuid : team) teamString.add(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName());
        return String.join(", ", teamString);
    }

    public static void clear(Player player) {
        player.getInventory().clear();
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setAbsorptionAmount(0);
        player.setSaturation(0);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
    }

    public static void removeEntries() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (TeamFight.team1.contains(player.getUniqueId())) TeamFight.team1Board.removeEntry(player.getDisplayName());
            else if (TeamFight.team2.contains(player.getUniqueId())) TeamFight.team2Board.removeEntry(player.getDisplayName());
        }
    }
}
