package net.flex.ManualTournaments.utils.gui.buttons;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.IOException;

public interface ButtonListener {
    void onClick(InventoryClickEvent event) throws IOException;
}
