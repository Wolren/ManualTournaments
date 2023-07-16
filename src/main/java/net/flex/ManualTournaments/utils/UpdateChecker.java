package net.flex.ManualTournaments.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static net.flex.ManualTournaments.Main.*;

public final class UpdateChecker {
    private String version;

    public UpdateChecker() {
        checkUpdate().thenAcceptAsync(result -> {
            if (result) updateAvailable();
        });
    }

    private void updateAvailable() {
        getPlugin().getLogger().log(Level.WARNING, "A new Version [v" + version + "] is available!");
        getPlugin().getLogger().log(Level.WARNING, "https://www.spigotmc.org/resources/manual-tournaments.105850/");
    }

    private CompletableFuture<Boolean> checkUpdate() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        getPlugin().getLogger().log(Level.INFO, "Checking for Updates...");
        String versionString = getPlugin().getDescription().getVersion();
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=105850");
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() != 200) result.complete(false);
            else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                version = reader.readLine();
                result.complete(!versionString.equalsIgnoreCase(version));
            }
            return result;
        } catch (IOException exception) {
            result.complete(false);
            return result;
        }
    }
}
