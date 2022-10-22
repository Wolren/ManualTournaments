package net.flex.ManualTournaments;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin {
    List<String> kitNames, arenaNames;
    File KitsConfigfile, ArenaConfigFile, customConfigFile;
    FileConfiguration KitsConfig, ArenaConfig, customConfig;
    public static int version = Main.formatNMSVersion(Main.getNMSVersion());

    static Main getPlugin() {
        return getPlugin(Main.class);
    }

    FileConfiguration getKitsConfig() {
        return KitsConfig;
    }

    FileConfiguration getArenaConfig() {
        return ArenaConfig;
    }

    public void onEnable() {
        kitNames = new ArrayList<>();
        arenaNames = new ArrayList<>();
        createKitsConfig();
        createArenaConfig();
        createCustomConfig();
        getConfig().options().copyDefaults(true);
        final FileConfiguration Kits = YamlConfiguration.loadConfiguration(KitsConfigfile);
        final FileConfiguration Arenas = YamlConfiguration.loadConfiguration(ArenaConfigFile);
        if (Kits.getConfigurationSection("Kits") != null) {
            kitNames.addAll(Objects.requireNonNull(Kits.getConfigurationSection("Kits")).getKeys(false));
        }
        if (Arenas.getConfigurationSection("Arenas") != null) {
            arenaNames.addAll(Objects.requireNonNull(Arenas.getConfigurationSection("Arenas")).getKeys(false));
        }
        Objects.requireNonNull(getCommand("manualtournaments_arena")).setExecutor(new Arena());
        Objects.requireNonNull(getCommand("manualtournaments_arena")).setTabCompleter(new Arena());
        Objects.requireNonNull(getCommand("manualtournaments_fight")).setExecutor(new Fight());
        Objects.requireNonNull(getCommand("manualtournaments_kit")).setExecutor(new Kit());
        Objects.requireNonNull(getCommand("manualtournaments_kit")).setTabCompleter(new Kit());
        Objects.requireNonNull(getCommand("manualtournaments_settings")).setExecutor(new Settings());
        Objects.requireNonNull(getCommand("manualtournaments_settings")).setTabCompleter(new Settings());
        Objects.requireNonNull(getCommand("manualtournaments_spectate")).setExecutor(new Spectate());
        Objects.requireNonNull(getCommand("manualtournaments_spectate")).setTabCompleter(new Spectate());
        getServer().getPluginManager().registerEvents(new MyListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJumpEvent.CallJumpEvent(), this);
    }

    public void onDisable() {
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "config.yml");
        customConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(customConfigFile);
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
    }

    private void createArenaConfig() {
        ArenaConfigFile = new File(getDataFolder(), "arenas.yml");
        ArenaConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(ArenaConfigFile);
        if (!ArenaConfigFile.exists()) {
            ArenaConfigFile.getParentFile().mkdirs();
            saveResource("arenas.yml", false);
        }
    }

    private void createKitsConfig() {
        KitsConfigfile = new File(getDataFolder(), "kits.yml");
        KitsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(KitsConfigfile);
        if (!KitsConfigfile.exists()) {
            KitsConfigfile.getParentFile().mkdirs();
            saveResource("kits.yml", false);
        }
    }

    static String getNMSVersion() {
        final String v = Bukkit.getServer().getClass().getPackage().getName();
        return v.substring(v.lastIndexOf('.') + 1);
    }

    static int formatNMSVersion(final String nms) {
        switch (nms) {
            case "v1_7_R1":
            case "v1_7_R2":
            case "v1_7_R3":
            case "v1_7_R4":
                throw new IllegalArgumentException(nms + " isn't supported");
            case "v1_8_R1":
                return 5;
            case "v1_8_R2":
                return 6;
            case "v1_8_R3":
                return 7;
            case "v1_9_R1":
                return 8;
            case "v1_9_R2":
                return 9;
            case "v1_10_R1":
                return 10;
            case "v1_11_R1":
                return 11;
            case "v1_12_R1":
                return 12;
            case "v1_13_R1":
                return 13;
            case "v1_13_R2":
                return 14;
            case "v1_14_R1":
                return 15;
            case "v1_15_R1":
                return 16;
            case "v1_16_R1":
                return 17;
            case "v1_16_R2":
                return 18;
            case "v1_16_R3":
                return 19;
            case "v1_17_R1":
                return 20;
            case "v1_18_R1":
                return 21;
            case "v1_18_R2":
                return 22;
            case "v1_19_R1":
                return 23;
        }
        throw new IllegalArgumentException(nms + " isn't supported");
    }

    static String conf(final String s) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getPlugin().getConfig().getString(s)));
    }
}
