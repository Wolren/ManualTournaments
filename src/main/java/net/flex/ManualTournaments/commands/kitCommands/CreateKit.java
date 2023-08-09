package net.flex.ManualTournaments.commands.kitCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.KitCommand;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapView;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

import java.util.*;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class CreateKit implements KitCommand {
    public static void saveLodestoneLocation(Location location, String path) {
        getKitConfig().set(path + "world", Objects.requireNonNull(location.getWorld()).getName());
        getKitConfig().set(path + "x", location.getX());
        getKitConfig().set(path + "y", location.getY());
        getKitConfig().set(path + "z", location.getZ());
    }

    public static Location lodestoneLocation(String path) {
        World world = Bukkit.getWorld(Objects.requireNonNull(getKitConfig().get(path + "world")).toString());
        double x = getKitConfig().getDouble(path + "x");
        double y = getKitConfig().getDouble(path + "y");
        double z = getKitConfig().getDouble(path + "z");
        return new Location(world, x, y, z);
    }

    @SneakyThrows
    @Override
    public void execute(Player player, String kitName, boolean kitExists) {
        if (!kitExists) {
            if (Objects.requireNonNull(config.getString("current-arena")).isEmpty()) {
                config.set("current-kit", kitName);
                config.save(getCustomConfigFile());
            }
            getKit(player, kitName);
            kitNames.add(kitName);
            send(player, "kit-made");
        } else player.sendMessage(message("kit-already-exists"));
    }

    @SneakyThrows
    private void getKit(Player player, String kitName) {
        String path = "Kits." + kitName + ".";
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack is = inventory.getItem(i);
            if (is != null) {
                String itemPath = path + i + ".";
                getType(itemPath, is);
            }
        }

        for (ItemStack armor : inventory.getArmorContents()) {
            if (armor != null) {
                String armorPath = path + "armor." + armor.getType().name() + ".";
                getKitConfig().set(armorPath, inventory.getArmorContents());
                getType(armorPath, armor);
            }
        }

        if (Main.version >= 15) {
            ItemStack offhand = inventory.getItemInOffHand();
            if (!offhand.getType().equals(Material.AIR)) {
                String offhandPath = path + "offhand." + offhand.getType().name() + ".";
                getKitConfig().set(offhandPath, offhand);
                getType(offhandPath, offhand);
            }
        }

        player.getActivePotionEffects().forEach(effect -> {
            String effectPath = path + "effects." + effect.getType().getName() + ".";
            getKitConfig().set(effectPath + "duration", effect.getDuration());
            getKitConfig().set(effectPath + "amplifier", effect.getAmplifier());
            if (effect.isAmbient()) {
                getKitConfig().set(effectPath + "ambient", effect.isAmbient());
            }
            if (effect.hasParticles()) {
                getKitConfig().set(effectPath + "particles", effect.hasParticles());
            }
            if (effect.hasIcon()) {
                getKitConfig().set(effectPath+ "icon", effect.hasIcon());
            }
        });

        getKitConfig().save(getKitConfigFile());
    }

    private void getType(String path, ItemStack is) {
        ItemMeta im = is.getItemMeta();

        getKitConfig().set(path + "type", is.getType().name());

        getKitConfig().set(path + "amount", is.getAmount());

        if (Main.version <= 18) {
            getKitConfig().set(path + "durability", is.getDurability());
        }

        if (im == null) return;

        if (im.hasDisplayName()) {
            getKitConfig().set(path + "name", im.getDisplayName());
        }

        if (im.hasLore()) {
            getKitConfig().set(path + "lore", im.getLore());
        }

        if (!im.getItemFlags().isEmpty()) {
            Set<String> set = new HashSet<>();
            for (ItemFlag itemFlag : im.getItemFlags()) {
                String name = itemFlag.name();
                set.add(name);
            }
            config.set(path + "flags", set);
        }

        if (im.hasCustomModelData()) {
            config.set(path + "modelData", im.getCustomModelData());
        }

        if (Main.version >= 17 && im.isUnbreakable()) {
            getKitConfig().set(path + "unbreakable", im.isUnbreakable());
        }

        if (im.hasEnchants()) {
            Map<Enchantment, Integer> enchants = is.getEnchantments();
            Collection<String> enchantList = new ArrayList<>();
            for (Enchantment e : enchants.keySet()) {
                int level = enchants.get(e);
                enchantList.add(e.getKey() + " = " + level);
            }
            getKitConfig().set(path + "enchants", enchantList);
        }

        if (im instanceof AxolotlBucketMeta) {
            AxolotlBucketMeta axolotlMeta = (AxolotlBucketMeta) im;
            if (axolotlMeta.hasVariant()) {
                getKitConfig().set(path + "variant", axolotlMeta.getVariant().name());
            }
        }

        if (im instanceof BannerMeta) {
            BannerMeta bannerMeta = (BannerMeta) im;
            List<String> patterns = new ArrayList<>();
            for (int i = 0; i < bannerMeta.numberOfPatterns(); i++) {
                Pattern pattern = bannerMeta.getPatterns().get(i);
                patterns.add(pattern.getPattern().name() + ": " +  pattern.getColor().name());
            }
            getKitConfig().set(path + "patterns", patterns);
        }

        if (im instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) im;
            if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                ItemStack[] items = shulkerBox.getInventory().getContents();
                for (int i = 0; i < items.length; i++) {
                    if (items[i] != null) {
                        getType(path + "storage." + i + ".", items[i]);
                    }
                }
            }
        }

        if (im instanceof BookMeta) {
            BookMeta bookMeta = (BookMeta) im;
            if (bookMeta.hasAuthor()) {
                getKitConfig().set(path + "book.author", bookMeta.getAuthor());
            }
            if (bookMeta.hasGeneration()) {
                getKitConfig().set(path + "book.generation", bookMeta.getGeneration());
            }
            if (bookMeta.hasTitle()) {
                getKitConfig().set(path + "book.title", bookMeta.getTitle());
            }
            if (bookMeta.hasPages()) {
                List<String> pages = new ArrayList<>();
                for (int i = 0; i < bookMeta.getPageCount(); i++) {
                    pages.add(bookMeta.getPages().get(i));
                }
                getKitConfig().set(path + "book.pages", pages);
            }
        }

        if (im instanceof CompassMeta) {
            CompassMeta compassMeta = (CompassMeta) im;
            if (compassMeta.hasLodestone()) {
                saveLodestoneLocation(Objects.requireNonNull(compassMeta.getLodestone()), path + "lodestone.location.");
            }
            if (compassMeta.isLodestoneTracked()) {
                getKitConfig().set(path + "lodestone.tracking", compassMeta.isLodestoneTracked());
            }
        }

        if (im instanceof CrossbowMeta) {
            CrossbowMeta crossbowMeta = (CrossbowMeta) im;
            if (crossbowMeta.hasChargedProjectiles()) {
                List<ItemStack> projectiles = crossbowMeta.getChargedProjectiles();
                for (int i = 0; i < projectiles.size(); i++) {
                    getType(path + "projectiles." + i + ".", projectiles.get(i));
                }
            }
        }

        if (im instanceof Damageable) {
            Damageable damageable = (Damageable) im;
            if (damageable.hasDamage()) {
                getKitConfig().set(path + "damage", damageable.getDamage());
            }
        }

        if (im instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) im;
            List<String> enchantList = new ArrayList<>();
            Map<Enchantment, Integer> storedEnchants = storageMeta.getStoredEnchants();
            for (Enchantment enchantment : storedEnchants.keySet()) {
                int level = storedEnchants.get(enchantment);
                enchantList.add(enchantment.getKey() + " = " + level);
            }
            getKitConfig().set(path + "storedEnchants", enchantList);
        }

        if (im instanceof FireworkMeta) {
            FireworkMeta fireworkMeta = (FireworkMeta) im;
            if (fireworkMeta.hasEffects()) {
                for (int i = 0; i < fireworkMeta.getEffectsSize(); i++) {
                    FireworkEffect fireworkEffect = fireworkMeta.getEffects().get(i);
                    getKitConfig().set(path + "firework." + i + ".type", fireworkEffect.getType().name());
                    getKitConfig().set(path + "firework." + i + ".flicker", fireworkEffect.hasFlicker());
                    getKitConfig().set(path + "firework." + i + ".trail", fireworkEffect.hasTrail());
                    List<String> colorList = new ArrayList<>();
                    List<Color> colors = fireworkEffect.getColors();
                    for (Color color : colors) {
                        colorList.add(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                    }
                    getKitConfig().set(path + "firework." + i + ".colors", colorList);
                    List<String> fadeColorList = new ArrayList<>();
                    List<Color> fadeColors = fireworkEffect.getFadeColors();
                    for (Color fadeColor : fadeColors) {
                        fadeColorList.add(fadeColor.getRed() + ", " + fadeColor.getGreen() + ", " + fadeColor.getBlue());
                    }
                    getKitConfig().set(path + "firework." + i + ".fadeColors", fadeColorList);
                }
            }
        }

        if (im instanceof KnowledgeBookMeta) {
            KnowledgeBookMeta knowledgeBookMeta = (KnowledgeBookMeta) im;
            if (knowledgeBookMeta.hasRecipes()) {
                List<String> recipeKeys = new ArrayList<>();
                for (NamespacedKey namespacedKey : knowledgeBookMeta.getRecipes()) {
                    String string = namespacedKey.toString();
                    recipeKeys.add(string);
                }
                getKitConfig().set(path + "recipes", recipeKeys);
            }
        }

        if (im instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) im;
            Color color = leatherArmorMeta.getColor();
            getKitConfig().set(path + "color", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
        }

        if (im instanceof MapMeta) {
            MapMeta mapMeta = (MapMeta) im;
            if (mapMeta.hasColor()) {
                Color color = mapMeta.getColor();
                if (color != null) {
                    getKitConfig().set(path + "map.color", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                }
            }
            if (mapMeta.isScaling()) {
                getKitConfig().set(path + "map.scaling", mapMeta.isScaling());
            }
            if (mapMeta.hasMapView() && mapMeta.getMapView() != null) {
                MapView mapView = mapMeta.getMapView();
                getKitConfig().set(path + "map.view.id", mapView.getId());
                getKitConfig().set(path + "map.view.scale", mapView.getScale().name());
                getKitConfig().set(path + "map.view.world", Objects.requireNonNull(mapView.getWorld()).getName());
                getKitConfig().set(path + "map.view.centerX", mapMeta.getMapView().getCenterX());
                getKitConfig().set(path + "map.view.centerZ", mapMeta.getMapView().getCenterZ());
                getKitConfig().set(path + "map.view.locked", mapView.isLocked());
                getKitConfig().set(path + "map.view.tracking", mapView.isTrackingPosition());
                getKitConfig().set(path + "map.view.unlimitedTracking", mapView.isUnlimitedTracking());
                getKitConfig().set(path + "map.view.virtual", mapView.isVirtual());
            }
        }

        if (Main.version <= 13 && im instanceof Potion) {
            Potion potion = (Potion) im;
            getKitConfig().set(path + "potion.type", potion.getType().name());
            getKitConfig().set(path + "potion.extended", potion.hasExtendedDuration());
            getKitConfig().set(path + "potion.level", potion.getLevel());
            getKitConfig().set(path + "potion.splash", potion.isSplash());
        }

        if (Main.version >= 14 && im instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) im;
            getKitConfig().set(path + "potion.type", potionMeta.getBasePotionData().getType().name());
            getKitConfig().set(path + "potion.extended", potionMeta.getBasePotionData().isExtended());
            getKitConfig().set(path + "potion.upgraded", potionMeta.getBasePotionData().isUpgraded());
            if (potionMeta.hasColor()) {
                Color color = potionMeta.getColor();
                if (color != null) {
                    getKitConfig().set(path + "potion.color", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                }
            }
            getKitConfig().set(path + "lore", potionMeta.getLore());
        }

        if (im instanceof Repairable) {
            Repairable repairable = (Repairable) im;
            if (repairable.hasRepairCost()) {
                getKitConfig().set(path + "repair", repairable.getRepairCost());
            }
        }

        if (im instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) im;
            if (skullMeta.hasOwner()) {
                getKitConfig().set(path + ".skull.owner", Objects.requireNonNull(skullMeta.getOwningPlayer()).getUniqueId().toString());
            }
            if (version >= 32) {
                getKitConfig().set(path + ".skull.sound", skullMeta.getNoteBlockSound());
            }
        }

        if (im instanceof SuspiciousStewMeta) {
            SuspiciousStewMeta stewMeta = (SuspiciousStewMeta) im;
            if (stewMeta.hasCustomEffects()) {
                List<PotionEffect> potionEffects = stewMeta.getCustomEffects();
                for (PotionEffect potionEffect : potionEffects) {
                    getKitConfig().set(path + ".stew.type", potionEffect.getType().getName());
                    getKitConfig().set(path + ".stew.duration", potionEffect.getDuration());
                    getKitConfig().set(path + ".stew.amplifier", potionEffect.getAmplifier());
                    if (potionEffect.isAmbient()) {
                        getKitConfig().set(path + ".stew.ambient", potionEffect.isAmbient());
                    }
                    if (potionEffect.hasParticles()) {
                        getKitConfig().set(path + ".stew.particles", potionEffect.hasParticles());
                    }
                    if (potionEffect.hasIcon()) {
                        getKitConfig().set(path + ".stew.icon", potionEffect.hasIcon());
                    }
                }
            }
        }

        if (im instanceof TropicalFishBucketMeta) {
            TropicalFishBucketMeta tropicalFishMeta = (TropicalFishBucketMeta) im;
            if (tropicalFishMeta.hasVariant()) {
                getKitConfig().set(path + ".fish.bodyColor", tropicalFishMeta.getBodyColor().name());
                getKitConfig().set(path + ".fish.pattern", tropicalFishMeta.getPattern().name());
                getKitConfig().set(path + ".fish.patternColor", tropicalFishMeta.getPatternColor().name());
            }
        }
    }
}
