package net.flex.ManualTournaments.commands.kitCommands;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.KitCommand;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class GiveKit implements KitCommand {
    public static void setKit(Player player, String kitName) {
        String path = "Kits." + kitName + ".";
        player.getInventory().clear();
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setSaturation(0);
        if (Main.version >= 22) player.setAbsorptionAmount(0);
        player.setFireTicks(0);
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
        ConfigurationSection itemsSection = getKitConfig().getConfigurationSection(path);
        ConfigurationSection armorSection = getKitConfig().getConfigurationSection(path + "armor");
        ConfigurationSection offhandSection = getKitConfig().getConfigurationSection(path + "offhand");
        ConfigurationSection effectsSection = getKitConfig().getConfigurationSection(path + "effects");
        setItems(player, path, player.getInventory(), itemsSection);
        setArmor(player, path, armorSection);
        if (Main.version >= 15) setOffhand(player, path, offhandSection);
        setPlayerEffects(player, path, effectsSection);
    }

    private static ItemStack[] setItems(Player player, String path, Inventory inventory, ConfigurationSection itemSection) {
        if (itemSection == null) return null;
        for (String string : Objects.requireNonNull(itemSection).getKeys(false)) {
            int slot = Integer.parseInt(string);
            String slotPath = path + slot + ".";

            String type = getKitConfig().getString(slotPath + "type");
            if (type != null) {
                Bukkit.broadcastMessage(type);
            }
            int amount = getKitConfig().getInt(slotPath + "amount");
            int durability = getKitConfig().getInt(slotPath + "durability");
            ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(type))), amount);
            ItemMeta im = is.getItemMeta();

            if (version <= 18 && durability != 0) {
                is.setDurability((short) durability);
            }

            if (im == null) return null;


            String potion = getKitConfig().getString(slotPath + "potion");


            String name = getKitConfig().getString(slotPath + "name");
            List<String> lore = getKitConfig().getStringList(slotPath + "lore");
            List<String> flags = getKitConfig().getStringList(slotPath + "flags");
            int modelData = getKitConfig().getInt(slotPath + "modelData");
            boolean unbreakable = getKitConfig().getBoolean(slotPath + "unbreakable");
            List<String> enchants = getKitConfig().getStringList(slotPath + "enchants");
            String trimMaterial = getKitConfig().getString(slotPath + "trim.material");
            String trimPattern = getKitConfig().getString(slotPath + "trim.pattern");
            String axolotl = getKitConfig().getString(slotPath + "variant");
            List<String> patterns = getKitConfig().getStringList(slotPath + "patterns");
            String author = getKitConfig().getString(slotPath + "book.author");
            String generation = getKitConfig().getString(slotPath + "book.generation");
            String title = getKitConfig().getString(slotPath + "book.title");
            List<String> pages = getKitConfig().getStringList(slotPath + "book.pages");
            boolean tracking = getKitConfig().getBoolean(slotPath + "lodestone.tracking");
            int damage = getKitConfig().getInt(slotPath + "damage");

            if (name != null) {
                im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            }

            if (!lore.isEmpty()) {
                im.setLore(lore);
            }

            if (!flags.isEmpty()) {
                for (String flag : flags) {
                    ItemFlag itemFlag = ItemFlag.valueOf(flag);
                    im.addItemFlags(itemFlag);
                }
            }

            if (modelData != 0) {
                im.setCustomModelData(modelData);
            }

            if (unbreakable && version >= 14) {
                im.setUnbreakable(true);
            }

            if (!enchants.isEmpty()) {
                enchant(enchants, im);
            }

            if (im instanceof AxolotlBucketMeta && axolotl != null) {
                AxolotlBucketMeta axolotlMeta = (AxolotlBucketMeta) im;
                Axolotl.Variant variant = Axolotl.Variant.valueOf(axolotl);
                axolotlMeta.setVariant(variant);
            }

            if (im instanceof BannerMeta) {
                BannerMeta bannerMeta = (BannerMeta) im;
                for (String patternString : patterns) {
                    String[] parts = patternString.split(": ");
                    PatternType patternType = PatternType.valueOf(parts[0]);
                    DyeColor color = DyeColor.valueOf(parts[1]);
                    Pattern pattern = new Pattern(color, patternType);
                    bannerMeta.addPattern(pattern);
                }
            }

            if (im instanceof BlockStateMeta) {
                BlockStateMeta blockStateMeta = (BlockStateMeta) im;
                if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
                    ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                    ConfigurationSection storageSection = getKitConfig().getConfigurationSection(slotPath + "storage");
                    shulkerBox.getInventory().setContents(setItems(player, slotPath + "storage.", shulkerBox.getInventory(), storageSection));
                    blockStateMeta.setBlockState(shulkerBox);
                }
            }

            if (im instanceof BookMeta) {
                BookMeta bookMeta = (BookMeta) im;
                if (author != null) {
                    bookMeta.setAuthor(author);
                }
                if (generation != null) {
                    bookMeta.setGeneration(BookMeta.Generation.valueOf(generation));
                }
                if (title != null)  {
                    bookMeta.setTitle(title);
                }
                bookMeta.setPages(pages);
            }

            if (im instanceof CompassMeta) {
                CompassMeta compassMeta = (CompassMeta) im;
                String lodestonePath = slotPath + "lodestone.location.";
                Location lodestoneLocation = CreateKit.lodestoneLocation(lodestonePath);
                compassMeta.setLodestone(lodestoneLocation);
                compassMeta.setLodestoneTracked(tracking);
            }

            if (im instanceof CrossbowMeta) {
                CrossbowMeta crossbowMeta = (CrossbowMeta) im;
                ConfigurationSection crossbowSection = getKitConfig().getConfigurationSection(slotPath + "projectiles");
                Inventory projecticlesInventory = Bukkit.createInventory(null, InventoryType.PLAYER, "");
                ItemStack[] projectiles = setItems(player, slotPath + "projectiles.", projecticlesInventory, crossbowSection);
                if (projectiles != null) {
                    for (ItemStack projectile : projectiles) {
                        if (projectile != null) {
                            crossbowMeta.addChargedProjectile(projectile);
                        }
                    }
                }
            }

            if (im instanceof Damageable) {
                Damageable damageable = (Damageable) im;
                damageable.setDamage(damage);
            }

            if (im instanceof EnchantmentStorageMeta) {
                for (String strings : enchants) {
                    String[] stringEnchants = strings.split(" = ");
                    NamespacedKey enchantmentKey = NamespacedKey.fromString(stringEnchants[0], getPlugin());
                    Enchantment enchantment = Enchantment.getByKey(enchantmentKey);
                    EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) is.getItemMeta();
                    if (storageMeta != null && enchantment != null) {
                        storageMeta.addStoredEnchant(enchantment, Integer.parseInt(stringEnchants[1]), true);
                    }
                    player.getInventory().setItem(slot, is);
                }
            }

            if (im instanceof FireworkMeta) {
                FireworkMeta fireworkMeta = (FireworkMeta) im;


            }


            if (potion != null) {
                effect(slotPath, is);
                player.getInventory().setItem(slot, is);
            }




            is.setItemMeta(im);
            inventory.setItem(slot, is);
        }
        return inventory.getContents();
    }

    private static void setArmor(Player player, String path, ConfigurationSection armorSection) {
        if (armorSection == null) return;
        for (String string : Objects.requireNonNull(armorSection).getKeys(false)) {
            String slotPath = path + "armor." + string + ".";
            ItemStack is = itemStack(slotPath);
            ItemMeta im = is.getItemMeta();
            if (Main.version <= 18) is.setDurability((short) getKitConfig().getInt(slotPath + "durability"));
            if (im == null) continue;
            enchant(getKitConfig().getStringList(slotPath + "enchants"), im);
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
        String type = getKitConfig().getString(slotPath + "type");
        String name = getKitConfig().getString(slotPath + "name");
        short durability = 0;
        if (Main.version <= 18) durability = (short) getKitConfig().getInt(slotPath + "durability");
        List<String> enchants = getKitConfig().getStringList(slotPath + "enchants");
        int amount = getKitConfig().getInt(slotPath + "amount");
        ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(type))), amount);
        if (getKitConfig().getString(slotPath + "potion") != null) {
            effect(slotPath, is);
            if (Main.version >= 14) player.getInventory().setItemInOffHand(is);
        } else if (is.getType().equals(Material.ENCHANTED_BOOK)) {
            for (String s : enchants) {
                if (Main.version >= 14) player.getInventory().setItemInOffHand(is);
            }
        } else {
            ItemMeta im = is.getItemMeta();
            if (Main.version <= 18) is.setDurability(durability);
            if (im != null) enchant(enchants, im);
            is.setItemMeta(im);
            if (Main.version >= 14) player.getInventory().setItemInOffHand(is);
        }
    }

    private static void setPlayerEffects(Player p, String path, ConfigurationSection effectSection) {
        if (effectSection == null) return;
        Iterable<String> effects = new ArrayList<>(Objects.requireNonNull(effectSection).getKeys(false));
        for (String s : effects) {
            PotionEffectType type = PotionEffectType.getByName(s);
            int amplifier = getKitConfig().getInt(path + "effects." + s.toUpperCase() + ".amplifier");
            int duration = getKitConfig().getInt(path + "effects." + s.toUpperCase() + ".duration");
            PotionEffect effect = new PotionEffect(Objects.requireNonNull(type), duration, amplifier);
            p.addPotionEffect(effect);
        }
    }

    private static ItemStack itemStack(String pathing) {
        return new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(getKitConfig().getString(pathing + "type")))), getKitConfig().getInt(pathing + "amount"));
    }

    private static void effect(String pathing, ItemStack is) {
        PotionType potionMetaType = PotionType.valueOf(getKitConfig().getString(pathing + "potion.type"));
        boolean metaExtended = getKitConfig().getBoolean(pathing + "potion.extended");
        boolean metaUpgraded = getKitConfig().getBoolean(pathing + "potion.upgraded");
        PotionMeta potionMeta = (PotionMeta) is.getItemMeta();
        if (Main.version >= 14 && potionMeta != null) {
            potionMeta.setBasePotionData(new PotionData(potionMetaType, metaExtended, metaUpgraded));
            is.setItemMeta(potionMeta);
        } else if (Main.version >= 13) {
            PotionType potionType = PotionType.valueOf(getKitConfig().getString(pathing + "potion.type"));
            int level = getKitConfig().getInt(pathing + "potion.level");
            boolean splash = getKitConfig().getBoolean(pathing + "potion.splash");
            boolean extended = getKitConfig().getBoolean(pathing + "potion.extended");
            Potion potion = new Potion(potionType, level, splash, extended);
            is.setItemMeta(potion.toItemStack(is.getAmount()).getItemMeta());
        }
    }

    private static void enchant(Iterable<String> enchants, ItemMeta im) {
        for (String enchant : enchants) {
            String[] stringEnchants = enchant.split(" = ");
            NamespacedKey enchantmentKey = NamespacedKey.fromString(stringEnchants[0], getPlugin());
            Enchantment enchantment = Enchantment.getByKey(enchantmentKey);
            if (enchantment != null) {
                im.addEnchant(enchantment, Integer.parseInt(stringEnchants[1]), true);
            }
        }
    }

    @Override
    public void execute(Player player, String kitName, boolean kitExists) {
        if (kitExists) {
            setKit(player, kitName);
            send(player, "kit-given");
        } else send(player, "kit-not-exists");
    }
}
