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

public class Fight extends Command {
    public Fight() {
        super("FlexTournaments", "", "", "", "ft_fight");
    }

    FileConfiguration config = Main.getPlugin().getConfig();
    public FileConfiguration KitsConfig = Main.getPlugin().KitsConfig;
    public FileConfiguration ArenasConfig = Main.getPlugin().ArenaConfig;
    public boolean onExecute(CommandSender sender, String[] args) {
        try {
            KitsConfig.load(Main.getPlugin().KitsConfigfile);
            ArenasConfig.load(Main.getPlugin().ArenaConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("sender-not-a-player"))));
        } else {
            Player player = ((Player) sender).getPlayer();
            assert player != null;
            if (args.length == 0 || args.length == 1) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("wrong-arguments"))));
            } else if (args.length <= 20) {
                if (args.length % 2 == 0) {
                    List<String> distinctElements = new java.util.ArrayList<>(List.of());
                    for (String arg : args) {
                        if (!arg.equals("null")) {
                            Player fighter = Bukkit.getPlayer(arg);
                            if (fighter != null) {
                                distinctElements.add(Objects.requireNonNull(fighter).toString());
                            }
                        }
                    }
                    if (distinctElements.size() == distinctElements.stream().distinct().toList().size()) {
                        for (int i = 0; i < args.length; i++) {
                            if (!args[i].equals("null")) {
                                Player fighter = Bukkit.getPlayer(args[i]);
                                if (fighter != null) {
                                    String patha = "Arenas." + config.getString("current-arena") + "." + "pos1" + ".";
                                    String pathb = "Arenas." + config.getString("current-arena") + "." + "pos2" + ".";
                                    Kit(fighter);
                                    if (i < (args.length / 2)) {
                                        fighter.teleport(pathing(patha));
                                    } else if (i >= (args.length / 2)) {
                                        fighter.teleport(pathing(pathb));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void Kit(Player x) {
        Kit.getInstance().giveKit(x, Main.getPlugin().getConfig().getString("current-kit"));
    }

    public Location pathing(String path) {
        World world = Bukkit.getWorld(Objects.requireNonNull(ArenasConfig.get(path + "world")).toString());
        double x = ArenasConfig.getDouble(path + "x");
        double y = ArenasConfig.getDouble(path + "y");
        double z = ArenasConfig.getDouble(path + "z");
        float yaw = (float) ArenasConfig.getDouble(path + "yaw");
        float pitch = (float) ArenasConfig.getDouble(path + "pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }
}
