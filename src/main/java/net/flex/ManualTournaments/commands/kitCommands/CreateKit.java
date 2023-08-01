package net.flex.ManualTournaments.commands.kitCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.KitCommand;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.message;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class CreateKit implements KitCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String kitName, boolean kitExists) {
        if (!kitExists) {
            if (Objects.equals(getPlugin().getConfig().getString("current-kit"), ""))  {
                getPlugin().getConfig().set("current-kit", kitName);
                getPlugin().getConfig().save(getCustomConfigFile());
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
            if (is == null) continue;
            String itemPath = path + "items." + i;
            getType(itemPath, is);
        }
        for (ItemStack armor : inventory.getArmorContents()) {
            if (armor != null) {
                String armorPath = path + "armor." + armor.getType().toString().toUpperCase();
                getKitConfig().set(armorPath, inventory.getArmorContents());
                getType(armorPath, armor);
            }
        }
        if (Main.version >= 15) {
            ItemStack offhand = inventory.getItemInOffHand();
            if (!offhand.getType().equals(Material.AIR)) {
                String offhandPath = path + "offhand." + offhand.getType().toString().toUpperCase();
                getKitConfig().set(offhandPath, offhand);
                getType(offhandPath, offhand);
            }
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            String effectPath = path + "effects." + effect.getType().getName().toUpperCase();
            getKitConfig().set(effectPath + ".amplifier", effect.getAmplifier());
            getKitConfig().set(effectPath + ".duration", effect.getDuration());
        }
        getKitConfig().save(getKitConfigFile());
    }

    private void getType(String path, ItemStack is) {
        getKitConfig().set(path + ".type", is.getType().toString().toUpperCase());
        getKitConfig().set(path + ".amount", is.getAmount());
        if (Main.version <= 18) getKitConfig().set(path + ".durability", is.getDurability());
        if (is.getItemMeta() != null) {
            if (is.getItemMeta().hasDisplayName())
                getKitConfig().set(path + ".name", is.getItemMeta().getDisplayName());
            if (Main.version >= 17) {
                if (is.getItemMeta().isUnbreakable()) {
                    getKitConfig().set(path + ".unbreakable", is.getItemMeta().isUnbreakable());
                }
            }
            if (is.getType().equals(Material.ENCHANTED_BOOK)) {
                EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) is.getItemMeta();
                Map<Enchantment, Integer> enchants = storageMeta.getStoredEnchants();
                Collection<String> enchantList = new ArrayList<>();
                for (Enchantment e : storageMeta.getStoredEnchants().keySet()) {
                    int level = enchants.get(e);
                    enchantList.add(e.getKey() + " = " + level);
                }
                getKitConfig().set(path + ".enchants", enchantList);
                if (storageMeta.hasLore()) getKitConfig().set(path + ".lore", storageMeta.getLore());
                if (storageMeta.hasDisplayName()) getKitConfig().set(path + ".name", storageMeta.getDisplayName());
                else getKitConfig().set(path + ".name", "&e" + "Enchanted Book");
            } else if (Main.version >= 14 && is.getItemMeta() instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta) is.getItemMeta();
                getKitConfig().set(path + ".potion.type", potionMeta.getBasePotionData().getType().name());
                getKitConfig().set(path + ".potion.extended", potionMeta.getBasePotionData().isExtended());
                getKitConfig().set(path + ".potion.upgraded", potionMeta.getBasePotionData().isUpgraded());
                getKitConfig().set(path + ".lore", potionMeta.getLore());
            } else if (Main.version <= 13 && is.getItemMeta() instanceof Potion) {
                Potion potion = (Potion) is.getItemMeta();
                getKitConfig().set(path + ".potion.type", potion.getType().name());
                getKitConfig().set(path + ".potion.extended", potion.hasExtendedDuration());
                getKitConfig().set(path + ".potion.level", potion.getLevel());
                getKitConfig().set(path + ".potion.splash", potion.isSplash());
                getKitConfig().set(path + ".lore", is.getItemMeta().getLore());
            } else if (is.getItemMeta().hasEnchants()) {
                Map<Enchantment, Integer> enchants = is.getEnchantments();
                Collection<String> enchantList = new ArrayList<>();
                for (Enchantment e : is.getEnchantments().keySet()) {
                    int level = enchants.get(e);
                    enchantList.add(e.getKey() + " = " + level);
                }
                getKitConfig().set(path + ".enchants", enchantList);
            }
            if (is.getItemMeta().hasLore()) getKitConfig().set(path + ".lore", is.getItemMeta().getLore());
        }
    }
}
