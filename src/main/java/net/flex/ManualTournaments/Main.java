package net.flex.ManualTournaments;

import net.flex.ManualTournaments.commands.*;
import net.flex.ManualTournaments.commands.fightCommands.TeamFight;
import net.flex.ManualTournaments.events.PlayerJumpEvent;
import net.flex.ManualTournaments.factories.FightFactory;
import net.flex.ManualTournaments.listeners.SpectateListener;
import net.flex.ManualTournaments.listeners.TeamFightListener;
import net.flex.ManualTournaments.listeners.TemporaryListener;
import net.flex.ManualTournaments.utils.SpiGUI.SpiGUI;
import net.flex.ManualTournaments.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
    public static Main getPlugin() {
        return getPlugin(Main.class);
    }

    public static int version;
    public List<String> kitNames, arenaNames;
    public File KitsConfigfile, ArenaConfigFile, customConfigFile;
    static FileConfiguration KitsConfig, ArenaConfig, customConfig;
    private static final Map<String, Integer> versionMap = new HashMap<>();
    public static SpiGUI gui;

    static {
        versionMap.put("v1_8_R1", 11);
        versionMap.put("v1_8_R2", 12);
        versionMap.put("v1_8_R3", 13);
        versionMap.put("v1_9_R1", 14);
        versionMap.put("v1_9_R2", 15);
        versionMap.put("v1_10_R1", 16);
        versionMap.put("v1_11_R1", 17);
        versionMap.put("v1_12_R1", 18);
        versionMap.put("v1_13_R1", 19);
        versionMap.put("v1_13_R2", 20);
        versionMap.put("v1_14_R1", 21);
        versionMap.put("v1_15_R1", 22);
        versionMap.put("v1_16_R1", 23);
        versionMap.put("v1_16_R2", 24);
        versionMap.put("v1_16_R3", 25);
        versionMap.put("v1_17_R1", 26);
        versionMap.put("v1_18_R1", 27);
        versionMap.put("v1_18_R2", 28);
        versionMap.put("v1_19_R1", 29);
        versionMap.put("v1_19_R2", 30);
        versionMap.put("v1_19_R3", 31);
        versionMap.put("v1_20_R1", 32);
    }

    public static FileConfiguration getKitsConfig() {
        return KitsConfig;
    }

    public static FileConfiguration getArenaConfig() {
        return ArenaConfig;
    }

    public static FileConfiguration getCustomConfig() {
        return customConfig;
    }

    @Override
    public void onLoad() {
        version = Main.formatNMSVersion(Main.getNMSVersion());
    }

    public void onEnable() {
        super.onEnable();
        new UpdateChecker();
        initializeData();
        gui = new SpiGUI(this);
        setCommands();
        registerEvents();
    }

    public void onDisable() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (TeamFight.team1.contains(player.getUniqueId()) || TeamFight.team2.contains(player.getUniqueId())) {
                player.getInventory().clear();
            }
        }
        FightFactory.fight.stopFight();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (Spectate.spectators.contains(player.getUniqueId())) Spectate.stopSpectator(player);
        }
        super.onDisable();
    }

    private void initializeData() {
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
    }

    private void setCommands() {
        Map<String, CommandExecutor> commandsMap = new HashMap<>();
        commandsMap.put("manualtournaments_arena", new Arena());
        commandsMap.put("manualtournaments_fight", new Fight());
        commandsMap.put("manualtournaments_kit", new Kit());
        commandsMap.put("manualtournaments_reload", new Reload());
        commandsMap.put("manualtournaments_settings", new Settings());
        commandsMap.put("manualtournaments_spectate", new Spectate());

        commandsMap.forEach((command, executor) -> {
            Objects.requireNonNull(getCommand(command)).setExecutor(executor);
            Objects.requireNonNull(getCommand(command)).setTabCompleter((TabCompleter) executor);
        });
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new SpectateListener(), this);
        getServer().getPluginManager().registerEvents(new TeamFightListener(), this);
        getServer().getPluginManager().registerEvents(new TemporaryListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJumpEvent.CallJumpEvent(), this);
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

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "config.yml");
        customConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(customConfigFile);
        if (!customConfigFile.exists()) {
            created(customConfigFile.getParentFile().mkdirs());
            saveResource("config.yml", false);
        }
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
        KitsConfigfile = new File(getDataFolder(), "kits.yml");
        KitsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(KitsConfigfile);
        if (!KitsConfigfile.exists()) {
            created(KitsConfigfile.getParentFile().mkdirs());
            saveResource("kits.yml", false);
        }
    }

    private void created(boolean create) {
        if (!create) getPlugin().getLogger().log(Level.SEVERE, "Failed to create config directory");
    }

}
