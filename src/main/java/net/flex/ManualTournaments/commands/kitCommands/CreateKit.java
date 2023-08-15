package net.flex.ManualTournaments.commands.kitCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.KitCommand;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapView;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class CreateKit implements KitCommand {
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

        IntStream.range(0, 36).forEachOrdered(i -> {
            ItemStack is = inventory.getItem(i);
            if (is != null) {
                String itemPath = path + i + ".";
                getType(itemPath, is);
            }
        });

        Arrays.stream(inventory.getArmorContents()).filter(Objects::nonNull).forEachOrdered(armor -> {
            String armorPath = path + "armor." + armor.getType().name();
            getKitConfig().set(armorPath, inventory.getArmorContents());
            getType(armorPath + ".", armor);
        });

        if (Main.version >= 15) {
            ItemStack offhand = inventory.getItemInOffHand();
            if (!offhand.getType().equals(Material.AIR)) {
                String offhandPath = path + "offhand." + offhand.getType().name();
                getKitConfig().set(offhandPath, offhand);
                getType(offhandPath + ".", offhand);
            }
        }

        player.getActivePotionEffects().forEach(effect -> {
            String effectPath = path + "effects." + effect.getType().getName() + ".";
            getKitConfig().set(effectPath + "duration", effect.getDuration());
            getKitConfig().set(effectPath + "amplifier", effect.getAmplifier());
            getKitConfig().set(effectPath + "ambient", effect.isAmbient());
            getKitConfig().set(effectPath + "particles", effect.hasParticles());
            if (Main.version > 19) {
                getKitConfig().set(effectPath + "icon", effect.hasIcon());
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
            Set<String> set = im.getItemFlags().stream().map(Enum::name).collect(Collectors.toSet());
            config.set(path + "flags", set);
        }

        if (Main.version >= 21 && im.hasCustomModelData()) {
            config.set(path + "modelData", im.getCustomModelData());
        }

        if (Main.version >= 17 && im.isUnbreakable()) {
            getKitConfig().set(path + "unbreakable", im.isUnbreakable());
        }

        if (im.hasEnchants()) {
            Map<Enchantment, Integer> enchants = is.getEnchantments();
            Collection<String> enchantList = new ArrayList<>();
            enchants.keySet().forEach(e -> {
                int level = enchants.get(e);
                if (version <= 18) {
                    enchantList.add(e.getName() + " = " + level);
                } else {
                    enchantList.add(e.getKey() + " = " + level);
                }
            });
            getKitConfig().set(path + "enchants", enchantList);
        }

        if (Main.version >= 26 && im instanceof AxolotlBucketMeta) {
            AxolotlBucketMeta axolotlMeta = (AxolotlBucketMeta) im;
            if (axolotlMeta.hasVariant()) {
                getKitConfig().set(path + "variant", axolotlMeta.getVariant().name());
            }
        }

        if (im instanceof BannerMeta) {
            BannerMeta bannerMeta = (BannerMeta) im;
            List<String> patterns = IntStream.range(0, bannerMeta.numberOfPatterns()).mapToObj(i ->
                    bannerMeta.getPatterns().get(i)).map(pattern -> pattern.getPattern().name() + ": " + pattern.getColor().name()).collect(Collectors.toList());
            getKitConfig().set(path + "patterns", patterns);
        }

        if (Main.version >= 17 && im instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) im;
            if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();
                ItemStack[] items = shulkerBox.getInventory().getContents();
                IntStream.range(0, items.length).filter(i -> items[i] != null).forEachOrdered(i -> getType(path + "storage." + i + ".", items[i]));
            }
        }

        if (im instanceof BookMeta) {
            BookMeta bookMeta = (BookMeta) im;
            if (bookMeta.hasAuthor()) {
                getKitConfig().set(path + "book.author", bookMeta.getAuthor());
            }
            if (Main.version >= 16 && bookMeta.hasGeneration()) {
                getKitConfig().set(path + "book.generation", bookMeta.getGeneration());
            }
            if (bookMeta.hasTitle()) {
                getKitConfig().set(path + "book.title", bookMeta.getTitle());
            }
            if (bookMeta.hasPages()) {
                List<String> pages = IntStream.range(0, bookMeta.getPageCount()).mapToObj(i -> bookMeta.getPages().get(i)).collect(Collectors.toList());
                getKitConfig().set(path + "book.pages", pages);
            }
        }

        if (Main.version >= 23 && im instanceof CompassMeta) {
            CompassMeta compassMeta = (CompassMeta) im;
            if (compassMeta.hasLodestone()) {
                saveLodestoneLocation(Objects.requireNonNull(compassMeta.getLodestone()), path + "lodestone.location.");
            }
            getKitConfig().set(path + "lodestone.tracking", compassMeta.isLodestoneTracked());
        }

        if (Main.version >= 19 && im instanceof CrossbowMeta) {
            CrossbowMeta crossbowMeta = (CrossbowMeta) im;
            if (crossbowMeta.hasChargedProjectiles()) {
                List<ItemStack> projectiles = crossbowMeta.getChargedProjectiles();
                IntStream.range(0, projectiles.size()).forEachOrdered(i -> getType(path + "projectiles." + i + ".", projectiles.get(i)));
            }
        }

        if (Main.version >= 19 && im instanceof Damageable) {
            Damageable damageable = (Damageable) im;
            if (damageable.hasDamage()) {
                getKitConfig().set(path + "damage", damageable.getDamage());
            }
        }

        if (im instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) im;
            List<String> enchantList = new ArrayList<>();
            Map<Enchantment, Integer> storedEnchants = storageMeta.getStoredEnchants();
            storedEnchants.keySet().forEach(enchantment -> {
                int level = storedEnchants.get(enchantment);
                if (Main.version >= 19) {
                    enchantList.add(enchantment.getKey() + " = " + level);
                } else {
                    enchantList.add(enchantment.getName() + " = " + level);
                }
            });
            getKitConfig().set(path + "storedEnchants", enchantList);
        }

        if (im instanceof FireworkMeta) {
            FireworkMeta fireworkMeta = (FireworkMeta) im;
            if (fireworkMeta.hasEffects()) {
                IntStream.range(0, fireworkMeta.getEffectsSize()).forEachOrdered(i -> {
                    FireworkEffect fireworkEffect = fireworkMeta.getEffects().get(i);
                    getKitConfig().set(path + "firework." + i + ".type", fireworkEffect.getType().name());
                    getKitConfig().set(path + "firework." + i + ".duration", fireworkMeta.getPower());
                    getKitConfig().set(path + "firework." + i + ".flicker", fireworkEffect.hasFlicker());
                    getKitConfig().set(path + "firework." + i + ".trail", fireworkEffect.hasTrail());
                    List<Color> colors = fireworkEffect.getColors();
                    List<Integer> colorList = colors.stream().map(Color::asRGB).collect(Collectors.toList());
                    getKitConfig().set(path + "firework." + i + ".colors", colorList);
                    List<Color> fadeColors = fireworkEffect.getFadeColors();
                    List<Integer> fadeColorList = fadeColors.stream().map(Color::asRGB).collect(Collectors.toList());
                    getKitConfig().set(path + "firework." + i + ".fadeColors", fadeColorList);
                });
            }
        }

        if (Main.version >= 18 && im instanceof KnowledgeBookMeta) {
            KnowledgeBookMeta knowledgeBookMeta = (KnowledgeBookMeta) im;
            if (knowledgeBookMeta.hasRecipes()) {
                List<String> recipeKeys = knowledgeBookMeta.getRecipes().stream().map(NamespacedKey::toString).collect(Collectors.toList());
                getKitConfig().set(path + "recipes", recipeKeys);
            }
        }

        if (im instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) im;
            int color = leatherArmorMeta.getColor().asRGB();
            getKitConfig().set(path + "color", color);
        }

        if (im instanceof MapMeta) {
            MapMeta mapMeta = (MapMeta) im;
            Color color = mapMeta.getColor();
            if (Main.version >= 17 && color != null) {
                getKitConfig().set(path + "map.color", color.asRGB());
            }
            getKitConfig().set(path + "map.scaling", mapMeta.isScaling());
            if (Main.version >= 21 && mapMeta.getMapView() != null) {
                MapView mapView = mapMeta.getMapView();
                getKitConfig().set(path + "map.view.id", mapView.getId());
                getKitConfig().set(path + "map.view.scale", mapView.getScale().name());
                getKitConfig().set(path + "map.view.world", Objects.requireNonNull(mapView.getWorld()).getName());
                getKitConfig().set(path + "map.view.centerX", mapMeta.getMapView().getCenterX());
                getKitConfig().set(path + "map.view.centerZ", mapMeta.getMapView().getCenterZ());
                getKitConfig().set(path + "map.view.locked", mapView.isLocked());
                if (Main.version >= 22) {
                    getKitConfig().set(path + "map.view.tracking", mapView.isTrackingPosition());
                }
                getKitConfig().set(path + "map.view.unlimitedTracking", mapView.isUnlimitedTracking());
            }
        }

        if (im instanceof PotionMeta) {
            if (Main.version >= 14) {
                PotionMeta potionMeta = (PotionMeta) im;
                getKitConfig().set(path + "potion.type", potionMeta.getBasePotionData().getType().name());
                getKitConfig().set(path + "potion.extended", potionMeta.getBasePotionData().isExtended());
                getKitConfig().set(path + "potion.upgraded", potionMeta.getBasePotionData().isUpgraded());
                if (Main.version >= 17) {
                    Color color = potionMeta.getColor();
                    if (potionMeta.hasColor() && color != null) {
                        getKitConfig().set(path + "potion.color", color.asRGB());
                    }
                }
            } else {
                Potion potion = Potion.fromItemStack(is);
                getKitConfig().set(path + "potion.type", potion.getType().name());
                getKitConfig().set(path + "potion.extended", potion.hasExtendedDuration());
                getKitConfig().set(path + "potion.level", potion.getLevel());
                getKitConfig().set(path + "potion.splash", potion.isSplash());
            }
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
                if (Main.version <= 18) {
                    getKitConfig().set(path + "skull.owner", skullMeta.getOwner());
                } else {
                    getKitConfig().set(path + "skull.owner", Objects.requireNonNull(Objects.requireNonNull(skullMeta.getOwningPlayer()).getUniqueId()).toString());
                }
            }
            if (version >= 32) {
                getKitConfig().set(path + "skull.sound", skullMeta.getNoteBlockSound());
            }
        }

        if (Main.version > 22 && im instanceof SuspiciousStewMeta) {
            SuspiciousStewMeta stewMeta = (SuspiciousStewMeta) im;
            if (stewMeta.hasCustomEffects()) {
                List<PotionEffect> potionEffects = stewMeta.getCustomEffects();
                for (int i = 0; i < potionEffects.size(); i++) {
                    PotionEffect potionEffect = potionEffects.get(i);
                    String stewPath = path + "stew." + i + ".";
                    getKitConfig().set(stewPath + "type", potionEffect.getType().getName());
                    getKitConfig().set(stewPath + "duration", potionEffect.getDuration());
                    getKitConfig().set(stewPath + "amplifier", potionEffect.getAmplifier());
                    getKitConfig().set(stewPath + "ambient", potionEffect.isAmbient());
                    getKitConfig().set(stewPath + "particles", potionEffect.hasParticles());
                    getKitConfig().set(stewPath + "icon", potionEffect.hasIcon());
                }
            }
        }

        if (Main.version >= 19 && im instanceof TropicalFishBucketMeta) {
            TropicalFishBucketMeta tropicalFishMeta = (TropicalFishBucketMeta) im;
            if (tropicalFishMeta.hasVariant()) {
                getKitConfig().set(path + "fish.bodyColor", tropicalFishMeta.getBodyColor().name());
                getKitConfig().set(path + "fish.pattern", tropicalFishMeta.getPattern().name());
                getKitConfig().set(path + "fish.patternColor", tropicalFishMeta.getPatternColor().name());
            }
        }
    }
}
