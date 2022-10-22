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
    private static final FileConfiguration config = Main.getPlugin().getConfig();
    private static final FileConfiguration KitsConfig = Main.getPlugin().getKitsConfig();

    @SneakyThrows
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String s, @NotNull final String[] args) {
        config.load(Main.getPlugin().customConfigFile);
        if (!(sender instanceof Player)) sender.sendMessage(Main.conf("sender-not-a-player"));
        else {
            final Player p = ((OfflinePlayer) sender).getPlayer();
            assert p != null;
            KitsConfig.load(Main.getPlugin().KitsConfigfile);
            if (args.length == 0) return false;
            else if (args.length == 1) {
                final String a = args[0];
                if (a.equals("list")) {
                    p.sendMessage(Main.conf("kit-list") + Main.getPlugin().kitNames.toString());
                } else if (a.equals("unbreakable")) {
                    if (Main.version > 4) {
                        final ItemStack[] inv = p.getInventory().getContents();
                        for (final ItemStack im : inv) {
                            if (im != null) {
                                if (im.getType().getMaxDurability() != 0) {
                                    final ItemMeta unb = im.getItemMeta();
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
                final String a = args[0];
                final String kitName = args[1];
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
            Main.getPlugin().getKitsConfig().save(Main.getPlugin().KitsConfigfile);
        }
        return true;
    }

    @SneakyThrows
    private void createKit(final Player p, final String kitName) {
        final String path = "Kits." + kitName + ".";
        final PlayerInventory inv = p.getInventory();
        final Collection<PotionEffect> effects = p.getActivePotionEffects();
        for (int i = 0; i < 36; i++) {
            final ItemStack is = inv.getItem(i);
            if (is == null) continue;
            final String itemPath = path + "items." + i;
            getType(itemPath, is);
        }
        for (final ItemStack armor : inv.getArmorContents()) {
            if (armor != null) {
                final String armorPath = path + "armor." + armor.getType().toString().toUpperCase();
                KitsConfig.set(armorPath, inv.getArmorContents());
                getType(armorPath, armor);
            }
        }
        if (Main.version > 8) {
            final ItemStack offhand = inv.getItemInOffHand();
            if (!offhand.getType().equals(Material.AIR)) {
                final String offhandPath = path + "offhand." + offhand.getType().toString().toUpperCase();
                KitsConfig.set(offhandPath, offhand);
                getType(offhandPath, offhand);
            }
        }
        for (final PotionEffect effect : effects) {
            final String effectPath = path + "effects." + effect.getType().getName().toUpperCase();
            KitsConfig.set(effectPath + ".amplifier", effect.getAmplifier());
            KitsConfig.set(effectPath + ".duration", effect.getDuration());
        }
        Main.getPlugin().kitNames.add(kitName);
        Main.getPlugin().getKitsConfig().save(Main.getPlugin().KitsConfigfile);
    }

    private void getType(final String path, final ItemStack is) {
        KitsConfig.set(path + ".type", is.getType().toString().toUpperCase());
        KitsConfig.set(path + ".amount", is.getAmount());
        if (Main.version < 13) {
            KitsConfig.set(path + ".durability", is.getDurability());
        }
        if (is.hasItemMeta()) {
            if (Objects.requireNonNull(is.getItemMeta()).hasDisplayName())
                KitsConfig.set(path + ".name", is.getItemMeta().getDisplayName());

            if (is.getType().equals(Material.ENCHANTED_BOOK)) {
                final EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) is.getItemMeta();
                final Map<Enchantment, Integer> enchants = storageMeta.getStoredEnchants();
                final Collection<String> enchantList = new ArrayList<>();
                for (final Enchantment e : storageMeta.getStoredEnchants().keySet()) {
                    final int level = enchants.get(e);
                    enchantList.add(e.getName().toUpperCase() + ":" + level);
                }
                KitsConfig.set(path + ".enchants", enchantList);
                if (storageMeta.hasLore()) {
                    KitsConfig.set(path + ".lore", storageMeta.getLore());
                }
                if (storageMeta.hasDisplayName()) {
                    KitsConfig.set(path + ".name", storageMeta.getDisplayName());
                } else {
                    KitsConfig.set(path + ".name", "&e" + "Enchanted Book");
                }
            } else if (is.getItemMeta() instanceof PotionMeta && Main.version > 7) {
                final PotionMeta potionMeta = (PotionMeta) is.getItemMeta();
                KitsConfig.set(path + ".potion.type", Objects.requireNonNull(potionMeta.getBasePotionData().getType().name()));
                KitsConfig.set(path + ".potion.extended", potionMeta.getBasePotionData().isExtended());
                KitsConfig.set(path + ".potion.upgraded", potionMeta.getBasePotionData().isUpgraded());
                KitsConfig.set(path + ".lore", potionMeta.getLore());
            } else if (is.getItemMeta().hasEnchants()) {
                final Map<Enchantment, Integer> enchants = is.getEnchantments();
                final Collection<String> enchantList = new ArrayList<>();
                for (final Enchantment e : is.getEnchantments().keySet()) {
                    final int level = enchants.get(e);
                    enchantList.add(e.getName().toUpperCase() + ":" + level);
                }
                KitsConfig.set(path + ".enchants", enchantList);
            }

            if (is.getItemMeta().hasLore()) {
                KitsConfig.set(path + ".lore", is.getItemMeta().getLore());
            }
        }
    }

    static void giveKit(final Player p, final String kitName) {
        final String path = "Kits." + kitName + ".";
        p.getInventory().clear();
        p.setHealth(20.0D);
        p.setFoodLevel(20);
        p.setFireTicks(0);
        for (final PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
        final ConfigurationSection csA = KitsConfig.getConfigurationSection("Kits." + kitName + "." + "items");
        final ConfigurationSection csB = KitsConfig.getConfigurationSection("Kits." + kitName + "." + "armor");
        final ConfigurationSection csC = KitsConfig.getConfigurationSection("Kits." + kitName + "." + "offhand");
        final ConfigurationSection csD = KitsConfig.getConfigurationSection("Kits." + kitName + "." + "effects");
        setPlayerContents(p, path, csA, csB, csC);
        setPlayerEffects(p, path, csD);
        p.updateInventory();
    }

    public static void setPlayerContents(final Player player, final String path, final ConfigurationSection csA, final ConfigurationSection csB, final ConfigurationSection csC) {
        if (csA != null) {
            for (final String str : Objects.requireNonNull(csA).getKeys(false)) {
                final int slot = Integer.parseInt(str);
                final String itemPath = path + "items." + slot + ".";
                final String type = KitsConfig.getString(itemPath + "type");
                assert type != null;
                final String name = KitsConfig.getString(itemPath + "name");
                final List<String> enchants = KitsConfig.getStringList(itemPath + "enchants");
                final List<String> lore = KitsConfig.getStringList(itemPath + "lore");
                final int amount = KitsConfig.getInt(itemPath + "amount");
                final ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(type)), amount);
                short durability = 0;
                if (Main.version < 13) durability = (short) KitsConfig.getInt(itemPath + "durability");
                if (KitsConfig.getString(itemPath + "potion") != null && Main.version > 7) {
                    final PotionType potionType = PotionType.valueOf(KitsConfig.getString(itemPath + "potion.type"));
                    final boolean extended = KitsConfig.getBoolean(itemPath + "potion.extended");
                    final boolean upgraded = KitsConfig.getBoolean(itemPath + "potion.upgraded");
                    final PotionMeta potionMeta = (PotionMeta) is.getItemMeta();
                    assert potionMeta != null;
                    potionMeta.setBasePotionData(new PotionData(potionType, extended, upgraded));
                    is.setItemMeta(potionMeta);
                    player.getInventory().setItem(slot, is);
                } else if (is.getType().equals(Material.ENCHANTED_BOOK)) {
                    for (final String s : enchants) {
                        final String[] indiEnchants = s.split(":");
                        final EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) is.getItemMeta();
                        assert storageMeta != null;
                        storageMeta.addStoredEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                        is.setItemMeta(storageMeta);
                        player.getInventory().setItem(slot, is);
                    }
                } else {
                    final ItemMeta im = is.getItemMeta();
                    if (Main.version < 13) is.setDurability(durability);
                    if (im == null) continue;
                    if (name != null) im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                    im.setLore(lore);
                    for (final String s : enchants) {
                        final String[] indiEnchants = s.split(":");
                        im.addEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                    }
                    is.setItemMeta(im);
                    player.getInventory().setItem(slot, is);
                }
            }
        }
        if (csB != null) {
            for (final String str : Objects.requireNonNull(csB).getKeys(false)) {
                final String pB = path + "armor." + str + ".";
                final String typeB = KitsConfig.getString(pB + "type");
                final String nameB = KitsConfig.getString(pB + "name");
                short durability = 0;
                if (Main.version < 13) {
                    durability = (short) KitsConfig.getInt(pB + "durability");
                }
                final List<String> enchants = KitsConfig.getStringList(pB + "enchants");
                final int amount = KitsConfig.getInt(pB + "amount");
                assert typeB != null;
                final ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(typeB)), amount);
                final ItemMeta im = is.getItemMeta();
                if (Main.version < 13) {
                    is.setDurability(durability);
                }
                if (im == null) continue;
                if (nameB != null) im.setDisplayName(ChatColor.translateAlternateColorCodes('&', nameB));
                for (final String s1 : enchants) {
                    final String[] indiEnchants = s1.split(":");
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
            final List<String> strs = new ArrayList<>(Objects.requireNonNull(csC).getKeys(false));
            final String pC = path + "offhand." + strs.get(0) + ".";
            final String typeC = KitsConfig.getString(pC + "type");
            final String nameC = KitsConfig.getString(pC + "name");
            short durability = 0;
            if (Main.version < 13) {
                durability = (short) KitsConfig.getInt(pC + "durability");
            }
            final List<String> enchants = KitsConfig.getStringList(pC + "enchants");
            final int amount = KitsConfig.getInt(pC + "amount");
            assert typeC != null;
            final ItemStack is = new ItemStack(Objects.requireNonNull(Material.matchMaterial(typeC)), amount);
            if (KitsConfig.getString(pC + "potion") != null && Main.version > 7) {
                final PotionType type = PotionType.valueOf(KitsConfig.getString(pC + "potion.type"));
                final boolean extended = KitsConfig.getBoolean(pC + "potion.extended");
                final boolean upgraded = KitsConfig.getBoolean(pC + "potion.upgraded");
                final PotionMeta potionMeta = (PotionMeta) is.getItemMeta();
                assert potionMeta != null;
                potionMeta.setBasePotionData(new PotionData(type, extended, upgraded));
                is.setItemMeta(potionMeta);
                player.getInventory().setItemInOffHand(is);
            } else if (is.getType().equals(Material.ENCHANTED_BOOK)) {
                for (final String s : enchants) {
                    final String[] indiEnchants = s.split(":");
                    final EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) is.getItemMeta();
                    assert storageMeta != null;
                    storageMeta.addStoredEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                    is.setItemMeta(storageMeta);
                    player.getInventory().setItemInOffHand(is);
                }
            } else {
                final ItemMeta im = is.getItemMeta();
                if (Main.version < 13) {
                    is.setDurability(durability);
                }
                if (im != null) {
                    if (nameC != null) im.setDisplayName(ChatColor.translateAlternateColorCodes('&', nameC));
                    for (final String s1 : enchants) {
                        final String[] indiEnchants = s1.split(":");
                        im.addEnchant(Objects.requireNonNull(Enchantment.getByName(indiEnchants[0])), Integer.parseInt(indiEnchants[1]), true);
                    }
                }
                is.setItemMeta(im);
                player.getInventory().setItemInOffHand(is);
            }
        }
    }

    public static void setPlayerEffects(final Player p, final String path, final ConfigurationSection csD) {
        if (csD != null) {
            final Iterable<String> effects = new ArrayList<>(Objects.requireNonNull(csD).getKeys(false));
            for (final String s : effects) {
                final PotionEffectType type = PotionEffectType.getByName(s);
                final int amplifier = KitsConfig.getInt(path + "effects." + s.toUpperCase() + ".amplifier");
                final int duration = KitsConfig.getInt(path + "effects." + s.toUpperCase() + ".duration");
                assert type != null;
                final PotionEffect effect = new PotionEffect(type, duration, amplifier);
                p.addPotionEffect(effect);
            }
        }
    }

    private static void send(final Player p, final String s) {
        p.sendMessage(Main.conf(s));
    }

    @Nullable
    public List<String> onTabComplete(@NotNull final CommandSender commandSender, @NotNull final org.bukkit.command.Command command, @NotNull final String s, @NotNull final String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(Arrays.asList("create", "give", "list", "remove", "unbreakable"));
        } else if (args.length == 2) {
            final List<String> arr = new ArrayList<>();
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
