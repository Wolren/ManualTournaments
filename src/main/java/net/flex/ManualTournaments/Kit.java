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
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("deprecation")
public class Kit implements TabCompleter, CommandExecutor {
    private final Main plugin = Main.getPlugin();
    private static final FileConfiguration config = Main.getPlugin().getConfig();
    private static final FileConfiguration KitsConfig = Main.getPlugin().getKitsConfig();
    private final List<String> kits = Main.getPlugin().kitNames;

    @SneakyThrows
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Optional<Player> playerOptional = Optional.ofNullable(((OfflinePlayer) sender).getPlayer());
        if (!playerOptional.isPresent() || !(sender instanceof Player)) {
            sender.sendMessage("sender-not-a-player");
            return false;
        }
        loadConfigs();
        Player player = playerOptional.get();
        if (args.length == 1) {
            switch (args[0].toUpperCase()) {
                case "LIST":
                    player.sendMessage(Main.conf("kit-list") + kits.toString());
                    break;
                case "UNBREAKABLE":
                    if (Main.version >= 17) {
                        for (ItemStack im : player.getInventory().getContents()) {
                            if (im != null && im.getType().getMaxDurability() != 0) {
                                ItemMeta unbreakable = im.getItemMeta();
                                if (unbreakable != null) unbreakable.setUnbreakable(true);
                                im.setItemMeta(unbreakable);
                            }
                        }
                        player.updateInventory();
                        send(player, "kit-set-unbreakable");
                    } else send(player, "not-supported");
                    break;
                default:
                    if (kits.contains(args[0])) {
                        setKit(player, args[0]);
                        send(player, "kit-given");
                    } else {
                        send(player, "kit-not-exists");
                        return false;
                    }
            }
        } else if (args.length == 2) {
            String kitName = args[1];
            boolean kitExists = kits.contains(kitName);
            switch (args[0].toUpperCase()) {
                case "CREATE":
                    if (!kitExists) {
                        getKit(player, kitName);
                        send(player, "kit-made");
                        KitsConfig.save(plugin.KitsConfigfile);
                    } else player.sendMessage(Main.conf("kit-already-exists"));
                    break;
                case "REMOVE":
                    if (kitExists) {
                        KitsConfig.set("Kits." + kitName, null);
                        kits.remove(kitName);
                        send(player, "kit-removed");
                    } else send(player, "kit-not-exists");
                    break;
                case "GIVE":
                    if (kitExists) {
                        setKit(player, kitName);
                        send(player, "kit-given");
                    } else send(player, "kit-not-exists");
                    break;
                default:
                    return false;
            }
        } else return false;
        KitsConfig.save(plugin.KitsConfigfile);
        return true;
    }

    @SneakyThrows
    private void getKit(Player player, String kitName) {
        String path = "Kits." + kitName + ".";
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack is = inventory.getItem(i);
            if (is == null) continue;
            String itemPath = path + "items." + i;
            getType(itemPath, is);
        }
        for (ItemStack armor : inventory.getArmorContents()) {
            if (armor != null) {
                String armorPath = path + "armor." + armor.getType().toString().toUpperCase();
                KitsConfig.set(armorPath, inventory.getArmorContents());
                getType(armorPath, armor);
            }
        }
        if (Main.version >= 15) {
            ItemStack offhand = inventory.getItemInOffHand();
            if (!offhand.getType().equals(Material.AIR)) {
                String offhandPath = path + "offhand." + offhand.getType().toString().toUpperCase();
                KitsConfig.set(offhandPath, offhand);
                getType(offhandPath, offhand);
            }
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            String effectPath = path + "effects." + effect.getType().getName().toUpperCase();
            KitsConfig.set(effectPath + ".amplifier", effect.getAmplifier());
            KitsConfig.set(effectPath + ".duration", effect.getDuration());
        }
        kits.add(kitName);
        KitsConfig.save(plugin.KitsConfigfile);
    }

    private void getType(String path, ItemStack is) {
        KitsConfig.set(path + ".type", is.getType().toString().toUpperCase());
        KitsConfig.set(path + ".amount", is.getAmount());
        if (Main.version <= 18) KitsConfig.set(path + ".durability", is.getDurability());
        if (is.getItemMeta() != null) {
            if (is.getItemMeta().hasDisplayName())
                KitsConfig.set(path + ".name", is.getItemMeta().getDisplayName());
            if (Main.version >= 17) {
                if (is.getItemMeta().isUnbreakable()) {
                    KitsConfig.set(path + ".unbreakable", is.getItemMeta().isUnbreakable());
                }
            }
            if (is.getType().equals(Material.ENCHANTED_BOOK)) {
                EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) is.getItemMeta();
                Map<Enchantment, Integer> enchants = storageMeta.getStoredEnchants();
                Collection<String> enchantList = new ArrayList<>();
                for (Enchantment e : storageMeta.getStoredEnchants().keySet()) {
                    int level = enchants.get(e);
                    enchantList.add(e.getName().toUpperCase() + ":" + level);
                }
                KitsConfig.set(path + ".enchants", enchantList);
                if (storageMeta.hasLore()) KitsConfig.set(path + ".lore", storageMeta.getLore());
                if (storageMeta.hasDisplayName()) KitsConfig.set(path + ".name", storageMeta.getDisplayName());
                else KitsConfig.set(path + ".name", "&e" + "Enchanted Book");
            } else if (is.getItemMeta() instanceof PotionMeta && Main.version >= 14) {
                PotionMeta potionMeta = (PotionMeta) is.getItemMeta();
                KitsConfig.set(path + ".potion.type", potionMeta.getBasePotionData().getType().name());
                KitsConfig.set(path + ".potion.extended", potionMeta.getBasePotionData().isExtended());
                KitsConfig.set(path + ".potion.upgraded", potionMeta.getBasePotionData().isUpgraded());
                KitsConfig.set(path + ".lore", potionMeta.getLore());
            } else if (is.getItemMeta().hasEnchants()) {
                Map<Enchantment, Integer> enchants = is.getEnchantments();
                Collection<String> enchantList = new ArrayList<>();
                for (Enchantment e : is.getEnchantments().keySet()) {
                    int level = enchants.get(e);
                    enchantList.add(e.getName().toUpperCase() + ":" + level);
                }
                KitsConfig.set(path + ".enchants", enchantList);
            }
            if (is.getItemMeta().hasLore()) KitsConfig.set(path + ".lore", is.getItemMeta().getLore());
        }
    }

    static void setKit(Player player, String kitName) {
        String path = "Kits." + kitName + ".";
        player.getInventory().clear();
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
        ConfigurationSection itemsSection = KitsConfig.getConfigurationSection(path + "items");
        ConfigurationSection armorSection = KitsConfig.getConfigurationSection(path + "armor");
        ConfigurationSection offhandSection = KitsConfig.getConfigurationSection(path + "offhand");
        ConfigurationSection effectsSection = KitsConfig.getConfigurationSection(path + "effects");
        setItems(player, path, itemsSection);
        setArmor(player, path, armorSection);
        if (Main.version >= 15) setOffhand(player, path, offhandSection);
        setPlayerEffects(player, path, effectsSection);
        player.updateInventory();
    }

    private static void setItems(Player player, String path, ConfigurationSection itemSection) {
        if (itemSection == null) return;
        for (String string : Objects.requireNonNull(itemSection).getKeys(false)) {
            int slot = Integer.parseInt(string);
            String slotPath = path + "items." + slot + ".";
            String name = KitsConfig.getString(slotPath + "name");
            List<String> enchants = KitsConfig.getStringList(slotPath + "enchants");
            ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(KitsConfig.getString(slotPath + "type")))), KitsConfig.getInt(slotPath + "amount"));
            if (KitsConfig.getString(slotPath + "potion") != null && Main.version >= 14) {
                effect(slotPath, is);
                player.getInventory().setItem(slot, is);
            } else if (is.getType().equals(Material.ENCHANTED_BOOK)) {
                for (String strings : enchants) {
                    storageEnchant(is, strings);
                    player.getInventory().setItem(slot, is);
                }
            } else {
                if (Main.version <= 18) is.setDurability((short) KitsConfig.getInt(slotPath + "durability"));
                ItemMeta im = is.getItemMeta();
                if (im != null) {
                    enchant(name, enchants, im, slotPath);
                    im.setLore(KitsConfig.getStringList(slotPath + "lore"));
                    is.setItemMeta(im);
                    player.getInventory().setItem(slot, is);
                }
            }
        }
    }

    private static void setArmor(Player player, String path, ConfigurationSection armorSection) {
        if (armorSection == null) return;
        for (String string : Objects.requireNonNull(armorSection).getKeys(false)) {
            String slotPath = path + "armor." + string + ".";
            ItemStack is = itemStack(slotPath);
            ItemMeta im = is.getItemMeta();
            if (Main.version <= 18) is.setDurability((short) KitsConfig.getInt(slotPath + "durability"));
            if (im == null) continue;
            enchant(KitsConfig.getString(slotPath + "name"), KitsConfig.getStringList(slotPath + "enchants"), im, slotPath);
            is.setItemMeta(im);
            if (slotPath.contains("HELMET")) player.getInventory().setHelmet(is);
            else if (slotPath.contains("CHESTPLATE")) player.getInventory().setChestplate(is);
            else if (slotPath.contains("LEGGINGS")) player.getInventory().setLeggings(is);
            else if (slotPath.contains("BOOTS")) player.getInventory().setBoots(is);
        }
    }

    private static void setOffhand(Player player, String path, ConfigurationSection offhandSection) {
        if (offhandSection == null) return;
        List<String> strings = new ArrayList<>(Objects.requireNonNull(offhandSection).getKeys(false));
        String slotPath = path + "offhand." + strings.get(0) + ".";
        String type = KitsConfig.getString(slotPath + "type");
        String name = KitsConfig.getString(slotPath + "name");
        short durability = 0;
        if (Main.version <= 18) durability = (short) KitsConfig.getInt(slotPath + "durability");
        List<String> enchants = KitsConfig.getStringList(slotPath + "enchants");
        int amount = KitsConfig.getInt(slotPath + "amount");
        ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(type))), amount);
        if (KitsConfig.getString(slotPath + "potion") != null) {
            effect(slotPath, is);
            if (Main.version >= 14) player.getInventory().setItemInOffHand(is);
        } else if (is.getType().equals(Material.ENCHANTED_BOOK)) {
            for (String s : enchants) {
                storageEnchant(is, s);
                if (Main.version >= 14) player.getInventory().setItemInOffHand(is);
            }
        } else {
            ItemMeta im = is.getItemMeta();
            if (Main.version <= 18) is.setDurability(durability);
            if (im != null) enchant(name, enchants, im, slotPath);
            is.setItemMeta(im);
            if (Main.version >= 14) player.getInventory().setItemInOffHand(is);
        }
    }

    private static void setPlayerEffects(Player p, String path, ConfigurationSection effectSection) {
        if (effectSection == null) return;
        Iterable<String> effects = new ArrayList<>(Objects.requireNonNull(effectSection).getKeys(false));
        for (String s : effects) {
            PotionEffectType type = PotionEffectType.getByName(s);
            int amplifier = KitsConfig.getInt(path + "effects." + s.toUpperCase() + ".amplifier");
            int duration = KitsConfig.getInt(path + "effects." + s.toUpperCase() + ".duration");
            PotionEffect effect = new PotionEffect(Objects.requireNonNull(type), duration, amplifier);
            p.addPotionEffect(effect);
        }
    }

    private static ItemStack itemStack(String pathing) {
        return new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(KitsConfig.getString(pathing + "type")))), KitsConfig.getInt(pathing + "amount"));
    }

    private static void effect(String pathing, ItemStack is) {
        PotionType potionType = PotionType.valueOf(KitsConfig.getString(pathing + "potion.type"));
        boolean extended = KitsConfig.getBoolean(pathing + "potion.extended");
        boolean upgraded = KitsConfig.getBoolean(pathing + "potion.upgraded");
        PotionMeta potionMeta = (PotionMeta) is.getItemMeta();
        if (Main.version >= 14 && potionMeta != null) potionMeta.setBasePotionData(new PotionData(potionType, extended, upgraded));
        is.setItemMeta(potionMeta);
    }

    private static void storageEnchant(ItemStack is, String s) {
        String[] stringEnchants = s.split(":");
        EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) is.getItemMeta();
        if (storageMeta != null)
            storageMeta.addStoredEnchant(Objects.requireNonNull(Enchantment.getByName(stringEnchants[0])), Integer.parseInt(stringEnchants[1]), true);
        is.setItemMeta(storageMeta);
    }

    private static void enchant(String name, Iterable<String> enchants, ItemMeta im, String slotPath) {
        if (name != null) im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        im.setLore(KitsConfig.getStringList(slotPath + "lore"));
        if (KitsConfig.getBoolean(slotPath + "unbreakable") && Main.version >= 14) im.setUnbreakable(true);
        for (String s1 : enchants) {
            String[] stringEnchants = s1.split(":");
            im.addEnchant(Objects.requireNonNull(Enchantment.getByName(stringEnchants[0])), Integer.parseInt(stringEnchants[1]), true);
        }
    }

    @SneakyThrows
    private void loadConfigs() {
        config.load(plugin.customConfigFile);
        KitsConfig.load(plugin.KitsConfigfile);
    }

    private static void send(Player p, String s) {
        p.sendMessage(Main.conf(s));
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) return new ArrayList<>(Arrays.asList("create", "give", "list", "remove", "unbreakable"));
        else if (args.length == 2) {
            List<String> arr = new ArrayList<>();
            if (args[0].equals("create")) arr.add("[name]");
            else if (args[0].equals("remove") || args[0].equals("give")) arr.addAll(kits);
            return arr;
        }
        return Collections.emptyList();
    }
}
