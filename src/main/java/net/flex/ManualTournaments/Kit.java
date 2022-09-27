package net.flex.ManualTournaments;

import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings("deprecation")
public class Kit implements TabCompleter, CommandExecutor {

    private final FileConfiguration KitsConfig = Main.getPlugin().KitsConfig;

    static Kit getInstance() {
        try {
            return Kit.class.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        KitsConfig.load(Main.getPlugin().KitsConfigfile);
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.conf("sender-not-a-player"));
        } else {
            Player player = ((OfflinePlayer) sender).getPlayer();
            assert player != null;
            if (args.length == 0) {
                Main.getPlugin();
                player.sendMessage(Main.conf("wrong-arguments"));
            } else if (args.length == 1) {
                if (args[0].equals("list")) {
                    player.sendMessage(Main.conf("kit-list") + Main.getPlugin().kitNames.toString());
                } else if (args[0].equals("unbreakable")) {
                    ItemStack[] inv = player.getInventory().getContents();
                    for (ItemStack im : inv) {
                        if (im != null) {
                            if (im.getType().getMaxDurability() != 0) {
                                ItemMeta unb = im.getItemMeta();
                                assert unb != null;
                                unb.setUnbreakable(true);
                                im.setItemMeta(unb);
                            }
                        }
                    }
                } else if (Main.getPlugin().kitNames.contains(args[0])) {
                    String kitName = args[0];
                    giveKit(player, kitName);
                    player.sendMessage(Main.conf("kit-given"));
                } else {
                    player.sendMessage(Main.conf("kit-not-exists"));
                }
            } else if (args.length == 2) {
                String kitName = args[1];
                if (args[0].equals("create")) {
                    if (!Main.getPlugin().kitNames.contains(args[1])) {
                        createKit(player, kitName);
                        player.sendMessage(Main.conf("kit-made"));
                    } else player.sendMessage(Main.conf("kit-already-exists"));
                } else if (Main.getPlugin().kitNames.contains(args[1])) {
                    if (args[0].equals("remove")) {
                        KitsConfig.set("Kits." + kitName, null);
                        Main.getPlugin().kitNames.remove(kitName);
                        player.sendMessage(Main.conf("kit-removed"));
                    } else if (args[0].equals("give")) {
                        giveKit(player, kitName);
                        player.sendMessage(Main.conf("kit-given"));
                    }
                } else {
                    player.sendMessage(Main.conf("kit-not-exists"));
                }
            } else {
                player.sendMessage(Main.conf("wrong-arguments"));
            }
            try {
                Main.getPlugin().getKitsConfig().save(Main.getPlugin().KitsConfigfile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private void getType(String pathing, ItemStack is) {
        KitsConfig.set(pathing + ".type", is.getType().toString().toUpperCase());
        KitsConfig.set(pathing + ".amount", is.getAmount());

        if (is.hasItemMeta()) {
            if (Objects.requireNonNull(is.getItemMeta()).hasDisplayName())
                KitsConfig.set(pathing + ".name", is.getItemMeta().getDisplayName());

            if (is.getItemMeta().hasEnchants()) {
                Map<Enchantment, Integer> enchants = is.getEnchantments();
                Collection<String> enchantList = new ArrayList<>();
                for (Enchantment e : is.getEnchantments().keySet()) {
                    int level = enchants.get(e);
                    enchantList.add(e.getName().toUpperCase() + ":" + level);
                }
                KitsConfig.set(pathing + ".enchants", enchantList);
            }
        }
    }

    private void createKit(Player player, String kitName) {
        String path = "Kits." + kitName + ".";
        PlayerInventory inv = player.getInventory();
        if (Main.getPlugin().kitNames.contains(kitName)) {
            player.sendMessage(Main.conf("kit-already-exists"));
        } else {
            Main.getPlugin().kitNames.add(kitName);
            for (int i = 0; i < 36; i++) {
                ItemStack is = inv.getItem(i);
                if (is == null || is.getType() == Material.AIR) continue;

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

    private void setIntType(Player player, ConfigurationSection s, String path) {
        if (s != null) {
            for (String str : Objects.requireNonNull(s).getKeys(false)) {
                int slot = Integer.parseInt(str);
                if (0 < slot && slot < 36) continue;

                String string = path + "items." + slot + ".";
                String type = KitsConfig.getString(string + "type");
                String name = KitsConfig.getString(string + "name");
                List<String> enchants = KitsConfig.getStringList(string + "enchants");
                int amount = KitsConfig.getInt(string + "amount");

                assert type != null;
                ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(type)), amount);
                ItemMeta im = is.getItemMeta();

                if (im == null) continue;

                if (name != null) im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

                for (String s1 : enchants) {
                    String[] indiEnchants = s1.split(":");
                    im.addEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                }

                is.setItemMeta(im);
                player.getInventory().setItem(slot, is);
            }
        }
    }

    private void setOffHandType(Player player, ConfigurationSection s, String path) {
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

                    if (im == null) continue;

                    if (name != null) im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

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

    private void setArmorType(Player player, ConfigurationSection s, String path) {
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

                if (im == null) continue;

                if (name != null) im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

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

    void giveKit(Player player, String kitName) {
        String path = "Kits." + kitName + ".";
        player.getInventory().clear();
        player.setHealth(20.0);
        for (PotionEffect effect : player.getActivePotionEffects()) {
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

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(Arrays.asList("create", "give", "list", "remove", "unbreakable"));
        } else if (args.length == 2) {
            List<String> b = new ArrayList<>();
            if (args[0].equals("create")) {
                b.add("[name]");
            } else if (args[0].equals("remove") || args[0].equals("give")) {
                b.addAll(Main.getPlugin().kitNames);
            }

            return b;
        }

        return Arrays.asList();
    }
}
