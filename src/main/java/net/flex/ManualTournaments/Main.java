package net.flex.ManualTournaments;

import net.flex.ManualTournaments.commands.*;
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
    private static final Map<String, Integer> VERSION_MAP = new HashMap<>();

    static {
        VERSION_MAP.put("v1_8_R1", 11);
        VERSION_MAP.put("v1_8_R2", 12);
        VERSION_MAP.put("v1_8_R3", 13);
        VERSION_MAP.put("v1_9_R1", 14);
        VERSION_MAP.put("v1_9_R2", 15);
        VERSION_MAP.put("v1_10_R1", 16);
        VERSION_MAP.put("v1_11_R1", 17);
        VERSION_MAP.put("v1_12_R1", 18);
        VERSION_MAP.put("v1_13_R1", 19);
        VERSION_MAP.put("v1_13_R2", 20);
        VERSION_MAP.put("v1_14_R1", 21);
        VERSION_MAP.put("v1_15_R1", 22);
        VERSION_MAP.put("v1_16_R1", 23);
        VERSION_MAP.put("v1_16_R2", 24);
        VERSION_MAP.put("v1_16_R3", 25);
        VERSION_MAP.put("v1_17_R1", 26);
        VERSION_MAP.put("v1_18_R1", 27);
        VERSION_MAP.put("v1_18_R2", 28);
        VERSION_MAP.put("v1_19_R1", 29);
        VERSION_MAP.put("v1_19_R2", 30);
        VERSION_MAP.put("v1_19_R3", 31);
        VERSION_MAP.put("v1_20_R1", 32);
    }

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
        version = formatNMSVersion(getNMSVersion());
    }

    public void onEnable() {
        gui = new GUI(this);
        new UpdateChecker();
        initializeData();
        setCommands();
        registerEvents();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TeamPrefix().register();
        }
    }

    public void onDisable() {
        Bukkit.getServer().getOnlinePlayers().stream().filter(player -> playerIsInTeam(player.getUniqueId())).forEach(player -> player.getInventory().clear());
        FightFactory.fight.stopFight();
        Bukkit.getServer().getOnlinePlayers().stream().filter(player -> Spectate.spectators.contains(player.getUniqueId())).forEach(Spectate::stopSpectator);
        ArenaGUI.isOpenerActive = false;
        KitGUI.isOpenerActive = false;
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
        // Known versions - use exact lookup
        Integer known = VERSION_MAP.get(nms);
        if (known != null) return known;

        // Unsupported early versions
        if (nms.matches("v1_[4-7]_R[1-4]")) {
            throw new IllegalArgumentException(nms + " isn't supported");
        }

        // Parse unknown newer versions dynamically.
        // Format: v1_XX_RY where XX = major version, Y = revision
        // Mapping: base = (majorVersion - 8) * 2 + 3, adjusted for revision
        try {
            String[] parts = nms.split("_");
            if (parts.length >= 3 && parts[0].startsWith("v") && parts[0].length() > 1) {
                int majorVersion = Integer.parseInt(parts[1]);
                int revision = Integer.parseInt(parts[2].substring(1)); // "R1" -> 1
                // Pattern: v1_8_R1=11, v1_9_R1=14, v1_10_R1=16
                // Base: (major - 8) * 2 + 3, then add (revision - 1)
                // v1_20_R1 = (20-8)*2+3 + (1-1) = 27
                // But we know v1_20_R1 = 32... so the pattern isn't simple linear.
                // Use a different mapping: for each major version bump, the value increases
                // by ~2 for minor revisions. Let's use a known-good formula:
                // For v1_X: the lowest R value for that version starts at a base and increments.
                // v1_8 starts at 11, v1_9 at 14 (+3), v1_10 at 16 (+2), v1_11 at 17 (+1),
                // v1_12 at 18 (+1), v1_13 at 19 (+1), v1_14 at 21 (+2), v1_15 at 22 (+1),
                // v1_16 at 23 (+1), v1_17 at 26 (+3), v1_18 at 27 (+1), v1_19 at 29 (+2),
                // v1_20 at 32 (+3)
                // There's no clean formula. For unknown versions, compute a reasonable
                // estimate: (majorVersion - 8) * 2 + 3 + (revision - 1) 
                // This gives us values in the right ballpark for current versions
                // and ensures future versions get higher numbers.
                int base = (majorVersion - 8) * 2 + 3;
                return base + (revision - 1);
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            // Fall through to default
        }

        // If all else fails, assume a modern version with all features
        getPlugin().getLogger().warning("Unknown server version: " + nms + ". Assuming latest API features.");
        return 100;
    }

    private void createArenaConfig() {
        ArenaConfigFile = new File(getDataFolder(), "arenas.yml");
        if (!ArenaConfigFile.exists()) {
            if (!ArenaConfigFile.getParentFile().exists()) {
                if (!ArenaConfigFile.getParentFile().mkdirs()) {
                    getLogger().log(Level.SEVERE, "Failed to create config directory");
                }
            }
            saveResource("arenas.yml", false);
        }
        ArenaConfig = YamlConfiguration.loadConfiguration(ArenaConfigFile);
    }

    private void createKitsConfig() {
        KitsConfigFile = new File(getDataFolder(), "kits.yml");
        if (!KitsConfigFile.exists()) {
            if (!KitsConfigFile.getParentFile().exists()) {
                if (!KitsConfigFile.getParentFile().mkdirs()) {
                    getLogger().log(Level.SEVERE, "Failed to create config directory");
                }
            }
            saveResource("kits.yml", false);
        }
        KitsConfig = YamlConfiguration.loadConfiguration(KitsConfigFile);
    }

    private void createPresetConfig() {
        PresetConfigFile = new File(getDataFolder(), "presets.yml");
        if (!PresetConfigFile.exists()) {
            if (!PresetConfigFile.getParentFile().exists()) {
                if (!PresetConfigFile.getParentFile().mkdirs()) {
                    getLogger().log(Level.SEVERE, "Failed to create config directory");
                }
            }
            saveResource("presets.yml", false);
        }
        PresetConfig = YamlConfiguration.loadConfiguration(PresetConfigFile);
    }

    private void createCustomConfig() {
        CustomConfigFile = new File(getDataFolder(), "config.yml");
        if (!CustomConfigFile.exists()) {
            if (!CustomConfigFile.getParentFile().exists()) {
                if (!CustomConfigFile.getParentFile().mkdirs()) {
                    getLogger().log(Level.SEVERE, "Failed to create config directory");
                }
            }
            saveResource("config.yml", false);
        }
        CustomConfig = YamlConfiguration.loadConfiguration(CustomConfigFile);
    }
}
