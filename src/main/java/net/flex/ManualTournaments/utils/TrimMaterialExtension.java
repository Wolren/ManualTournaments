package net.flex.ManualTournaments.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.jetbrains.annotations.NotNull;

public final class TrimMaterialExtension implements TrimMaterial {
    private final NamespacedKey materialKey;

    private TrimMaterialExtension(NamespacedKey materialKey) {
        this.materialKey = materialKey;
    }

    public static TrimMaterialExtension fromString(String materialName) {
        NamespacedKey key = NamespacedKey.minecraft(materialName);
        return new TrimMaterialExtension(key);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        if (isTrimMaterialAvailable()) {
            return ((TrimMaterial) this).getKey();
        } else {
            throw new UnsupportedOperationException("TrimMaterial is not available in this server version.");
        }
    }

    private boolean isTrimMaterialAvailable() {
        try {
            Class.forName("org.bukkit.inventory.meta.trim.TrimMaterial");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

