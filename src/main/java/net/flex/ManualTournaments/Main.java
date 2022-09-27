package net.flex.ManualTournaments;

import org.bstats.bukkit.Metrics;
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

    static Main getPlugin() {
        return getPlugin(Main.class);
    }

    static String conf(String s) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getPlugin().getConfig().getString(s)));
    }

    public void onEnable() {
        int pluginId = 16516;
        new Metrics(this, pluginId);
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
        getServer().getPluginManager().registerEvents(new MyListener(), this);
    }

    public void onDisable() {
    }

    FileConfiguration getKitsConfig() {
        return KitsConfig;
    }

    FileConfiguration getArenaConfig() {
        return ArenaConfig;
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

    private void createArenaConfig() {
        ArenaConfigFile = new File(getDataFolder(), "arenas.yml");
        ArenaConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(ArenaConfigFile);
        if (!ArenaConfigFile.exists()) {
            ArenaConfigFile.getParentFile().mkdirs();
            saveResource("arenas.yml", false);
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
}
