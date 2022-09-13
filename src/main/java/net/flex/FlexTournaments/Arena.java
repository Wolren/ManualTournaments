package net.flex.FlexTournaments;

import net.flex.FlexTournaments.api.Command;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Arena extends Command {

    FileConfiguration config = Main.getPlugin().getConfig();
    private final FileConfiguration ArenasConfig = Main.getPlugin().ArenaConfig;

    public Arena() {
        super("ft_arena", "", "", "", "");
    }

    public boolean onExecute(CommandSender sender, String[] args) {
        try {
            ArenasConfig.load(Main.getPlugin().ArenaConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("sender-not-a-player"))));
        } else {
            Player player = ((Player) sender).getPlayer();
            assert player != null;
            if (args.length == 0) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("wrong-arguments"))));
            } else if (args.length == 1) {
                if (args[0].equals("list")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("arena-list"))) + ChatColor.translateAlternateColorCodes('&', Main.getPlugin().arenaNames.toString()));
                }
            } else if (args.length == 2) {
                String arenaName = args[1];
                String path = "Arenas." + arenaName + ".";
                if (args[0].equals("create")) {
                    Main.getPlugin().getArenaConfig().set("Arenas", arenaName);
                    Main.getPlugin().arenaNames.add(arenaName);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("arena-create"))));
                } else if (args[0].equals("pos1") & Main.getPlugin().arenaNames.contains(args[1])) {
                    String pathing = path + "pos1.";
                    getLocation(pathing, player, ArenasConfig);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("arena-pos1"))));
                } else if (args[0].equals("pos2") & Main.getPlugin().arenaNames.contains(args[1])) {
                    String pathing = path + "pos2.";
                    getLocation(pathing, player, ArenasConfig);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("arena-pos2"))));
                } else if ((args[0].equals("spec") || args[0].equals("spectator")) & Main.getPlugin().arenaNames.contains(args[1])) {
                    String pathing = path + "spectator.";
                    getLocation(pathing, player, ArenasConfig);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("arena-spectator"))));
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("wrong-arguments"))));
            }
            try {
                Main.getPlugin().getArenaConfig().save(Main.getPlugin().ArenaConfigFile);
                checkArena(player, args[1]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }

    public static void getLocation(String pathing, Player player, FileConfiguration cfg) {
        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        float yaw =  player.getLocation().getYaw();
        float pitch =  player.getLocation().getPitch();
        String world = Objects.requireNonNull(player.getLocation().getWorld()).getName();

        cfg.set(pathing + "x", x);
        cfg.set(pathing + "y", y);
        cfg.set(pathing + "z", z);
        cfg.set(pathing + "yaw", yaw);
        cfg.set(pathing + "pitch", pitch);
        cfg.set(pathing + "world", world);
    }

    public static Location pathing(String path, FileConfiguration cfg) {
        World world = Bukkit.getWorld(Objects.requireNonNull(cfg.get(path + "world")).toString());
        double x = cfg.getDouble(path + "x");
        double y = cfg.getDouble(path + "y");
        double z = cfg.getDouble(path + "z");
        float yaw = (float) cfg.getDouble(path + "yaw");
        float pitch = (float) cfg.getDouble(path + "pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private void checkArena(Player player, String arenaName) {
        if (ArenasConfig.isSet("Arenas." + arenaName + ".pos1") & ArenasConfig.isSet("Arenas." + arenaName + ".pos2") & ArenasConfig.isSet("Arenas." + arenaName + ".spectator")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("arena-created"))));
        }
    }
}


