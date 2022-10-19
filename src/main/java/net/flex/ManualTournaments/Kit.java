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
    private static final FileConfiguration config = Main.getPlugin().getConfig();
    private final FileConfiguration KitsConfig = Main.getPlugin().getKitsConfig();

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
        config.load(Main.getPlugin().customConfigFile);
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.conf("sender-not-a-player"));
        } else {
            Player p = ((OfflinePlayer) sender).getPlayer();
            assert p != null;
            KitsConfig.load(Main.getPlugin().KitsConfigfile);
            if (args.length == 0) {
                return false;
            } else if (args.length == 1) {
                String a = args[0];
                if (a.equals("list")) {
                    p.sendMessage(Main.conf("kit-list") + Main.getPlugin().kitNames.toString());
                } else if (a.equals("unbreakable")) {
                    if (Main.version > 4) {
                        ItemStack[] inv = p.getInventory().getContents();
                        for (ItemStack im : inv) {
                            if (im != null) {
                                if (im.getType().getMaxDurability() != 0) {
                                    ItemMeta unb = im.getItemMeta();
                                    assert unb != null;
                                    if (Main.version > 10) {
                                        unb.setUnbreakable(true);
                                    } else {
                                        send(p, "not-supported");
                                    }
                                    im.setItemMeta(unb);
                                }
                            }
                        }
                        p.updateInventory();
                        send(p, "kit-set-unbreakable");
                    }
                } else if (Main.getPlugin().kitNames.contains(a)) {
                    giveKit(p, a);
                    send(p, "kit-given");
                } else {
                    send(p, "kit-not-exists");
                }
            } else if (args.length == 2) {
                String a = args[0];
                String kitName = args[1];
                if (a.equals("create")) {
                    if (!Main.getPlugin().kitNames.contains(kitName)) {
                        createKit(p, kitName);
                        send(p, "kit-made");
                    } else p.sendMessage(Main.conf("kit-already-exists"));
                } else if (Main.getPlugin().kitNames.contains(kitName)) {
                    if (a.equals("remove")) {
                        KitsConfig.set("Kits." + kitName, null);
                        Main.getPlugin().kitNames.remove(kitName);
                        send(p, "kit-removed");
                    } else if (a.equals("give")) {
                        giveKit(p, kitName);
                        send(p, "kit-given");
                    }
                } else {
                    send(p, "kit-not-exists");
                    return true;
                }
            } else {
                return false;
            }
            try {
                Main.getPlugin().getKitsConfig().save(Main.getPlugin().KitsConfigfile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private void createKit(Player p, String kitName) {
        String path = "Kits." + kitName + ".";
        PlayerInventory inv = p.getInventory();
        Main.getPlugin().kitNames.add(kitName);
        for (int i = 0; i < 36; i++) {
            ItemStack is = inv.getItem(i);
            if (is == null) continue;
            String pathA = path + "items." + i;
            getType(pathA, is);
        }
        for (ItemStack armor : inv.getArmorContents()) {
            if (armor != null) {
                String pathB = path + "armor." + armor.getType().toString().toUpperCase();
                KitsConfig.set(pathB, inv.getArmorContents());
                getType(pathB, armor);
            }
        }
        if (Main.version > 8) {
            ItemStack offhand = inv.getItemInOffHand();
            String pathC = path + "offhand." + offhand.getType().toString().toUpperCase();
            if (!offhand.getType().equals(Material.AIR)) {
                KitsConfig.set(pathC, offhand);
                getType(pathC, offhand);
            }
        }
        try {
            Main.getPlugin().getKitsConfig().save(Main.getPlugin().KitsConfigfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getType(String path, ItemStack is) {
        KitsConfig.set(path + ".type", is.getType().toString().toUpperCase());
        KitsConfig.set(path + ".amount", is.getAmount());
        if (Main.version < 13) {
            KitsConfig.set(path + ".durability", is.getDurability());
        }

        if (is.hasItemMeta()) {
            if (Objects.requireNonNull(is.getItemMeta()).hasDisplayName())
                KitsConfig.set(path + ".name", is.getItemMeta().getDisplayName());

            if (is.getItemMeta().hasEnchants()) {
                Map<Enchantment, Integer> enchants = is.getEnchantments();
                Collection<String> enchantList = new ArrayList<>();
                for (Enchantment e : is.getEnchantments().keySet()) {
                    int level = enchants.get(e);
                    enchantList.add(e.getName().toUpperCase() + ":" + level);
                }
                KitsConfig.set(path + ".enchants", enchantList);
            }
        }
    }

    void giveKit(Player p, String kitName) {
        String path = "Kits." + kitName + ".";
        p.getInventory().clear();
        p.setHealth(20.0D);
        p.setFoodLevel(20);
        p.setFireTicks(0);
        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
        ConfigurationSection csA = KitsConfig.getConfigurationSection("Kits." + kitName + "." + "items");
        ConfigurationSection csB = KitsConfig.getConfigurationSection("Kits." + kitName + "." + "armor");
        ConfigurationSection csC = KitsConfig.getConfigurationSection("Kits." + kitName + "." + "offhand");
        setPlayerContents(p, path, csA, csB, csC);
        p.updateInventory();
    }

    private void setPlayerContents(Player player, String path, ConfigurationSection csA, ConfigurationSection csB, ConfigurationSection csC) {
        if (csA != null) {
            for (String str : Objects.requireNonNull(csA).getKeys(false)) {
                int slot = Integer.parseInt(str);
                String pA = path + "items." + slot + ".";
                String typeA = KitsConfig.getString(pA + "type");
                String nameA = KitsConfig.getString(pA + "name");
                short durability = 0;
                if (Main.version < 13) {
                    durability = (short) KitsConfig.getInt(pA + "durability");
                }
                List<String> enchants = KitsConfig.getStringList(pA + "enchants");
                int amount = KitsConfig.getInt(pA + "amount");
                assert typeA != null;
                ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(typeA)), amount);
                ItemMeta im = is.getItemMeta();
                if (Main.version < 13) {
                    is.setDurability(durability);
                }
                if (im == null) continue;
                if (nameA != null) im.setDisplayName(ChatColor.translateAlternateColorCodes('&', nameA));
                for (String s1 : enchants) {
                    String[] indiEnchants = s1.split(":");
                    im.addEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                }
                is.setItemMeta(im);
                player.getInventory().setItem(slot, is);
            }
        }
        if (csB != null) {
            for (String str : Objects.requireNonNull(csB).getKeys(false)) {
                String pB = path + "armor." + str + ".";
                String typeB = KitsConfig.getString(pB + "type");
                String nameB = KitsConfig.getString(pB + "name");
                short durability = 0;
                if (Main.version < 13) {
                    durability = (short) KitsConfig.getInt(pB + "durability");
                }
                List<String> enchants = KitsConfig.getStringList(pB + "enchants");
                int amount = KitsConfig.getInt(pB + "amount");
                assert typeB != null;
                ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(typeB)), amount);
                ItemMeta im = is.getItemMeta();
                if (Main.version < 13) {
                    is.setDurability(durability);
                }
                if (im == null) continue;
                if (nameB != null) im.setDisplayName(ChatColor.translateAlternateColorCodes('&', nameB));
                for (String s1 : enchants) {
                    String[] indiEnchants = s1.split(":");
                    im.addEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                }
                is.setItemMeta(im);
                if (pB.contains("HELMET")) {
                    player.getInventory().setHelmet(is);
                } else if (pB.contains("CHESTPLATE")) {
                    player.getInventory().setChestplate(is);
                } else if (pB.contains("LEGGINGS")) {
                    player.getInventory().setLeggings(is);
                } else if (pB.contains("BOOTS")) {
                    player.getInventory().setBoots(is);
                }
            }
        }
        if (Main.version > 8 && csC != null) {
            List<String> strs = new ArrayList<>(Objects.requireNonNull(csC).getKeys(false));
            String pC = path + "offhand." + strs.get(0) + ".";
            String typeC = KitsConfig.getString(pC + "type");
            String nameC = KitsConfig.getString(pC + "name");
            short durability = 0;
            if (Main.version < 13) {
                durability = (short) KitsConfig.getInt(pC + "durability");
            }
            List<String> enchants = KitsConfig.getStringList(pC + "enchants");
            int amount = KitsConfig.getInt(pC + "amount");
            assert typeC != null;
            ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(typeC)), amount);
            ItemMeta im = is.getItemMeta();
            if (Main.version < 13) {
                is.setDurability(durability);
            }
            if (im != null) {
                if (nameC != null) im.setDisplayName(ChatColor.translateAlternateColorCodes('&', nameC));
                for (String s1 : enchants) {
                    String[] indiEnchants = s1.split(":");
                    im.addEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                }
            }
            is.setItemMeta(im);
            player.getInventory().setItemInOffHand(is);
        }
    }

    private static void send(Player p, String s) {
        p.sendMessage(Main.conf(s));
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(Arrays.asList("create", "give", "list", "remove", "unbreakable"));
        } else if (args.length == 2) {
            List<String> arr = new ArrayList<>();
            if (args[0].equals("create")) {
                arr.add("[name]");
            } else if (args[0].equals("remove") || args[0].equals("give")) {
                arr.addAll(Main.getPlugin().kitNames);
            }

            return arr;
        }

        return Collections.emptyList();
    }
}
