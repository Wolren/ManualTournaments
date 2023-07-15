package net.flex.ManualTournaments;

import net.flex.ManualTournaments.events.PlayerJumpEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Main extends JavaPlugin {
    List<String> kitNames, arenaNames;
    File KitsConfigfile, ArenaConfigFile, customConfigFile;
    FileConfiguration KitsConfig, ArenaConfig, customConfig;
    public static int version = Main.formatNMSVersion(Main.getNMSVersion());

    public static Main getPlugin() {
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
        FileConfiguration Kits = YamlConfiguration.loadConfiguration(KitsConfigfile);
        FileConfiguration Arenas = YamlConfiguration.loadConfiguration(ArenaConfigFile);
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

    static String getNMSVersion() {
        String v = Bukkit.getServer().getClass().getPackage().getName();
        return v.substring(v.lastIndexOf('.') + 1);
    }

    static int formatNMSVersion(String nms) {
        switch (nms) {
            case "1_4_R1":
            case "1_5_R2":
            case "1_5_R3":
            case "1_6_R2":
            case "1_6_R3":
            case "1_6_R4":
            case "1_7_R1":
            case "1_7_R2":
            case "1_7_R3":
            case "1_7_R4":
                throw new IllegalArgumentException(nms + " isn't supported");
            case "v1_8_R1":
                return 11;
            case "v1_8_R2":
                return 12;
            case "v1_8_R3":
                return 13;
            case "v1_9_R1":
                return 14;
            case "v1_9_R2":
                return 15;
            case "v1_10_R1":
                return 16;
            case "v1_11_R1":
                return 17;
            case "v1_12_R1":
                return 18;
            case "v1_13_R1":
                return 19;
            case "v1_13_R2":
                return 20;
            case "v1_14_R1":
                return 21;
            case "v1_15_R1":
                return 22;
            case "v1_16_R1":
                return 23;
            case "v1_16_R2":
                return 24;
            case "v1_16_R3":
                return 25;
            case "v1_17_R1":
                return 26;
            case "v1_18_R1":
                return 27;
            case "v1_18_R2":
                return 28;
            case "v1_19_R1":
                return 29;
            case "v1_19_R2":
                return 30;
            case "v1_19_R3":
                return 31;
            case "v1_20_R1":
                return 32;
            default:
                return 100;
        }
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
}
