package net.flex.FlexTournaments;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin {
    public List<String> kitNames, arenaNames;
    public File KitsConfigfile, ArenaConfigFile, customConfigFile;
    public FileConfiguration KitsConfig, ArenaConfig, customConfig;
    public static Main getPlugin() {
        return getPlugin(Main.class);
    }

    public void onEnable(){
        kitNames = new ArrayList<>();
        arenaNames = new ArrayList<>();
        createKitsConfig();
        createArenaConfig();
        createCustomConfig();
        createFightsFolder();
        getConfig().options().copyDefaults(true);
        FileConfiguration Kits = YamlConfiguration.loadConfiguration(KitsConfigfile);
        FileConfiguration Arenas = YamlConfiguration.loadConfiguration(ArenaConfigFile);
        if (Kits.getConfigurationSection("Kits") != null) {
            kitNames.addAll(Objects.requireNonNull(Kits.getConfigurationSection("Kits")).getKeys(false));
        }
        if (Arenas.getConfigurationSection("Arenas") != null) {
            arenaNames.addAll(Objects.requireNonNull(Arenas.getConfigurationSection("Arenas")).getKeys(false));
        }
        Objects.requireNonNull(getCommand("flextournaments_arena")).setExecutor(new Arena());
        Objects.requireNonNull(getCommand("flextournaments_arena")).setTabCompleter(new Arena());
        Objects.requireNonNull(getCommand("flextournaments_fight")).setExecutor(new Fight());
        Objects.requireNonNull(getCommand("flextournaments_kit")).setExecutor(new Kit());
        Objects.requireNonNull(getCommand("flextournaments_kit")).setTabCompleter(new Kit());
        Objects.requireNonNull(getCommand("flextournaments_settings")).setExecutor(new Settings());
        Objects.requireNonNull(getCommand("flextournaments_settings")).setTabCompleter(new Settings());
        this.getServer().getPluginManager().registerEvents(new MyListener(), this);
    }

    public void onDisable() {
    }

    public FileConfiguration getKitsConfig() {
        return this.KitsConfig;
    }
    public FileConfiguration getArenaConfig() {
        return this.ArenaConfig;
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

    private void createFightsFolder() {
        File file = new File("/fights");
        file.mkdirs();
    }

    public static String conf(String s) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Main.getPlugin().getConfig().getString(s)));
    }
}
