package net.flex.FlexTournaments;

import net.flex.FlexTournaments.api.CommandManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin {
    public List<String> kitNames;
    public static Main getPlugin() {
        return getPlugin(Main.class);
    }
    private File KitsConfigfile, customConfigFile;
    private FileConfiguration KitsConfig, customConfig;

    public void onEnable(){
        CommandManager.register(new Kit());
        kitNames = new ArrayList();
        createKitsConfig();
        createCustomConfig();
        getConfig().options().copyDefaults(true);
        FileConfiguration config = YamlConfiguration.loadConfiguration(KitsConfigfile);
        if (config.getConfigurationSection("Kits") != null) {
            kitNames.addAll(config.getConfigurationSection("Kits").getKeys(false));
        }
    }

    public void onDisable() {
    }

    public FileConfiguration getKitsConfig() {
        return this.KitsConfig;
    }

    private void createKitsConfig() {
        KitsConfigfile = new File(getDataFolder(), "kits.yml");
        if (!KitsConfigfile.exists()) {
            KitsConfigfile.getParentFile().mkdirs();
            saveResource("kits.yml", false);
        }

        KitsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(KitsConfigfile);
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "config.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        customConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(customConfigFile);
    }

    public String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
