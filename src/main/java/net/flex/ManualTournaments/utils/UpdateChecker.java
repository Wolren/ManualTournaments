package net.flex.ManualTournaments.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.flex.ManualTournaments.Main.getPlugin;

public final class UpdateChecker {
    private String version;
    private final Logger logger = getPlugin().getLogger();
    private final CompletableFuture<Boolean> updateFuture;

    /**
     * Creates an UpdateChecker that automatically checks for updates on construction.
     * The returned CompletableFuture can be tracked by Main to cancel on plugin disable.
     *
     * @param callback optional callback invoked with the check result (true = update available)
     */
    public UpdateChecker(Consumer<Boolean> callback) {
        this.updateFuture = checkUpdate();
        updateFuture.thenAcceptAsync(result -> {
            if (callback != null) {
                callback.accept(result);
            }
            if (result) {
                updateAvailable();
            }
        });
    }

    /**
     * Creates an UpdateChecker that automatically checks for updates on construction.
     * Use the overload accepting a callback or track the CompletableFuture via Main.
     */
    public UpdateChecker() {
        this(null);
    }

    private void updateAvailable() {
        logger.log(Level.WARNING, "A new Version [v" + version + "] is available!");
        logger.log(Level.WARNING, "https://www.spigotmc.org/resources/manual-tournaments.105850/");
    }

    /**
     * Returns the CompletableFuture for the update check, so callers (e.g. Main)
     * can track and cancel it on plugin disable if needed.
     */
    public CompletableFuture<Boolean> getUpdateFuture() {
        return updateFuture;
    }

    public CompletableFuture<Boolean> checkUpdate() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        logger.log(Level.INFO, "Checking for Updates...");
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=105850");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() != 200) {
                result.complete(false);
            } else {
                // Specify UTF-8 charset to ensure proper encoding of the response
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                version = reader.readLine();
                result.complete(!getPlugin().getDescription().getVersion().equalsIgnoreCase(version));
                reader.close();
            }
            return result;
        } catch (IOException exception) {
            logger.log(Level.WARNING, "Could not check for updates", exception);
            result.complete(false);
            return result;
        }
    }
}
