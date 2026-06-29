package net.flex.ManualTournaments.utils.tournament;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.flex.ManualTournaments.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

/**
 * MySQL storage layer for tournament data using HikariCP connection pool.
 * All DB writes are async (off main thread) when the pool is active.
 * Activated when mysql-enabled: true in config.yml.
 */
public class TournamentDatabase {

    private static TournamentDatabase instance;
    private HikariDataSource dataSource;
    private boolean available;

    private TournamentDatabase() {
        this.available = false;
    }

    public static TournamentDatabase getInstance() {
        if (instance == null) {
            instance = new TournamentDatabase();
        }
        return instance;
    }

    /**
     * Initialize the connection pool and create tables.
     */
    public boolean initialize() {
        FileConfiguration config = Main.getPlugin().getConfig();
        if (!config.getBoolean("mysql-enabled", false)) {
            return false;
        }
        try {
            String host = config.getString("mysql.url", "localhost:3306");
            String username = config.getString("mysql.username", "root");
            String password = config.getString("mysql.password", "");
            String jdbcUrl = "jdbc:mysql://" + host
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setPoolName("MT-Tournament");
            int poolSize = config.getInt("tournament-database-pool-size", 4);
            hikariConfig.setMaximumPoolSize(Math.max(1, Math.min(32, poolSize)));
            hikariConfig.setMinimumIdle(1);
            int connTimeout = config.getInt("tournament-database-connection-timeout", 5000);
            hikariConfig.setConnectionTimeout(Math.max(1000, connTimeout));
            hikariConfig.setIdleTimeout(300000);
            hikariConfig.setMaxLifetime(600000);
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "25");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(hikariConfig);

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE DATABASE IF NOT EXISTS ManualTournaments");
                stmt.execute("USE ManualTournaments");

                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS mt_tournaments (" +
                    "  name VARCHAR(64) PRIMARY KEY," +
                    "  phase VARCHAR(20) NOT NULL DEFAULT 'REGISTRATION'," +
                    "  max_players INT NOT NULL DEFAULT 16," +
                    "  team_size INT NOT NULL DEFAULT 1," +
                    "  arena_name VARCHAR(64) DEFAULT ''," +
                    "  kit_name VARCHAR(64) DEFAULT ''," +
                    "  created_time BIGINT NOT NULL," +
                    "  scheduled_start_time BIGINT NOT NULL DEFAULT 0," +
                    "  match_timeout INT NOT NULL DEFAULT 0," +
                    "  paused BOOLEAN NOT NULL DEFAULT FALSE," +
                    "  winner VARCHAR(36) DEFAULT NULL" +
                    ")"
                );
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS mt_tournament_players (" +
                    "  tournament_name VARCHAR(64) NOT NULL," +
                    "  player_uuid VARCHAR(36) NOT NULL," +
                    "  PRIMARY KEY (tournament_name, player_uuid)," +
                    "  FOREIGN KEY (tournament_name) REFERENCES mt_tournaments(name) ON DELETE CASCADE" +
                    ")"
                );
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS mt_tournament_prizes (" +
                    "  tournament_name VARCHAR(64) NOT NULL," +
                    "  prize_index INT NOT NULL," +
                    "  command TEXT NOT NULL," +
                    "  PRIMARY KEY (tournament_name, prize_index)," +
                    "  FOREIGN KEY (tournament_name) REFERENCES mt_tournaments(name) ON DELETE CASCADE" +
                    ")"
                );
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS mt_tournament_team_members (" +
                    "  tournament_name VARCHAR(64) NOT NULL," +
                    "  captain_uuid VARCHAR(36) NOT NULL," +
                    "  member_uuid VARCHAR(36) NOT NULL," +
                    "  member_order INT NOT NULL," +
                    "  PRIMARY KEY (tournament_name, captain_uuid, member_uuid)," +
                    "  FOREIGN KEY (tournament_name) REFERENCES mt_tournaments(name) ON DELETE CASCADE" +
                    ")"
                );
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS mt_player_stats (" +
                    "  player_uuid VARCHAR(36) PRIMARY KEY," +
                    "  tournaments_played INT NOT NULL DEFAULT 0," +
                    "  wins INT NOT NULL DEFAULT 0," +
                    "  losses INT NOT NULL DEFAULT 0" +
                    ")"
                );
            }

