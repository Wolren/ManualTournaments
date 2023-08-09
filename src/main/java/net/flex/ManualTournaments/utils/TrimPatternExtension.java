package net.flex.ManualTournaments.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;

public final class TrimPatternExtension implements TrimPattern {
    private NamespacedKey patternKey;

    private TrimPatternExtension(NamespacedKey patternKey) {
        this.patternKey = patternKey;
    }

    public static TrimPatternExtension fromString(String patternName) {
        NamespacedKey key = NamespacedKey.minecraft(patternName);
        return new TrimPatternExtension(key);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        if (isTrimPatternAvailable()) {
            return ((TrimPattern) this).getKey();
        } else {
            throw new UnsupportedOperationException("TrimMaterial is not available in this server version.");
        }
    }

    private boolean isTrimPatternAvailable() {
        try {
            Class.forName("org.bukkit.inventory.meta.trim.TrimMaterial");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
