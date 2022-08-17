package net.flex.FlexTournaments;

import net.flex.FlexTournaments.api.Command;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Kit extends Command {

    public Kit() {
        super("FlexTournaments", "", "", "", "kit");
    }

    public boolean onExecute(CommandSender sender, String[] args) {
        FileConfiguration aconfig = Main.getPlugin().getaConfig();
        File file = new File(Main.getPlugin().getDataFolder(), "kits.yml");
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.getPlugin().getConfig().getString("sender-not-a-player"));
        } else {
            Player player = ((Player) sender).getPlayer();
            assert player != null;
            PlayerInventory inv = player.getInventory();
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED +  Main.getPlugin().getConfig().getString("kit-no-arguments"));
            } else if (args.length == 1) {
                if (!Main.getPlugin().kitNames.contains(args[0]) & !args[0].equals("list")) {
                    sender.sendMessage(ChatColor.RED + Main.getPlugin().getConfig().getString("kit-not-exists"));
                } else if (args[0].equals("list")) {
                    sender.sendMessage("Kits: " + Main.getPlugin().kitNames.toString());
                } else if (Main.getPlugin().kitNames.contains(args[0]) & !args[0].equals("list")) {
                    String kitName = args[0];
                    player.getInventory().clear();
                    String path = "Kits." + kitName + ".";
                    ConfigurationSection s = aconfig.getConfigurationSection("Kits." + kitName + "." + "items");
                    for (String str : Objects.requireNonNull(s).getKeys(false)) {
                        int slot = Integer.parseInt(str);
                        if (0 < slot && slot < 36)
                            continue;

                        String string = path + "items." + slot + ".";
                        String type = aconfig.getString(string + "type");
                        String name = aconfig.getString(string + "name");
                        List<String> enchants = aconfig.getStringList(string + "enchants");
                        int amount = aconfig.getInt(string + "amount");

                        ItemStack is = new ItemStack(Material.matchMaterial(type), amount);
                        ItemMeta im = is.getItemMeta();

                        if (im == null)
                            continue;

                        if (name != null)
                            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                        if (enchants != null) {
                            for (String s1 : enchants) {
                                String[] indiEnchants = s1.split(":");
                                im.addEnchant(Enchantment.getByName(indiEnchants[0]), Integer.parseInt(indiEnchants[1]), true);
                            }
                        }

                        is.setItemMeta(im);
                        player.getInventory().setItem(slot, is);
                    }

                    String helmet = aconfig.getString(path + "armor.helmet");
                    String chestplate = aconfig.getString(path + "armor.chestplate");
                    String leggings = aconfig.getString(path + "armor.leggings");
                    String boots = aconfig.getString(path + "armor.boots");

                    player.getInventory().setHelmet(new ItemStack(helmet != null ? Material.matchMaterial(helmet) : Material.AIR));
                    player.getInventory().setChestplate(new ItemStack(chestplate != null ? Material.matchMaterial(chestplate) : Material.AIR));
                    player.getInventory().setLeggings(new ItemStack(leggings != null ? Material.matchMaterial(leggings) : Material.AIR));
                    player.getInventory().setBoots(new ItemStack(boots != null ? Material.matchMaterial(boots) : Material.AIR));

                    player.updateInventory();
                }
            } else if (args.length == 2 & args[0].equals("create")) {
                    String kitName = args[1];
                    String path = "Kits." + kitName + ".";
                    if (Main.getPlugin().kitNames.contains(args[1])) {
                        sender.sendMessage(ChatColor.RED + Main.getPlugin().getConfig().getString("kit-already-exists"));
                        return false;
                    } else {
                        Main.getPlugin().kitNames.add(args[1]);
                        for (int i = 0; i < 36; i++) {
                            ItemStack is = inv.getItem(i);
                            if (is == null || is.getType() == Material.AIR)
                                continue;

                            String slot = path + "items." + i;
                            aconfig.set(slot + ".type", is.getType().toString().toUpperCase());
                            aconfig.set(slot + ".amount", is.getAmount());

                            if (!is.hasItemMeta())
                                continue;

                            if (Objects.requireNonNull(is.getItemMeta()).hasDisplayName())
                                aconfig.set(slot + ".name", is.getItemMeta().getDisplayName());

                            if (is.getItemMeta().hasEnchants()) {
                                Map<Enchantment, Integer> enchants = is.getEnchantments();
                                List<String> enchantList = new ArrayList<>();
                                for (Enchantment e : is.getEnchantments().keySet()) {
                                    int level = enchants.get(e);
                                    enchantList.add(e.getName().toUpperCase()+ ":" + level);
                                }
                                aconfig.set(slot + ".enchants", enchantList);
                            }
                        }

                        aconfig.set(path + "armor.helmet", inv.getHelmet() != null ? inv
                                .getHelmet().getType().toString().toUpperCase() : "air");

                        aconfig.set(path + "armor.chestplate", inv.getChestplate() != null ? inv
                                .getChestplate().getType().toString().toUpperCase() : "air");

                        aconfig.set(path + "armor.leggings", inv.getLeggings() != null ? inv
                                .getLeggings().getType().toString().toUpperCase() : "air");

                        aconfig.set(path + "armor.boots", inv.getBoots() != null ? inv
                                .getBoots().getType().toString().toUpperCase() : "air");

                        try {
                            Main.getPlugin().getaConfig().save(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            } else if (args.length == 2 & args[0].equals("delete")) {
                if (Main.getPlugin().kitNames.contains(args[1])) {
                    String kitName = args[1];
                    Main.getPlugin().getaConfig().set("Kits." + kitName, null);
                    try {
                        Main.getPlugin().getaConfig().save(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Main.getPlugin().kitNames.remove(kitName);
                }
            }
        }


        return false;
    }
}