package net.flex.ManualTournaments.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.*;

public final class Shared {
    public static String message(String s) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getPlugin().getConfig().getString(s)));
    }

    public static void send(Player player, String s) {
        player.sendMessage(message(s));
    }

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
}
