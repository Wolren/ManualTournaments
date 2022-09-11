package net.flex.FlexTournaments;

import net.flex.FlexTournaments.api.CommandManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin {
    public List<String> kitNames;
    public List<String> arenaNames;
    public static Main getPlugin() {
        return getPlugin(Main.class);
    }
    public File KitsConfigfile, ArenaConfigFile, customConfigFile;
    public FileConfiguration KitsConfig, ArenaConfig, customConfig;

    public void onEnable(){
        kitNames = new ArrayList<>();
        arenaNames = new ArrayList<>();
        createKitsConfig();
        createArenaConfig();
        createCustomConfig();
        FileConfiguration Kits = YamlConfiguration.loadConfiguration(KitsConfigfile);
        FileConfiguration Arenas = YamlConfiguration.loadConfiguration(ArenaConfigFile);
        getConfig().options().copyDefaults(true);
        if (Kits.getConfigurationSection("Kits") != null) {
            kitNames.addAll(Objects.requireNonNull(Kits.getConfigurationSection("Kits")).getKeys(false));
        }
        if (Arenas.getConfigurationSection("Arenas") != null) {
            arenaNames.addAll(Objects.requireNonNull(Arenas.getConfigurationSection("Arenas")).getKeys(false));
        }
        CommandManager.register(new Kit());
        CommandManager.register(new Arena());
        CommandManager.register(new Fight());
        CommandManager.register(new Settings());
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
}
