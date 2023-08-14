package net.flex.ManualTournaments.commands.kitCommands;

import lombok.SneakyThrows;
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
import org.bukkit.entity.TropicalFish;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapView;
import org.bukkit.potion.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @SneakyThrows
    private static ItemStack[] setItems(Player player, String path, Inventory inventory, ConfigurationSection itemSection) {
        if (itemSection == null) return null;
        for (String string : Objects.requireNonNull(itemSection).getKeys(false)) {
            if (!string.equals("armor") && !string.equals("offhand") && !string.equals("effects")) {
                int slot = Integer.parseInt(string);
                String slotPath = path + slot + ".";
                ItemStack is = setItemMeta(player, slotPath);
                inventory.setItem(slot, is);
            }
        }
        return inventory.getContents();
    }

    private static void setArmor(Player player, String path, ConfigurationSection armorSection) {
        if (armorSection == null) return;
        for (String string : Objects.requireNonNull(armorSection).getKeys(false)) {
            String slotPath = path + "armor." + string + ".";
            ItemStack is = setItemMeta(player, slotPath);
            if (slotPath.contains("HELMET")) player.getInventory().setHelmet(is);
            else if (slotPath.contains("CHESTPLATE")) player.getInventory().setChestplate(is);
            else if (slotPath.contains("LEGGINGS")) player.getInventory().setLeggings(is);
            else if (slotPath.contains("BOOTS")) player.getInventory().setBoots(is);
        }
    }

    private static void setOffhand(Player player, String path, ConfigurationSection offhandSection) {
        if (offhandSection == null) return;
        String firstKey = Objects.requireNonNull(offhandSection).getKeys(false).iterator().next();
        String slotPath = path + "offhand." + firstKey + ".";
        ItemStack is = setItemMeta(player, slotPath);
        if (Main.version >= 14) player.getInventory().setItemInOffHand(is);
    }

    private static void setPlayerEffects(Player p, String path, ConfigurationSection effectSection) {
        if (effectSection == null) return;
        for (String string : Objects.requireNonNull(effectSection).getKeys(false)) {
            PotionEffectType type = PotionEffectType.getByName(string);
            String effectPath = path + "effects." + string + ".";
            int duration = getKitConfig().getInt(effectPath + "duration");
            int amplifier = getKitConfig().getInt(effectPath + "amplifier");
            boolean ambient = getKitConfig().getBoolean(effectPath + "ambient");
            boolean particles = getKitConfig().getBoolean(effectPath + "particles");
            boolean icon = getKitConfig().getBoolean(effectPath + "icon");
            if (type != null) {
                PotionEffect effect = new PotionEffect(type, duration, amplifier, ambient, particles, icon);
                p.addPotionEffect(effect);
            }
        }
    }

    private static ItemStack setItemMeta(Player player, String slotPath) {
        String type = getKitConfig().getString(slotPath + "type");
        int amount = getKitConfig().getInt(slotPath + "amount");
        int durability = getKitConfig().getInt(slotPath + "durability");
        ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(type))), amount);
        ItemMeta im = is.getItemMeta();



        if (version <= 18 && durability != 0) {
            is.setDurability((short) durability);
        }

        if (im == null) return null;

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
        List<String> recipes = getKitConfig().getStringList(slotPath + "recipes");
        String potionPath = getKitConfig().getString(slotPath + "potion");

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
            for (String enchant : enchants) {
                String[] stringEnchants = enchant.split(" = ");
                NamespacedKey enchantmentKey = NamespacedKey.fromString(stringEnchants[0], getPlugin());
                Enchantment enchantment = Enchantment.getByKey(enchantmentKey);
                if (enchantment != null) {
                    im.addEnchant(enchantment, Integer.parseInt(stringEnchants[1]), true);
                }
            }
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
            for (String enchant : enchants) {
                String[] stringEnchants = enchant.split(" = ");
                NamespacedKey enchantmentKey = NamespacedKey.fromString(stringEnchants[0]);
                Enchantment enchantment = Enchantment.getByKey(enchantmentKey);
                EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) is.getItemMeta();
                if (storageMeta != null && enchantment != null) {
                    storageMeta.addStoredEnchant(enchantment, Integer.parseInt(stringEnchants[1]), true);
                }
            }
        }

        if (im instanceof FireworkMeta) {
            FireworkMeta fireworkMeta = (FireworkMeta) im;
            ConfigurationSection fireworkSection = getKitConfig().getConfigurationSection(slotPath + "firework");
            for (String firework : Objects.requireNonNull(fireworkSection).getKeys(false)) {
                int fireworkSlot = Integer.parseInt(firework);
                String fireworkPath = slotPath + "firework." + fireworkSlot + ".";
                FireworkEffect.Type fireworkType = FireworkEffect.Type.valueOf(getKitConfig().getString(fireworkPath + "type"));
                int fireworkDuration = getKitConfig().getInt(fireworkPath + "duration");
                boolean flicker = getKitConfig().getBoolean(fireworkPath + "flicker");
                boolean trail = getKitConfig().getBoolean(fireworkPath + "trail");
                List<Integer> colors = getKitConfig().getIntegerList(fireworkPath + "colors");
                List<Color> colorsRGB = colors.stream().mapToInt(color -> color).mapToObj(Color::fromRGB).collect(Collectors.toList());
                List<Integer> fadeColors = getKitConfig().getIntegerList(fireworkPath + "fadeColors");
                List<Color> fadeColorsRGB = fadeColors.stream().mapToInt(color -> color).mapToObj(Color::fromRGB).collect(Collectors.toList());
                FireworkEffect fireworkEffect = FireworkEffect.builder()
                        .with(fireworkType)
                        .flicker(flicker)
                        .trail(trail)
                        .withColor(colorsRGB)
                        .withFade(fadeColorsRGB)
                        .build();
                fireworkMeta.addEffect(fireworkEffect);
                fireworkMeta.setPower(fireworkDuration);
            }
        }

        if (im instanceof KnowledgeBookMeta) {
            KnowledgeBookMeta knowledgeBookMeta = (KnowledgeBookMeta) im;
            List<NamespacedKey> recipeKeyList = new ArrayList<>();
            for (String recipeString : recipes) {
                recipeKeyList.add(NamespacedKey.fromString(recipeString));
            }
            knowledgeBookMeta.setRecipes(recipeKeyList);
        }

        if (im instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) im;
            Color color = Color.fromRGB(getKitConfig().getInt(slotPath + "color"));
            leatherArmorMeta.setColor(color);
        }

        if (im instanceof MapMeta) {
            MapMeta mapMeta = (MapMeta) im;
            Color mapColor = Color.fromRGB(getKitConfig().getInt(slotPath + "map.color"));
            mapMeta.setColor(mapColor);
            boolean scaling = getKitConfig().getBoolean(slotPath + "map.scaling");
            mapMeta.setScaling(scaling);
            World world = Bukkit.getWorld(Objects.requireNonNull(getKitConfig().getString(slotPath + "map.view.world")));
            MapView mapView = Bukkit.createMap(Objects.requireNonNull(world));
            int id = getKitConfig().getInt(slotPath + "map.view.id");
            mapMeta.setMapId(id);
            MapView.Scale scale = MapView.Scale.valueOf(getKitConfig().getString(slotPath + "map.view.scale"));
            mapView.setScale(scale);
            mapView.setWorld(world);
            int centerX = getKitConfig().getInt(slotPath + "map.view.centerX");
            mapView.setCenterX(centerX);
            int centerZ = getKitConfig().getInt(slotPath + "map.view.centerZ");
            mapView.setCenterZ(centerZ);
            boolean locked = getKitConfig().getBoolean(slotPath + "map.view.locked");
            mapView.setLocked(locked);
            boolean tracked = getKitConfig().getBoolean(slotPath + "map.view.tracking");
            mapView.setTrackingPosition(tracked);
            boolean unlimitedTracking = getKitConfig().getBoolean(slotPath + "map.view.unlimitedTracking");
            mapView.setUnlimitedTracking(unlimitedTracking);
            mapMeta.setMapView(mapView);
        }

        if (im instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) im;
            if (version >= 14) {
                PotionType potionMetaType = PotionType.valueOf(getKitConfig().getString(slotPath + "potion.type"));
                boolean metaExtended = getKitConfig().getBoolean(slotPath + "potion.extended");
                boolean metaUpgraded = getKitConfig().getBoolean(slotPath + "potion.upgraded");
                potionMeta.setBasePotionData(new PotionData(potionMetaType, metaExtended, metaUpgraded));
                is.setItemMeta(potionMeta);
            } else if (version >= 13) {
                PotionType potionType = PotionType.valueOf(getKitConfig().getString(slotPath + "potion.type"));
                int level = getKitConfig().getInt(slotPath + "potion.level");
                boolean splash = getKitConfig().getBoolean(slotPath + "potion.splash");
                boolean extended = getKitConfig().getBoolean(slotPath + "potion.extended");
                Potion potion = new Potion(potionType, level, splash, extended);
                is.setItemMeta(potion.toItemStack(is.getAmount()).getItemMeta());
            }
        }

        if (im instanceof Repairable) {
            Repairable repairable = (Repairable) im;
            int repairCost = getKitConfig().getInt(slotPath + "repair");
            repairable.setRepairCost(repairCost);
        }

        if (im instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) im;
            String uuidString = getKitConfig().getString(slotPath + "skull.owner");
            UUID uuid = UUID.fromString(Objects.requireNonNull(uuidString));
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            if (version >= 32) {
                NamespacedKey noteblockSound = NamespacedKey.fromString(Objects.requireNonNull(getKitConfig().getString(slotPath + "skull.sound")));
                skullMeta.setNoteBlockSound(noteblockSound);
            }
        }

        if (im instanceof SuspiciousStewMeta) {
            SuspiciousStewMeta stewMeta = (SuspiciousStewMeta) im;
            ConfigurationSection stewSection = getKitConfig().getConfigurationSection(slotPath + "stew");
            for (String stew : Objects.requireNonNull(stewSection).getKeys(false)) {
                int stewSlot = Integer.parseInt(stew);
                String stewPath = slotPath + "stew." + stewSlot + ".";
                PotionEffectType stewType = PotionEffectType.getByName(Objects.requireNonNull(getKitConfig().getString(stewPath + "type")));
                int stewDuration = getKitConfig().getInt(stewPath + "duration");
                int stewAmplifier = getKitConfig().getInt(stewPath + "amplifier");
                boolean stewAmbient = getKitConfig().getBoolean(stewPath + "ambient");
                boolean stewParticles = getKitConfig().getBoolean(stewPath + "particles");
                boolean stewIcon = getKitConfig().getBoolean(stewPath + "icon");
                if (stewType != null) {
                    PotionEffect potionEffect = new PotionEffect(stewType, stewDuration, stewAmplifier, stewAmbient, stewParticles, stewIcon);
                    stewMeta.addCustomEffect(potionEffect, true);
                }
            }
        }

        if (im instanceof TropicalFishBucketMeta) {
            TropicalFishBucketMeta fishMeta = (TropicalFishBucketMeta) im;
            DyeColor fishBodyColor = DyeColor.valueOf(getKitConfig().getString(slotPath + "fish.bodyColor"));
            fishMeta.setBodyColor(fishBodyColor);
            TropicalFish.Pattern fishPattern = TropicalFish.Pattern.valueOf(getKitConfig().getString(slotPath + "fish.pattern"));
            fishMeta.setPattern(fishPattern);
            DyeColor fishPatternColor = DyeColor.valueOf(getKitConfig().getString(slotPath + "fish.patternColor"));
            fishMeta.setPatternColor(fishPatternColor);
        }

        is.setItemMeta(im);
        return is;
    }

    @Override
    public void execute(Player player, String kitName, boolean kitExists) {
        if (kitExists) {
            setKit(player, kitName);
            send(player, "kit-given");
        } else send(player, "kit-not-exists");
    }
}
