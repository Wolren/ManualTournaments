package net.flex.ManualTournaments.utils;

import net.flex.ManualTournaments.commands.fightCommands.TeamFight;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.logging.Level;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.teamList;

public class SqlMethods {
    private static final FileConfiguration config = getPlugin().getConfig();
    private static final String currentArena = config.getString("current-arena");
    private static final String currentKit = config.getString("current-kit");

    public static void sqlFights() {
        String url = buildUrl();
        if (url == null) return;

        try (Connection connection = DriverManager.getConnection(url,
                config.getString("mysql.username"), config.getString("mysql.password"));
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Fights" +
                    " (id INT AUTO_INCREMENT," +
                    " team1 VARCHAR(1000), team2 VARCHAR(1000)," +
                    " damageTeam1 DOUBLE, damageTeam2 DOUBLE," +
                    " regeneratedTeam1 DOUBLE, regeneratedTeam2 DOUBLE," +
                    " arena VARCHAR(50), kit VARCHAR(50)," +
                    " duration INT," +
                    " winners VARCHAR(1000), PRIMARY KEY(id))");

            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Fights(" +
                    "team1, team2," +
                    " damageTeam1, damageTeam2," +
                    " regeneratedTeam1, regeneratedTeam2," +
                    " arena, kit," +
                    " duration," +
                    " winners)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                preparedStatement.setString(1, teamList("1"));
                preparedStatement.setString(2, teamList("2"));
                preparedStatement.setDouble(3, 0);
                preparedStatement.setDouble(4, 0);
                preparedStatement.setDouble(5, 0);
                preparedStatement.setDouble(6, 0);
                preparedStatement.setString(7, currentArena);
                preparedStatement.setString(8, currentKit);
                preparedStatement.setInt(9, TeamFight.duration);
                preparedStatement.setString(10, "");
                preparedStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            getPlugin().getLogger().log(Level.SEVERE, "SQL error in sqlFights", exception);
        }
    }

    public static void durationUpdate(int newDuration) {
        String url = buildUrl();
        if (url == null) return;

        try (Connection connection = DriverManager.getConnection(url,
                config.getString("mysql.username"), config.getString("mysql.password"));
             Statement maxIdStatement = connection.createStatement();
             ResultSet resultSet = maxIdStatement.executeQuery("SELECT MAX(id) AS max_id FROM Fights")) {

            if (resultSet.next()) {
                int maxId = resultSet.getInt("max_id");
                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE Fights SET duration = ? WHERE id = ?")) {
                    updateStatement.setInt(1, newDuration);
                    updateStatement.setInt(2, maxId);
                    updateStatement.executeUpdate();
                }
            }
        } catch (SQLException exception) {
            getPlugin().getLogger().log(Level.SEVERE, "SQL error in durationUpdate", exception);
        }
    }

    public static void damageUpdate(double newDamageTeam1, double newDamageTeam2) {
        String url = buildUrl();
        if (url == null) return;

        try (Connection connection = DriverManager.getConnection(url,
                config.getString("mysql.username"), config.getString("mysql.password"));
             Statement maxIdStatement = connection.createStatement();
             ResultSet resultSet = maxIdStatement.executeQuery("SELECT MAX(id) AS max_id FROM Fights")) {

            if (resultSet.next()) {
                int maxId = resultSet.getInt("max_id");
                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE Fights SET" +
                                " damageTeam1 = ?," +
                                " damageTeam2 = ?" +
                                " WHERE id = ?")) {
                    updateStatement.setDouble(1, newDamageTeam1);
                    updateStatement.setDouble(2, newDamageTeam2);
                    updateStatement.setInt(3, maxId);
                    updateStatement.executeUpdate();
                }
            }
        } catch (SQLException exception) {
            getPlugin().getLogger().log(Level.SEVERE, "SQL error in damageUpdate", exception);
        }
    }

    public static void regeneratedUpdate(double newRegeneratedTeam1, double newRegeneratedTeam2) {
        String url = buildUrl();
        if (url == null) return;

        try (Connection connection = DriverManager.getConnection(url,
                config.getString("mysql.username"), config.getString("mysql.password"));
             Statement maxIdStatement = connection.createStatement();
             ResultSet resultSet = maxIdStatement.executeQuery("SELECT MAX(id) AS max_id FROM Fights")) {

            if (resultSet.next()) {
                int maxId = resultSet.getInt("max_id");
                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE Fights SET" +
                                " regeneratedTeam1 = ?," +
                                " regeneratedTeam2 = ?" +
                                " WHERE id = ?")) {
                    updateStatement.setDouble(1, newRegeneratedTeam1);
                    updateStatement.setDouble(2, newRegeneratedTeam2);
                    updateStatement.setInt(3, maxId);
                    updateStatement.executeUpdate();
                }
            }
        } catch (SQLException exception) {
            getPlugin().getLogger().log(Level.SEVERE, "SQL error in regeneratedUpdate", exception);
        }
    }

    public static void winnersUpdate(String newWinners) {
        String url = buildUrl();
        if (url == null) return;

        try (Connection connection = DriverManager.getConnection(url,
                config.getString("mysql.username"), config.getString("mysql.password"));
             Statement maxIdStatement = connection.createStatement();
             ResultSet resultSet = maxIdStatement.executeQuery("SELECT MAX(id) AS max_id FROM Fights")) {

            if (resultSet.next()) {
                int maxId = resultSet.getInt("max_id");
                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE Fights SET winners = ? WHERE id = ?")) {
                    updateStatement.setString(1, newWinners);
                    updateStatement.setInt(2, maxId);
                    updateStatement.executeUpdate();
                }
            }
        } catch (SQLException exception) {
            getPlugin().getLogger().log(Level.SEVERE, "SQL error in winnersUpdate", exception);
        }
    }

    /**
     * Builds the JDBC connection URL for the ManualTournaments database.
     * Validates the host to prevent SQL injection via config.
     *
     * @return the JDBC URL string, or null if the host is invalid
     */
    private static String buildUrl() {
        String host = config.getString("mysql.url");
        if (host == null || host.trim().isEmpty()) {
            getPlugin().getLogger().severe("MySQL host is not configured in config.yml");
            return null;
        }

        // Basic hostname validation to prevent SQL injection via connection URL
        // Allows: hostnames, IPv4, IPv6 (simplified), with optional port
        if (!host.matches("^[a-zA-Z0-9]([a-zA-Z0-9\\-.:\\[\\]]*[a-zA-Z0-9])?$")) {
            getPlugin().getLogger().log(Level.SEVERE,
                    "MySQL host URL contains invalid characters: " + host);
            return null;
        }

        return String.format("jdbc:mysql://%s/ManualTournaments?useSSL=false&serverTimezone=UTC", host);
    }
}
