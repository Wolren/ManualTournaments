package net.flex.ManualTournaments;

import net.flex.ManualTournaments.commands.Arena;
import net.flex.ManualTournaments.commands.Fight;
import net.flex.ManualTournaments.commands.FightArena;
import net.flex.ManualTournaments.commands.Kit;
import net.flex.ManualTournaments.commands.Reload;
import net.flex.ManualTournaments.commands.Settings;
import net.flex.ManualTournaments.commands.Spectate;
import net.flex.ManualTournaments.events.PlayerJumpEvent;
import net.flex.ManualTournaments.factories.FightFactory;
import net.flex.ManualTournaments.guis.ArenaGUI;
import net.flex.ManualTournaments.guis.KitGUI;
import net.flex.ManualTournaments.listeners.GUIListener;
import net.flex.ManualTournaments.listeners.SpectateListener;
import net.flex.ManualTournaments.listeners.TeamFightListener;
import net.flex.ManualTournaments.listeners.TemporaryListener;
import net.flex.ManualTournaments.utils.UpdateChecker;
import net.flex.ManualTournaments.utils.gui.GUI;
import net.flex.ManualTournaments.expansions.PlaceholderHook;
import net.flex.ManualTournaments.utils.tournament.TournamentCommand;
import net.flex.ManualTournaments.utils.tournament.TournamentDatabase;
import net.flex.ManualTournaments.utils.tournament.TournamentGUIListener;
import net.flex.ManualTournaments.utils.tournament.TournamentListener;
import net.flex.ManualTournaments.utils.tournament.TournamentManager;
import net.flex.ManualTournaments.utils.tournament.TournamentScheduler;
import net.flex.ManualTournaments.utils.tournament.TournamentScoreboard;
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

public class Main extends JavaPlugin {
    private static Main instance;

    public static Main getPlugin() {
        return instance;
    }
    public static GUI gui;
    public static int version;
    public static Set<String> kitNames = new HashSet<>(), arenaNames = new HashSet<>(), presetNames = new HashSet<>();
    private static File KitsConfigFile, ArenaConfigFile, PresetConfigFile;
    private static FileConfiguration KitsConfig, ArenaConfig, PresetConfig;
    private final List<TeamFightListener> activeFightListeners = new ArrayList<>();
    private final List<TemporaryListener> activeTemporaryListeners = new ArrayList<>();
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
        put("v1_20_R2", 33);
        put("v1_20_R3", 34);
        put("v1_20_R4", 35);
        put("v1_21_R1", 36);
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



    public static FileConfiguration getKitConfig() {
        return KitsConfig;
    }

    public static FileConfiguration getArenaConfig() {
        return ArenaConfig;
    }

    public static FileConfiguration getPresetConfig() {
        return PresetConfig;
    }



    @Override
    public void onLoad() {
        version = Main.formatNMSVersion(Main.getNMSVersion());
    }

    public void onEnable() {
        super.onEnable();
        instance = this;
        gui = new GUI(this);
        new UpdateChecker();
        initializeData();
        TournamentManager.getInstance().initialize(getDataFolder());
        TournamentScheduler.reload();
        TournamentScheduler.start();
        setCommands();
        registerEvents();
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(getPlugin()).register();
        }
    }

    public void onDisable() {
        Bukkit.getServer().getOnlinePlayers().stream().filter(player -> FightFactory.isPlayerInAnyFight(player.getUniqueId())).forEach(player -> player.getInventory().clear());
        FightFactory.stopAllFights();
        Bukkit.getServer().getOnlinePlayers().stream().filter(player -> Spectate.spectators.contains(player.getUniqueId())).forEach(Spectate::stopSpectator);
        ArenaGUI.isOpenerActive.clear();
        KitGUI.isOpenerActive.clear();
        TournamentScoreboard.hideAll();
        TournamentScheduler.stop();
        TournamentManager.getInstance().flush();
        TournamentDatabase.getInstance().close();
        for (TeamFightListener fightListener : activeFightListeners) {
            fightListener.triggerBlockResetAsync();
        }
        activeFightListeners.clear();
        activeTemporaryListeners.clear();
        instance = null;
        super.onDisable();
    }

    public void addFightListener(TeamFightListener listener) {
        activeFightListeners.add(listener);
    }

    public void removeFightListener(TeamFightListener listener) {
        activeFightListeners.remove(listener);
    }

    public void addTemporaryListener(TemporaryListener listener) {
        activeTemporaryListeners.add(listener);
    }

    public void removeTemporaryListener(TemporaryListener listener) {
        activeTemporaryListeners.remove(listener);
    }

    private void initializeData() {
        createKitsConfig();
        createArenaConfig();
        createPresetConfig();
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
        commandsMap.put("manualtournaments_fight_arena", new FightArena());
        commandsMap.put("manualtournaments_kit", new Kit());
        commandsMap.put("manualtournaments_reload", new Reload());
        commandsMap.put("manualtournaments_settings", new Settings());
        commandsMap.put("manualtournaments_spectate", new Spectate());
        commandsMap.put("manualtournaments_queue", new net.flex.ManualTournaments.commands.Queue());
        commandsMap.put("manualtournaments_tournament", new TournamentCommand());
        commandsMap.forEach((command, executor) -> {
            Objects.requireNonNull(getCommand(command)).setExecutor(executor);
            Objects.requireNonNull(getCommand(command)).setTabCompleter((TabCompleter) executor);
        });
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new GUIListener(), this);
        pluginManager.registerEvents(new SpectateListener(), this);
        pluginManager.registerEvents(new PlayerJumpEvent.CallJumpEvent(), this);
        pluginManager.registerEvents(new TournamentGUIListener(), this);
        pluginManager.registerEvents(new TournamentListener(), this);
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
        if (!ArenaConfigFile.exists()) {
            ArenaConfigFile.getParentFile().mkdirs();
            saveResource("arenas.yml", false);
        }
        ArenaConfig = YamlConfiguration.loadConfiguration(ArenaConfigFile);
    }

    private void createKitsConfig() {
        KitsConfigFile = new File(getDataFolder(), "kits.yml");
        if (!KitsConfigFile.exists()) {
            KitsConfigFile.getParentFile().mkdirs();
            saveResource("kits.yml", false);
        }
        KitsConfig = YamlConfiguration.loadConfiguration(KitsConfigFile);
    }

    private void createPresetConfig() {
        PresetConfigFile = new File(getDataFolder(), "presets.yml");
        if (!PresetConfigFile.exists()) {
            PresetConfigFile.getParentFile().mkdirs();
            saveResource("presets.yml", false);
        }
        PresetConfig = YamlConfiguration.loadConfiguration(PresetConfigFile);
    }


}