            available = true;
            Main.getPlugin().getLogger().info("[TournamentDB] HikariCP pool initialized.");
            return true;
        } catch (SQLException e) {
            Main.getPlugin().getLogger().log(Level.WARNING, "[TournamentDB] Could not initialize MySQL pool", e);
            close();
            return false;
        }
    }

    /**
     * Close the connection pool.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
        available = false;
    }

    public boolean isAvailable() {
        return available;
    }

    // --- Write helpers (async) ---

    private void asyncWrite(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), task);
        } else {
            task.run();
        }
    }

    // --- Tournament CRUD ---

    public void saveTournament(Tournament t) {
        if (!available) return;
        asyncWrite(() -> saveTournamentSync(t));
    }

    private void saveTournamentSync(Tournament t) {
        try (Connection conn = getConnection()) {
            // Upsert tournament
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO mt_tournaments (name, phase, max_players, team_size, arena_name, kit_name, " +
                    "  created_time, scheduled_start_time, match_timeout, paused, winner) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "  phase=VALUES(phase), max_players=VALUES(max_players), team_size=VALUES(team_size), " +
                    "  arena_name=VALUES(arena_name), kit_name=VALUES(kit_name), paused=VALUES(paused), " +
                    "  winner=VALUES(winner)"
            )) {
                ps.setString(1, t.getName());
                ps.setString(2, t.getPhase().name());
                ps.setInt(3, t.getMaxPlayers());
                ps.setInt(4, t.getTeamSize());
                ps.setString(5, t.getArenaName() != null ? t.getArenaName() : "");
                ps.setString(6, t.getKitName() != null ? t.getKitName() : "");
                ps.setLong(7, t.getCreatedTime());
                ps.setLong(8, t.getScheduledStartTime());
                ps.setInt(9, t.getMatchTimeout());
                ps.setBoolean(10, t.isPaused());
                ps.setString(11, t.getWinner() != null ? t.getWinner().toString() : null);
                ps.executeUpdate();
            }

            // Batch-replace players
            try (PreparedStatement clearPs = conn.prepareStatement(
                    "DELETE FROM mt_tournament_players WHERE tournament_name = ?")) {
                clearPs.setString(1, t.getName());
                clearPs.executeUpdate();
            }
            try (PreparedStatement insertPs = conn.prepareStatement(
                    "INSERT INTO mt_tournament_players (tournament_name, player_uuid) VALUES (?, ?)")) {
                for (UUID uid : t.getPlayers()) {
                    insertPs.setString(1, t.getName());
                    insertPs.setString(2, uid.toString());
                    insertPs.addBatch();
                }
                insertPs.executeBatch();
            }

            // Batch-replace prizes
            try (PreparedStatement clearPs = conn.prepareStatement(
                    "DELETE FROM mt_tournament_prizes WHERE tournament_name = ?")) {
                clearPs.setString(1, t.getName());
                clearPs.executeUpdate();
            }
            if (!t.getPrizeCommands().isEmpty()) {
                try (PreparedStatement insertPs = conn.prepareStatement(
                        "INSERT INTO mt_tournament_prizes (tournament_name, prize_index, command) VALUES (?, ?, ?)")) {
                    for (int i = 0; i < t.getPrizeCommands().size(); i++) {
                        insertPs.setString(1, t.getName());
                        insertPs.setInt(2, i);
                        insertPs.setString(3, t.getPrizeCommands().get(i));
                        insertPs.addBatch();
                    }
                    insertPs.executeBatch();
                }
            }

            // Team members
            if (t.isTeamTournament()) {
                try (PreparedStatement clearPs = conn.prepareStatement(
                        "DELETE FROM mt_tournament_team_members WHERE tournament_name = ?")) {
                    clearPs.setString(1, t.getName());
                    clearPs.executeUpdate();
                }
                try (PreparedStatement insertPs = conn.prepareStatement(
                        "INSERT INTO mt_tournament_team_members (tournament_name, captain_uuid, member_uuid, member_order) " +
                        "VALUES (?, ?, ?, ?)")) {
                    Set<UUID> seen = new HashSet<>();
                    for (List<TournamentMatch> round : t.getBracket()) {
                        for (TournamentMatch match : round) {
                            for (UUID captain : new UUID[]{match.getPlayer1(), match.getPlayer2()}) {
                                if (captain == null || seen.contains(captain)) continue;
                                seen.add(captain);
                                List<UUID> members = t.getTeamMembers(captain);
                                if (members.size() <= 1) continue;
                                for (int i = 0; i < members.size(); i++) {
                                    insertPs.setString(1, t.getName());
                                    insertPs.setString(2, captain.toString());
                                    insertPs.setString(3, members.get(i).toString());
                                    insertPs.setInt(4, i);
                                    insertPs.addBatch();
                                }
                            }
                        }
                    }
                    insertPs.executeBatch();
                }
            }
        } catch (SQLException e) {
            Main.getPlugin().getLogger().log(Level.WARNING,
                    "[TournamentDB] Failed to save tournament '" + t.getName() + "'", e);
        }
    }

    /**
     * Load all tournaments from the database.
     */
    public Map<String, Tournament> loadAll() {
        Map<String, Tournament> result = new LinkedHashMap<>();
        if (!available) return result;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT t.*, " +
                     "  GROUP_CONCAT(DISTINCT CONCAT(tp.player_uuid) SEPARATOR ',') AS player_list, " +
                     "  GROUP_CONCAT(DISTINCT CONCAT(tpr.prize_index, ':', tpr.command) SEPARATOR '|') AS prize_list " +
                     "FROM mt_tournaments t " +
                     "LEFT JOIN mt_tournament_players tp ON tp.tournament_name = t.name " +
                     "LEFT JOIN mt_tournament_prizes tpr ON tpr.tournament_name = t.name " +
                     "GROUP BY t.name")) {

            while (rs.next()) {
                String name = rs.getString("name");
                Tournament t = new Tournament(name, rs.getInt("max_players"),
                        rs.getString("arena_name"), rs.getString("kit_name"));
                t.setTeamSize(rs.getInt("team_size"));
                try { t.setPhase(Tournament.Phase.valueOf(rs.getString("phase"))); } catch (Exception ignored) {}
                t.setScheduledStartTime(rs.getLong("scheduled_start_time"));
                t.setMatchTimeout(rs.getInt("match_timeout"));
                t.setPaused(rs.getBoolean("paused"));

                // Parse comma-separated player UUIDs
                String playerList = rs.getString("player_list");
                if (playerList != null && !playerList.isEmpty()) {
                    for (String uuidStr : playerList.split(",")) {
                        try { t.addPlayer(UUID.fromString(uuidStr.trim())); } catch (Exception ignored) {}
                    }
                }

                // Parse pipe-separated prize commands
                String prizeList = rs.getString("prize_list");
                if (prizeList != null && !prizeList.isEmpty()) {
                    // prize_list has format "0:command1|1:command2"
                    for (String entry : prizeList.split("\\|")) {
                        int colonIdx = entry.indexOf(':');
                        if (colonIdx > 0 && colonIdx < entry.length() - 1) {
                            t.addPrizeCommand(entry.substring(colonIdx + 1));
                        }
                    }
                }

                result.put(name, t);
            }
        } catch (SQLException e) {
            Main.getPlugin().getLogger().log(Level.WARNING, "[TournamentDB] Failed to load tournaments", e);
        }
        return result;
    }

    public void deleteTournament(String name) {
        if (!available) return;
        asyncWrite(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM mt_tournaments WHERE name = ?")) {
                ps.setString(1, name);
                ps.executeUpdate();
            } catch (SQLException e) {
                Main.getPlugin().getLogger().log(Level.WARNING,
                        "[TournamentDB] Failed to delete tournament '" + name + "'", e);
            }
        });
    }

    public void saveStats(UUID player, int tournamentsPlayed, int wins, int losses) {
        if (!available) return;
        asyncWrite(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO mt_player_stats (player_uuid, tournaments_played, wins, losses) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE tournaments_played=VALUES(tournaments_played), " +
                    "  wins=VALUES(wins), losses=VALUES(losses)")) {
                ps.setString(1, player.toString());
                ps.setInt(2, tournamentsPlayed);
                ps.setInt(3, wins);
                ps.setInt(4, losses);
                ps.executeUpdate();
            } catch (SQLException e) {
                Main.getPlugin().getLogger().log(Level.WARNING,
                        "[TournamentDB] Failed to save stats for " + player, e);
            }
        });
    }

    public int[] loadStats(UUID player) {
        if (!available) return new int[]{0, 0, 0};
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                    "SELECT tournaments_played, wins, losses FROM mt_player_stats WHERE player_uuid = ?")) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt("tournaments_played"), rs.getInt("wins"), rs.getInt("losses")};
                }
            }
        } catch (SQLException e) {
            Main.getPlugin().getLogger().log(Level.WARNING,
                    "[TournamentDB] Failed to load stats for " + player, e);
        }
        return new int[]{0, 0, 0};
    }

    private Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("Pool not initialized");
        return dataSource.getConnection();
    }
}
