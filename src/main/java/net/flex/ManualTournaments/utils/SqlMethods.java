package net.flex.ManualTournaments.utils;

import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.commands.Fight.*;
import static net.flex.ManualTournaments.utils.SharedMethods.teamList;

public class SqlMethods {
    private static final FileConfiguration config = getPlugin().getConfig();
    private static final String currentArena = config.getString("current-arena");
    private static final String currentKit = config.getString("current-kit");
    public static void sqlFights() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String host = config.getString("mysql.url");
            String username = config.getString("mysql.username");
            String password = config.getString("mysql.password");
            String url = String.format("jdbc:mysql://%s", host);
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS ManualTournaments");
            try (PreparedStatement useDatabaseStatement = connection.prepareStatement("USE ManualTournaments")) {
                useDatabaseStatement.execute();
            }
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ManualTournaments.Fights (id INT AUTO_INCREMENT, team1 TEXT, team2 TEXT, arena TEXT, kit TEXT, duration INT, PRIMARY KEY(id))");
            String sql = "INSERT INTO Fights(team1, team2, arena, kit, duration) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, teamList(team1, team1String));
            preparedStatement.setString(2, teamList(team2, team2String));
            preparedStatement.setString(3, currentArena);
            preparedStatement.setString(4, currentKit);
            preparedStatement.setInt(5, duration);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void durationUpdate(int id, int newDuration) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String host = config.getString("mysql.url");
            String username = config.getString("mysql.username");
            String password = config.getString("mysql.password");
            String url = String.format("jdbc:mysql://%s", host);
            Connection connection = DriverManager.getConnection(url, username, password);
            try (PreparedStatement useDatabaseStatement = connection.prepareStatement("USE ManualTournaments")) {
                useDatabaseStatement.execute();
            }
            String sql = "UPDATE Fights SET duration = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, newDuration);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void winnersUpdate() {

    }
}
