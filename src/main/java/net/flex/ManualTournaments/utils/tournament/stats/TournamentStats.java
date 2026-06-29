package net.flex.ManualTournaments.utils.tournament.stats;

import net.flex.ManualTournaments.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * YAML-based per-player tournament statistics.
 * Tracks: tournaments played, wins, losses.
 * Stats written to tournaments_stats.yml.
 */
public class TournamentStats {

    private static TournamentStats instance;
    private File configFile;
    private FileConfiguration config;

    private TournamentStats() {}

    public static TournamentStats getInstance() {
        if (instance == null) {
            instance = new TournamentStats();
        }
        return instance;
    }

    public void initialize(File dataFolder) {
        configFile = new File(dataFolder, "tournaments_stats.yml");
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (IOException e) {
                Main.getPlugin().getLogger().log(Level.SEVERE, "Could not create tournaments_stats.yml", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public boolean isEnabled() {
        return Main.getPlugin().getConfig().getBoolean("tournament-stats-enabled", true);
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            Main.getPlugin().getLogger().log(Level.SEVERE, "Could not save tournaments_stats.yml", e);
        }
    }

    public boolean hasData(UUID player) {
        return config.isSet("stats." + player.toString());
    }

    public int getTournamentsPlayed(UUID player) {
        return config.getInt("stats." + player.toString() + ".tournaments", 0);
    }

    public int getWins(UUID player) {
        return config.getInt("stats." + player.toString() + ".wins", 0);
    }

    public int getLosses(UUID player) {
        return config.getInt("stats." + player.toString() + ".losses", 0);
    }

    public int getWinRate(UUID player) {
        int played = getTournamentsPlayed(player);
        if (played == 0) return 0;
        return (int) Math.round((double) getWins(player) / played * 100);
    }

    public void recordTournamentPlayed(UUID player) {
        if (!isEnabled()) return;
        String path = "stats." + player.toString() + ".tournaments";
        config.set(path, config.getInt(path, 0) + 1);
        save();
    }

    public void recordWin(UUID player) {
        if (!isEnabled()) return;
        String path = "stats." + player.toString() + ".wins";
        config.set(path, config.getInt(path, 0) + 1);
        // Ensure tournaments counter exists
        String tPath = "stats." + player.toString() + ".tournaments";
        if (!config.isSet(tPath)) {
            config.set(tPath, 1);
        }
        save();
    }

    public void recordLoss(UUID player) {
        if (!isEnabled()) return;
        String path = "stats." + player.toString() + ".losses";
        config.set(path, config.getInt(path, 0) + 1);
        save();
    }
}
