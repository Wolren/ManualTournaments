package net.flex.ManualTournaments.commands.kitCommands;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.KitCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.flex.ManualTournaments.Main.getKitsConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class GiveKit implements KitCommand {
    @Override
    public void execute(Player player, String kitName, boolean kitExists) {
        if (kitExists) {
            setKit(player, kitName);
            send(player, "kit-given");
        } else send(player, "kit-not-exists");
    }

    public static void setKit(Player player, String kitName) {
        String path = "Kits." + kitName + ".";
        player.getInventory().clear();
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setSaturation(0);
        if (Main.version >= 22) player.setAbsorptionAmount(0);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
        ConfigurationSection itemsSection = getKitsConfig().getConfigurationSection(path + "items");
        ConfigurationSection armorSection = getKitsConfig().getConfigurationSection(path + "armor");
        ConfigurationSection offhandSection = getKitsConfig().getConfigurationSection(path + "offhand");
        ConfigurationSection effectsSection = getKitsConfig().getConfigurationSection(path + "effects");
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
            String name = getKitsConfig().getString(slotPath + "name");
            List<String> enchants = getKitsConfig().getStringList(slotPath + "enchants");
            ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(getKitsConfig().getString(slotPath + "type")))), getKitsConfig().getInt(slotPath + "amount"));
            if (getKitsConfig().getString(slotPath + "potion") != null) {
                effect(slotPath, is);
                player.getInventory().setItem(slot, is);
            } else if (is.getType().equals(Material.ENCHANTED_BOOK)) {
                for (String strings : enchants) {
                    storageEnchant(is, strings);
                    player.getInventory().setItem(slot, is);
                }
            } else {
                if (Main.version <= 18) is.setDurability((short) getKitsConfig().getInt(slotPath + "durability"));
                ItemMeta im = is.getItemMeta();
                if (im != null) {
                    enchant(name, enchants, im, slotPath);
                    im.setLore(getKitsConfig().getStringList(slotPath + "lore"));
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
            if (Main.version <= 18) is.setDurability((short) getKitsConfig().getInt(slotPath + "durability"));
            if (im == null) continue;
            enchant(getKitsConfig().getString(slotPath + "name"), getKitsConfig().getStringList(slotPath + "enchants"), im, slotPath);
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
        String type = getKitsConfig().getString(slotPath + "type");
        String name = getKitsConfig().getString(slotPath + "name");
        short durability = 0;
        if (Main.version <= 18) durability = (short) getKitsConfig().getInt(slotPath + "durability");
        List<String> enchants = getKitsConfig().getStringList(slotPath + "enchants");
        int amount = getKitsConfig().getInt(slotPath + "amount");
        ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(type))), amount);
        if (getKitsConfig().getString(slotPath + "potion") != null) {
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
            int amplifier = getKitsConfig().getInt(path + "effects." + s.toUpperCase() + ".amplifier");
            int duration = getKitsConfig().getInt(path + "effects." + s.toUpperCase() + ".duration");
            PotionEffect effect = new PotionEffect(Objects.requireNonNull(type), duration, amplifier);
            p.addPotionEffect(effect);
        }
    }

    private static ItemStack itemStack(String pathing) {
        return new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(getKitsConfig().getString(pathing + "type")))), getKitsConfig().getInt(pathing + "amount"));
    }

    private static void effect(String pathing, ItemStack is) {
        PotionType potionMetaType = PotionType.valueOf(getKitsConfig().getString(pathing + "potion.type"));
        boolean metaExtended = getKitsConfig().getBoolean(pathing + "potion.extended");
        boolean metaUpgraded = getKitsConfig().getBoolean(pathing + "potion.upgraded");
        PotionMeta potionMeta = (PotionMeta) is.getItemMeta();
        if (Main.version >= 14 && potionMeta != null) {
            potionMeta.setBasePotionData(new PotionData(potionMetaType, metaExtended, metaUpgraded));
            is.setItemMeta(potionMeta);
        } else if (Main.version >= 13) {
            PotionType potionType = PotionType.valueOf(getKitsConfig().getString(pathing + "potion.type"));
            int level = getKitsConfig().getInt(pathing + "potion.level");
            boolean splash = getKitsConfig().getBoolean(pathing + "potion.splash");
            boolean extended = getKitsConfig().getBoolean(pathing + "potion.extended");
            Potion potion = new Potion(potionType, level, splash, extended);
            is.setItemMeta(potion.toItemStack(is.getAmount()).getItemMeta());
        }
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
        im.setLore(getKitsConfig().getStringList(slotPath + "lore"));
        if (getKitsConfig().getBoolean(slotPath + "unbreakable") && Main.version >= 14) im.setUnbreakable(true);
        for (String s1 : enchants) {
            String[] stringEnchants = s1.split(":");
            im.addEnchant(Objects.requireNonNull(Enchantment.getByName(stringEnchants[0])), Integer.parseInt(stringEnchants[1]), true);
        }
    }
}
