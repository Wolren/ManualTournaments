package net.flex.FlexTournaments;

import net.flex.FlexTournaments.api.CommandManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {
    public static Main getPlugin() {
        return getPlugin(Main.class);
    }

    private File customConfigFile;
    private FileConfiguration customConfig;

    public void onEnable(){
        CommandManager.register(new Kit());
        createCustomConfig();
        getConfig().addDefault("kit-no-arguments", "You have to type at least one argument");
        saveDefaultConfig();
        getConfig().set("kit-not-exists", "Kit doesn't exist");
        saveConfig();
    }

    public void onDisable() {
    }

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
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
}
