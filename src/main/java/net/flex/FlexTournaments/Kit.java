package net.flex.FlexTournaments;

import net.flex.FlexTournaments.api.Command;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Kit extends Command {

    FileConfiguration config = Main.getPlugin().getConfig();
    public FileConfiguration KitsConfig = Main.getPlugin().KitsConfig;
    public static Kit getInstance(){
        try {
            return Kit.class.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Kit() {
        super("ft_kit", "", "", "", "");
    }

    public boolean onExecute(CommandSender sender, String[] args) {
        try {
            KitsConfig.load(Main.getPlugin().KitsConfigfile);
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
                if (Main.getPlugin().kitNames != null & !Main.getPlugin().kitNames.contains(args[0]) & !args[0].equals("list")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("kit-not-exists"))));
                } else if (args[0].equals("list")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("kit-list"))) + ChatColor.translateAlternateColorCodes('&', Main.getPlugin().kitNames.toString()));
                } else if (Main.getPlugin().kitNames.contains(args[0])) {
                    String kitName = args[0];
                    giveKit(player, kitName);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("kit-given"))));
                }
            } else if (args.length == 2 & args[0].equals("create")) {
                String kitName = args[1];
                createKit(player, kitName);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("kit-made"))));
            } else if (args.length == 2 & (args[0].equals("delete") || args[0].equals("remove"))) {
                if (Main.getPlugin().kitNames.contains(args[1])) {
                    String kitName = args[1];
                    KitsConfig.set("Kits." + kitName, null);
                    try {
                       Main.getPlugin().getKitsConfig().save(Main.getPlugin().KitsConfigfile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Main.getPlugin().kitNames.remove(kitName);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("kit-removed"))));
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("wrong-arguments"))));
            }
        }


        return false;
    }

    public void getType(String pathing, ItemStack is) {
        KitsConfig.set(pathing + ".type", is.getType().toString().toUpperCase());
        KitsConfig.set(pathing + ".amount", is.getAmount());

        if (is.hasItemMeta()) {
            if (Objects.requireNonNull(is.getItemMeta()).hasDisplayName())
                KitsConfig.set(pathing + ".name", is.getItemMeta().getDisplayName());

            if (is.getItemMeta().hasEnchants()) {
                Map<Enchantment, Integer> enchants = is.getEnchantments();
                List<String> enchantList = new ArrayList<>();
                for (Enchantment e : is.getEnchantments().keySet()) {
                    int level = enchants.get(e);
                    enchantList.add(e.getName().toUpperCase() + ":" + level);
                }
                KitsConfig.set(pathing + ".enchants", enchantList);
            }
        }
    }

    public void createKit(Player player, String kitName) {
        String path = "Kits." + kitName + ".";
        PlayerInventory inv = player.getInventory();
        if (Main.getPlugin().kitNames.contains(kitName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Main.getPlugin().getConfig().getString("kit-already-exists"))));
        } else {
            Main.getPlugin().kitNames.add(kitName);
            for (int i = 0; i < 36; i++) {
                ItemStack is = inv.getItem(i);
                if (is == null || is.getType() == Material.AIR)
                    continue;

                String pathing = path + "items." + i;
                getType(pathing, is);
                }
            }

            for (ItemStack armor : inv.getArmorContents()) {
                if (armor != null) {
                    String pathing = path + "armor." + armor.getType().toString().toUpperCase();
                    KitsConfig.set(pathing, inv.getArmorContents());
                    getType(pathing, armor);
                    }
                }
            ItemStack offhand = inv.getItemInOffHand();
        String oathing = path + "offhand." + offhand.getType().toString().toUpperCase();
        KitsConfig.set(oathing, offhand);
        getType(oathing, offhand);

        try {
            Main.getPlugin().getKitsConfig().save(Main.getPlugin().KitsConfigfile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    public void setIntType(Player player, ConfigurationSection s, String path) {
        if (s != null) {
            for (String str : Objects.requireNonNull(s).getKeys(false)) {
                int slot = Integer.parseInt(str);
                if (0 < slot && slot < 36)
                    continue;

                String string = path + "items." + slot + ".";
                String type = KitsConfig.getString(string + "type");
                String name = KitsConfig.getString(string + "name");
                List<String> enchants = KitsConfig.getStringList(string + "enchants");
                int amount = KitsConfig.getInt(string + "amount");

                assert type != null;
                ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(type)), amount);
                ItemMeta im = is.getItemMeta();

                if (im == null)
                    continue;

                if (name != null)
                    im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                for (String s1 : enchants) {
                    String[] indiEnchants = s1.split(":");
                    im.addEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                }

                is.setItemMeta(im);
                player.getInventory().setItem(slot, is);
            }
        }
    }

    public void setOffHandType(Player player, ConfigurationSection s, String path) {
        if (s != null) {
            for (String str : Objects.requireNonNull(s).getKeys(false)) {
                if (!str.equals("AIR")) {
                    String string = path + "offhand." + str + ".";
                    String type = KitsConfig.getString(string + "type");
                    String name = KitsConfig.getString(string + "name");
                    List<String> enchants = KitsConfig.getStringList(string + "enchants");
                    int amount = KitsConfig.getInt(string + "amount");

                    assert type != null;
                    ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(type)), amount);
                    ItemMeta im = is.getItemMeta();

                    if (im == null)
                        continue;

                    if (name != null)
                        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                    for (String s1 : enchants) {
                        String[] indiEnchants = s1.split(":");
                        im.addEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                    }

                    is.setItemMeta(im);
                    player.getInventory().setItemInOffHand(is);
                }
            }
        }
    }

    public void setArmorType(Player player, ConfigurationSection s, String path) {
        if (s != null) {
            for (String str : Objects.requireNonNull(s).getKeys(false)) {
                String string = path + "armor." + str + ".";
                String type = KitsConfig.getString(string + "type");
                String name = KitsConfig.getString(string + "name");
                List<String> enchants = KitsConfig.getStringList(string + "enchants");
                int amount = KitsConfig.getInt(string + "amount");

                assert type != null;
                ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(type)), amount);
                ItemMeta im = is.getItemMeta();

                if (im == null)
                    continue;

                if (name != null)
                    im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                for (String s1 : enchants) {
                    String[] indiEnchants = s1.split(":");
                    im.addEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                }

                is.setItemMeta(im);
                if (string.contains("HELMET")) {
                    player.getInventory().setHelmet(is);
                } else if (string.contains("CHESTPLATE")) {
                    player.getInventory().setChestplate(is);
                } else if (string.contains("LEGGINGS")) {
                    player.getInventory().setLeggings(is);
                } else if (string.contains("BOOTS")) {
                    player.getInventory().setBoots(is);
                }
            }
        }
    }

    public void giveKit(Player player, String kitName) {
        String path = "Kits." + kitName + ".";
        player.getInventory().clear();
        player.setHealth(20.0);
        for(PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setFoodLevel(20);
        player.setFireTicks(0);
        ConfigurationSection a = KitsConfig.getConfigurationSection("Kits." + kitName + "." + "items");
        setIntType(player, a, path);
        ConfigurationSection b = KitsConfig.getConfigurationSection("Kits." + kitName + "." + "armor");
        setArmorType(player, b, path);
        ConfigurationSection c = KitsConfig.getConfigurationSection("Kits." + kitName + "." + "offhand");
        setOffHandType(player, c, path);
        player.updateInventory();
    }
}