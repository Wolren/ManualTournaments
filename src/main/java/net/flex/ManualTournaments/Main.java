package net.flex.ManualTournaments;

import net.flex.ManualTournaments.commands.*;
import net.flex.ManualTournaments.commands.Queue;
import net.flex.ManualTournaments.events.PlayerJumpEvent;
import net.flex.ManualTournaments.expansions.TeamPrefix;
import net.flex.ManualTournaments.factories.FightFactory;
import net.flex.ManualTournaments.guis.ArenaGUI;
import net.flex.ManualTournaments.guis.KitGUI;
import net.flex.ManualTournaments.listeners.GUIListener;
import net.flex.ManualTournaments.listeners.SpectateListener;
import net.flex.ManualTournaments.listeners.TeamFightListener;
import net.flex.ManualTournaments.listeners.TemporaryListener;
import net.flex.ManualTournaments.utils.UpdateChecker;
import net.flex.ManualTournaments.utils.gui.GUI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

import static net.flex.ManualTournaments.utils.SharedComponents.playerIsInTeam;

public final class Main extends JavaPlugin {
    public static Main getPlugin() {
        return getPlugin(Main.class);
    }
    public static GUI gui;
    public static int version;
    public static Set<String> kitNames = new HashSet<>(), arenaNames = new HashSet<>(), presetNames = new HashSet<>();
    private static File KitsConfigFile, ArenaConfigFile, PresetConfigFile, CustomConfigFile;
    private static FileConfiguration KitsConfig, ArenaConfig, PresetConfig, CustomConfig;
    private static final Map<String, Integer> versionMap = new HashMap<String, Integer>() {{
        put("v1_8_R1", 11);
        put("v1_8_R2", 12);
        put("v1_8_R3", 13);
        put("v1_9_R1", 14);
        put("v1_9_R2", 15);
        put("v1_10_R1", 16);
        put("v1_11_R1", 17);
        put("v1_12_R1", 18);
        put("v1_13_R1", 19);
        put("v1_13_R2", 20);
        put("v1_14_R1", 21);
        put("v1_15_R1", 22);
        put("v1_16_R1", 23);
        put("v1_16_R2", 24);
        put("v1_16_R3", 25);
        put("v1_17_R1", 26);
        put("v1_18_R1", 27);
        put("v1_18_R2", 28);
        put("v1_19_R1", 29);
        put("v1_19_R2", 30);
        put("v1_19_R3", 31);
        put("v1_20_R1", 32);
    }};

    public static File getKitConfigFile() {
        return KitsConfigFile;
    }

    public static File getArenaConfigFile() {
        return ArenaConfigFile;
    }

    public static File getPresetConfigFile() {
        return PresetConfigFile;
    }

    public static File getCustomConfigFile() {
        return CustomConfigFile;
    }

    public static FileConfiguration getKitConfig() {
        return KitsConfig;
    }

    public static FileConfiguration getArenaConfig() {
        return ArenaConfig;
    }

    public static FileConfiguration getPresetConfig() {
        return PresetConfig;
    }

    public static FileConfiguration getCustomConfig() {
        return CustomConfig;
    }

    @Override
    public void onLoad() {
        version = Main.formatNMSVersion(Main.getNMSVersion());
    }

