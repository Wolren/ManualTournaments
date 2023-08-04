package net.flex.ManualTournaments.commands.kitCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.KitCommand;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;

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
            if (Objects.requireNonNull(config.getString("current-arena")).isEmpty())  {
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
                String itemPath = path + "items." + i;
                getType(itemPath, is);
            }
        });

        Arrays.stream(inventory.getArmorContents()).filter(Objects::nonNull).forEach(armor -> {
            String armorPath = path + "armor." + armor.getType().toString().toUpperCase();
            getKitConfig().set(armorPath, inventory.getArmorContents());
            getType(armorPath, armor);
        });

        if (Main.version >= 15) {
            ItemStack offhand = inventory.getItemInOffHand();
            if (!offhand.getType().equals(Material.AIR)) {
                String offhandPath = path + "offhand." + offhand.getType().toString().toUpperCase();
                getKitConfig().set(offhandPath, offhand);
                getType(offhandPath, offhand);
            }
        }

        player.getActivePotionEffects().forEach(effect -> {
            String effectPath = path + "effects." + effect.getType().getName().toUpperCase();
            getKitConfig().set(effectPath + ".amplifier", effect.getAmplifier());
            getKitConfig().set(effectPath + ".duration", effect.getDuration());
        });

        getKitConfig().save(getKitConfigFile());
    }

    private void getType(String path, ItemStack is) {
        ItemMeta im = is.getItemMeta();

        getKitConfig().set(path + ".type", is.getType().toString().toUpperCase());

        getKitConfig().set(path + ".amount", is.getAmount());

        if (Main.version <= 18) {
            getKitConfig().set(path + ".durability", is.getDurability());
        }

        if (im == null) return;

        if (im.hasDisplayName()) {
            getKitConfig().set(path + ".name", im.getDisplayName());
        }

        if (im.hasLore()) {
            getKitConfig().set(path + ".lore", im.getLore());
        }

        if (!im.getItemFlags().isEmpty()) {
            config.set(path + ".itemFlags", im.getItemFlags().stream().map(ItemFlag::name).collect(Collectors.toSet()));
        }

        if (Main.version >= 17) {
            if (im.isUnbreakable()) {
                getKitConfig().set(path + ".unbreakable", im.isUnbreakable());
            }
        }

        if (im.hasCustomModelData()) {
            config.set(path + ".customModelData", im.getCustomModelData());
        }

        if (im.hasEnchants()) {
            Map<Enchantment, Integer> enchants = is.getEnchantments();
            Collection<String> enchantList = new ArrayList<>();
            enchants.keySet().forEach(e -> {
                int level = enchants.get(e);
                enchantList.add(e.getKey() + " = " + level);
            });
            getKitConfig().set(path + ".enchants", enchantList);
        }

        if (is.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) im;
            Map<Enchantment, Integer> enchants = storageMeta.getStoredEnchants();
            Collection<String> enchantList = new ArrayList<>();
            enchants.keySet().forEach(e -> {
                int level = enchants.get(e);
                enchantList.add(e.getKey() + " = " + level);
            });
            getKitConfig().set(path + ".enchants", enchantList);
            if (storageMeta.hasLore()) getKitConfig().set(path + ".lore", storageMeta.getLore());
            if (storageMeta.hasDisplayName()) getKitConfig().set(path + ".name", storageMeta.getDisplayName());
            else getKitConfig().set(path + ".name", "&e" + "Enchanted Book");
        }

        if (Main.version >= 14 && im instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) im;
            getKitConfig().set(path + ".potion.type", potionMeta.getBasePotionData().getType().name());
            getKitConfig().set(path + ".potion.extended", potionMeta.getBasePotionData().isExtended());
            getKitConfig().set(path + ".potion.upgraded", potionMeta.getBasePotionData().isUpgraded());
            getKitConfig().set(path + ".lore", potionMeta.getLore());
        }

        if (Main.version <= 13 && im instanceof Potion) {
            Potion potion = (Potion) im;
            getKitConfig().set(path + ".potion.type", potion.getType().name());
            getKitConfig().set(path + ".potion.extended", potion.hasExtendedDuration());
            getKitConfig().set(path + ".potion.level", potion.getLevel());
            getKitConfig().set(path + ".potion.splash", potion.isSplash());
            getKitConfig().set(path + ".lore", im.getLore());
        }

        if (im instanceof SkullMeta) {
            SkullMeta skullMeta = ((SkullMeta) im);
            if (skullMeta.hasOwner()) {
                getKitConfig().set(path + ".owner", Objects.requireNonNull(skullMeta.getOwningPlayer()).getUniqueId().toString());
            }
        }

        if (im instanceof FireworkMeta) {
            getKitConfig().set(path + ".fireworkEffect", ((FireworkMeta) im).getEffects());
        }

        if (im instanceof MapMeta) {
            getKitConfig().set(path + ".isScaling", ((MapMeta) im).isScaling());
        }
    }
}
