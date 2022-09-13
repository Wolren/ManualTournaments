package net.flex.FlexTournaments.api;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public final class ChatUtil {
    public ChatUtil() {
    }

    public static Location locFromString(String str) {
        String[] str2loc = str.split(":");
        Location loc = new Location((World)Bukkit.getWorlds().get(0), 0.0, 0.0, 0.0, 0.0F, 0.0F);
        loc.setX(Double.parseDouble(str2loc[0]));
        loc.setY(Double.parseDouble(str2loc[1]));
        loc.setZ(Double.parseDouble(str2loc[2]));
        loc.setYaw(Float.parseFloat(str2loc[3]));
        loc.setPitch(Float.parseFloat(str2loc[4]));
        return loc;
    }

    public static ItemStack getItemStackFromString(String itemstack) {
        String[] splits = itemstack.split("@");
        String type = splits[0];
        String data = splits.length == 2 ? splits[1] : null;
        return data == null ? new ItemStack(Material.getMaterial(type), 1) : new ItemStack(Material.getMaterial(type), 1, (short)Integer.parseInt(data));
    }

    public static String locToString(double x, double y, double z) {
        return String.valueOf(x) + ":" + y + ":" + z + ":0.0:0.0";
    }

    public static String locToString(Location loc) {
        return String.valueOf(loc.getX()) + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
    }

    public static String fixColor(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

    public static Collection<String> fixColor1(Collection<String> collection) {
        Collection<String> local = new ArrayList();

        for(String s : collection) {
            local.add(fixColor(s));
        }

        return local;
    }

    public static List<String> fixColor(List<String> strings) {
        List<String> colors = new ArrayList();

        for(String s : strings) {
            colors.add(fixColor(s));
        }

        return colors;
    }

    public static Collection<String> fixColor(Collection<String> collection) {
        Collection<String> local = new ArrayList();

        for(String s : collection) {
            local.add(fixColor(s));
        }

        return local;
    }

    public static int floor(double value) {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }

    public static double round(double value, int decimals) {
        double p = Math.pow(10.0, (double)decimals);
        return (double)Math.round(value * p) / p;
    }

    public static String[] fixColor(String[] array) {
        for(int i = 0; i < array.length; ++i) {
            array[i] = fixColor(array[i]);
        }

        return array;
    }

    public static boolean sendMessage(CommandSender sender, String message, String permission) {
        if (sender instanceof ConsoleCommandSender) {
            sendMessage(sender, message);
        }

        return permission != null && permission != "" && sender.hasPermission(permission) && sendMessage(sender, message);
    }

    public static boolean sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            if (message != null || null != "") {
                sender.sendMessage(fixColor(message));
            }
        } else {
            sender.sendMessage(ChatColor.stripColor(fixColor(message)));
        }

        return true;
    }

    public static void removeItems(Player p, ItemStack... items) {
        PlayerInventory playerInventory = p.getInventory();
        HashMap<Integer, ItemStack> notStored = playerInventory.removeItem(items);

        for(Entry var5 : notStored.entrySet()) {
        }

    }

    public static boolean sendMessage(Collection<? extends CommandSender> collection, String message) {
        for(CommandSender cs : collection) {
            sendMessage(cs, message);
        }

        return true;
    }

    public static boolean sendMessage(Collection<? extends CommandSender> collection, String message, String permission) {
        for(CommandSender cs : collection) {
            sendMessage(cs, message, permission);
        }

        return true;
    }

    public static boolean containsIgnoreCase(String[] array, String element) {
        for(String s : array) {
            if (s.equalsIgnoreCase(element)) {
                return true;
            }
        }

        return false;
    }

    public static void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];

            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            in.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public static void giveItems(Player p, ItemStack... items) {
        PlayerInventory playerInventory = p.getInventory();
        HashMap<Integer, ItemStack> notStored = playerInventory.addItem(items);

        for(Entry<Integer, ItemStack> e : notStored.entrySet()) {
            p.getWorld().dropItemNaturally(p.getLocation(), (ItemStack)e.getValue());
        }

    }

    public static Player getDamager(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if (damager instanceof Player) {
            return (Player)damager;
        } else {
            if (damager instanceof Projectile p && p.getShooter() instanceof Player) {
                return (Player)p.getShooter();
            }

            return null;
        }
    }

    public static boolean isAlphaNumeric(String s) {
        return s.matches("^[a-zA-Z0-9_]*$");
    }

    public static boolean isFloat(String string) {
        return Pattern.matches("([0-9]*)\\.([0-9]*)", string);
    }

    public static boolean isInteger(String string) {
        return Pattern.matches("-?[0-9]+", string.subSequence(0, string.length()));
    }
}
