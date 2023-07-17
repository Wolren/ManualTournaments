package net.flex.ManualTournaments.utils;

import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.commands.Fight.*;
import static net.flex.ManualTournaments.utils.SharedMethods.teamList;

public class SqlMethods {
    private static final FileConfiguration config = getPlugin().getConfig();
    private static final String currentArena = config.getString("current-arena");
    private static final String currentKit = config.getString("current-kit");

    public static void sqlFights() {
        try {
            Connection connection = load();
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS ManualTournaments");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ManualTournaments.Fights" +
                    " (id INT AUTO_INCREMENT," +
                    " team1 VARCHAR(1000), team2 VARCHAR(1000)," +
                    " damageTeam1 DOUBLE, damageTeam2 DOUBLE," +
                    " regeneratedTeam1 DOUBLE, regeneratedTeam2 DOUBLE," +
                    " arena VARCHAR(50), kit VARCHAR(50)," +
                    " duration INT," +
                    " winners VARCHAR(1000), PRIMARY KEY(id))");
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Fights(" +
                    "team1, team2," +
                    " damageTeam1, damageTeam2," +
                    " regeneratedTeam1, regeneratedTeam2," +
                    " arena, kit," +
                    " duration," +
                    " winners)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, teamList(team1, team1String));
            team1String.clear();
            preparedStatement.setString(2, teamList(team2, team2String));
            team2String.clear();
            preparedStatement.setDouble(3, 0);
            preparedStatement.setDouble(4, 0);
            preparedStatement.setDouble(5, 0);
            preparedStatement.setDouble(6, 0);
            preparedStatement.setString(7, currentArena);
            preparedStatement.setString(8, currentKit);
            preparedStatement.setInt(9, duration);
            preparedStatement.setString(10, "");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void durationUpdate(int newDuration) {
        try {
            Connection connection = load();
            Statement maxIdStatement = connection.createStatement();
            ResultSet resultSet = maxIdStatement.executeQuery("SELECT MAX(id) AS max_id FROM Fights");
            if (resultSet.next()) {
                int maxId = resultSet.getInt("max_id");
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE Fights SET duration = ? WHERE id = ?");
                updateStatement.setInt(1, newDuration);
                updateStatement.setInt(2, maxId);
                updateStatement.executeUpdate();
                updateStatement.close();
            }
            resultSet.close();
            maxIdStatement.close();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void damageUpdate(double newDamageTeam1, double newDamageTeam2) {
        try {
            Connection connection = load();
            Statement maxIdStatement = connection.createStatement();
            ResultSet resultSet = maxIdStatement.executeQuery("SELECT MAX(id) AS max_id FROM Fights");
            if (resultSet.next()) {
                int maxId = resultSet.getInt("max_id");
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE Fights SET" +
                        " damageTeam1 = ?," +
                        " damageTeam2 = ?" +
                        " WHERE id = ?");
                updateStatement.setDouble(1, newDamageTeam1);
                updateStatement.setDouble(2, newDamageTeam2);
                updateStatement.setInt(3, maxId);
                updateStatement.executeUpdate();
                updateStatement.close();
            }
            resultSet.close();
            maxIdStatement.close();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void regeneratedUpdate(double newRegeneratedTeam1, double newRegeneratedTeam2) {
        try {
            Connection connection = load();
            Statement maxIdStatement = connection.createStatement();
            ResultSet resultSet = maxIdStatement.executeQuery("SELECT MAX(id) AS max_id FROM Fights");
            if (resultSet.next()) {
                int maxId = resultSet.getInt("max_id");
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE Fights SET" +
                        " regeneratedTeam1 = ?," +
                        " regeneratedTeam2 = ?" +
                        " WHERE id = ?");
                updateStatement.setDouble(1, newRegeneratedTeam1);
                updateStatement.setDouble(2, newRegeneratedTeam2);
                updateStatement.setInt(3, maxId);
                updateStatement.executeUpdate();
                updateStatement.close();
            }
            resultSet.close();
            maxIdStatement.close();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void winnersUpdate(String newWinners) {
        try {
            Connection connection = load();
            Statement maxIdStatement = connection.createStatement();
            ResultSet resultSet = maxIdStatement.executeQuery("SELECT MAX(id) AS max_id FROM Fights");
            if (resultSet.next()) {
                int maxId = resultSet.getInt("max_id");
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE Fights SET winners = ? WHERE id = ?");
                updateStatement.setString(1, newWinners);
                updateStatement.setInt(2, maxId);
                updateStatement.executeUpdate();
                updateStatement.close();
            }
            resultSet.close();
            maxIdStatement.close();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static Connection load() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String host = config.getString("mysql.url");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");
        String url = String.format("jdbc:mysql://%s", host);
        Connection connection = DriverManager.getConnection(url, username, password);
        try (PreparedStatement useDatabaseStatement = connection.prepareStatement("USE ManualTournaments")) {
            useDatabaseStatement.execute();
        }
        return connection;
    }
}
