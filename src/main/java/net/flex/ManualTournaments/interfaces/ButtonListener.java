package net.flex.ManualTournaments.interfaces;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.IOException;

public interface ButtonListener {
    void onClick(InventoryClickEvent event) throws IOException, InvalidConfigurationException;
}
