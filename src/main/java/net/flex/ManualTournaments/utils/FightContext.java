package net.flex.ManualTournaments.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

public class FightContext {
    public final AtomicBoolean cancelled = new AtomicBoolean(false);
    public final Set<UUID> frozen = new HashSet<>();
    public final Set<Player> distinctFighters = new HashSet<>();
    public final Map<Team, Set<UUID>> teams;
    public final Scoreboard board;
    public Team team1, team2;
    public FileConfiguration fightsConfig;
    public File fightsConfigFile;
    public int duration;
    public boolean stopper;
    public Runnable onFightEnd;

    public FightContext(Map<Team, Set<UUID>> teams, Scoreboard board) {
        this.teams = teams;
        this.board = board;
    }

    public boolean playerIsInTeam(UUID player) {
        return teams.values().stream().anyMatch(list -> list.contains(player));
    }

    public void removeEntry(Player player) {
        for (Map.Entry<Team, Set<UUID>> entry : teams.entrySet()) {
            Team team = entry.getKey();
            Set<UUID> playerUUIDs = entry.getValue();
            if (playerUUIDs.contains(player.getUniqueId())) {
                if (team.getName().equals("1") && team1 != null) {
                    team1.removeEntry(player.getName());
                } else if (team.getName().equals("2") && team2 != null) {
                    team2.removeEntry(player.getName());
                }
                break;
            }
        }
    }

    public String teamList(String teamName) {
        Set<String> teamString = new HashSet<>();
        for (Map.Entry<Team, Set<UUID>> entry : teams.entrySet()) {
            if (entry.getKey().getName().equals(teamName)) {
                for (UUID uuid : entry.getValue()) {
                    teamString.add(Objects.requireNonNull(Bukkit.getOfflinePlayer(uuid)).getName());
                }
            }
        }
        return String.join(", ", teamString);
    }

    public void createFightsFolder() {
        File fightsConfigFolder = new File(net.flex.ManualTournaments.Main.getPlugin().getDataFolder(), "fights");
        if (!fightsConfigFolder.exists()) {
            boolean create = fightsConfigFolder.mkdir();
            if (!create) net.flex.ManualTournaments.Main.getPlugin().getLogger().log(Level.SEVERE, "Failed to create fights directory");
        }
        File[] filesInFightsFolder = fightsConfigFolder.listFiles();
        int i = (filesInFightsFolder != null ? filesInFightsFolder.length : 0) + 1;
        fightsConfigFile = new File(net.flex.ManualTournaments.Main.getPlugin().getDataFolder(), "fights/fight" + i + ".yml");
        fightsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(fightsConfigFile);
    }
}
