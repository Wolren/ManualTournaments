package net.flex.FlexTournaments;

import net.flex.FlexTournaments.api.CommandManager;
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

    private File aConfigfile;

    private File customConfigFile;
    private FileConfiguration aConfig;

    private FileConfiguration customConfig;

    public void onEnable(){
        CommandManager.register(new Kit());
        this.kitNames = new ArrayList();
        createaConfig();
        createCustomConfig();
        getConfig().addDefault("kit-no-arguments", "You have to type at least one argument");
        getConfig().addDefault("kit-not-exists", "Kit doesn't exist");
        saveDefaultConfig();
        saveConfig();
    }

    public void onDisable() {
    }

    public FileConfiguration getaConfig() {
        return this.aConfig;
    }

    private void createaConfig() {
        aConfigfile = new File(getDataFolder(), "kits.yml");
        if (!aConfigfile.exists()) {
            aConfigfile.getParentFile().mkdirs();
            saveResource("kits.yml", false);
        }

        aConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(aConfigfile);
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