    public void onEnable() {
        super.onEnable();
        gui = new GUI(this);
        new UpdateChecker();
        initializeData();
        setCommands();
        registerEvents();
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TeamPrefix().register();
        }
    }

    public void onDisable() {
        Bukkit.getServer().getOnlinePlayers().stream().filter(player -> playerIsInTeam(player.getUniqueId())).forEach(player -> player.getInventory().clear());
        FightFactory.fight.stopFight();
        Bukkit.getServer().getOnlinePlayers().stream().filter(player -> Spectate.spectators.contains(player.getUniqueId())).forEach(Spectate::stopSpectator);
        ArenaGUI.isOpenerActive = false;
        KitGUI.isOpenerActive = false;
        super.onDisable();
    }

    private void initializeData() {
        createKitsConfig();
        createArenaConfig();
        createPresetConfig();
        createCustomConfig();
        getConfig().options().copyDefaults(true);
        FileConfiguration Kits = YamlConfiguration.loadConfiguration(KitsConfigFile);
        FileConfiguration Arenas = YamlConfiguration.loadConfiguration(ArenaConfigFile);
        FileConfiguration Presets = YamlConfiguration.loadConfiguration(PresetConfigFile);
        if (Kits.getConfigurationSection("Kits") != null) {
            kitNames.addAll(Objects.requireNonNull(Kits.getConfigurationSection("Kits")).getKeys(false));
        }
        if (Arenas.getConfigurationSection("Arenas") != null) {
            arenaNames.addAll(Objects.requireNonNull(Arenas.getConfigurationSection("Arenas")).getKeys(false));
        }
        if (Presets.getConfigurationSection("Presets") != null) {
            presetNames.addAll(Objects.requireNonNull(Presets.getConfigurationSection("Presets")).getKeys(false));
        }
    }

    private void setCommands() {
        Map<String, CommandExecutor> commandsMap = new HashMap<>();
        commandsMap.put("manualtournaments_arena", new Arena());
        commandsMap.put("manualtournaments_fight", new Fight());
        commandsMap.put("manualtournaments_kit", new Kit());
        commandsMap.put("manualtournaments_reload", new Reload());
        commandsMap.put("manualtournaments_settings", new Settings());
        commandsMap.put("manualtournaments_spectate", new Spectate());
        commandsMap.put("manualtournaments_queue", new Queue());
        commandsMap.forEach((command, executor) -> {
            Objects.requireNonNull(getCommand(command)).setExecutor(executor);
            Objects.requireNonNull(getCommand(command)).setTabCompleter((TabCompleter) executor);
        });
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new GUIListener(), this);
        pluginManager.registerEvents(new SpectateListener(), this);
        pluginManager.registerEvents(new TeamFightListener(), this);
        pluginManager.registerEvents(new TemporaryListener(), this);
        pluginManager.registerEvents(new PlayerJumpEvent.CallJumpEvent(), this);
    }

    static String getNMSVersion() {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        return version.substring(version.lastIndexOf('.') + 1);
    }

    static int formatNMSVersion(String nms) {
        if (Objects.requireNonNull(versionMap).containsKey(nms)) return versionMap.get(nms);
        else if (nms.matches("v1_[4-7]_R[1-4]")) throw new IllegalArgumentException(nms + " isn't supported");
        else return 100;
    }

    private void createArenaConfig() {
        ArenaConfigFile = new File(getDataFolder(), "arenas.yml");
        ArenaConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(ArenaConfigFile);
        if (!ArenaConfigFile.exists()) {
            created(ArenaConfigFile.getParentFile().mkdirs());
            saveResource("arenas.yml", false);
        }
    }

    private void createKitsConfig() {
        KitsConfigFile = new File(getDataFolder(), "kits.yml");
        KitsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(KitsConfigFile);
        if (!KitsConfigFile.exists()) {
            created(KitsConfigFile.getParentFile().mkdirs());
            saveResource("kits.yml", false);
        }
    }

    private void createPresetConfig() {
        PresetConfigFile = new File(getDataFolder(), "presets.yml");
        PresetConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(PresetConfigFile);
        if (!PresetConfigFile.exists()) {
            created(PresetConfigFile.getParentFile().mkdirs());
            saveResource("presets.yml", false);
        }
    }

    private void createCustomConfig() {
        CustomConfigFile = new File(getDataFolder(), "config.yml");
        CustomConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(CustomConfigFile);
        if (!CustomConfigFile.exists()) {
            created(CustomConfigFile.getParentFile().mkdirs());
            saveResource("config.yml", false);
        }
    }


    private void created(boolean create) {
        if (!create) getPlugin().getLogger().log(Level.SEVERE, "Failed to create config directory");
    }
}
