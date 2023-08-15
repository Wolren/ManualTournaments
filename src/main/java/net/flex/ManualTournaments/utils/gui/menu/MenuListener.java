package net.flex.ManualTournaments.utils.gui.menu;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.utils.gui.GUI;
import net.flex.ManualTournaments.utils.gui.toolbar.ToolbarBuilder;
import net.flex.ManualTournaments.utils.gui.toolbar.ToolbarButtonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class MenuListener implements Listener {

    private final JavaPlugin owner;
    private final GUI spiGUI;

    public MenuListener(JavaPlugin owner, GUI spiGUI) {
        this.owner = owner;
        this.spiGUI = spiGUI;
    }

    private static boolean shouldIgnoreInventoryEvent(Inventory inventory) {
        return !(inventory != null && inventory.getHolder() != null && inventory.getHolder() instanceof Menu);
    }

    @SneakyThrows
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws IOException {
        if (shouldIgnoreInventoryEvent(event.getClickedInventory())) return;
        Menu clickedGui = (Menu) event.getClickedInventory().getHolder();
        if (clickedGui != null) {
            if (!clickedGui.getOwner().equals(owner)) return;
            if (clickedGui.areDefaultInteractionsBlocked() != null) {
                event.setCancelled(clickedGui.areDefaultInteractionsBlocked());
            } else {
                if (spiGUI.areDefaultInteractionsBlocked()) event.setCancelled(true);
            }
            if (event.getSlot() > clickedGui.getPageSize()) {
                int offset = event.getSlot() - clickedGui.getPageSize();
                ToolbarBuilder paginationButtonBuilder = spiGUI.getDefaultToolbarBuilder();
                if (clickedGui.getToolbarBuilder() != null) {
                    paginationButtonBuilder = clickedGui.getToolbarBuilder();
                }
                ToolbarButtonType buttonType = ToolbarButtonType.getDefaultForSlot(offset);
                Button paginationButton = paginationButtonBuilder.buildToolbarButton(offset, clickedGui.getCurrentPage(), buttonType, clickedGui);
                if (paginationButton != null) paginationButton.getListener().onClick(event);
                return;
            }
            if (clickedGui.isStickiedSlot(event.getSlot())) {
                Button button = clickedGui.getButton(0, event.getSlot());
                if (button != null && button.getListener() != null) button.getListener().onClick(event);
                return;
            }
            Button button = clickedGui.getButton(clickedGui.getCurrentPage(), event.getSlot());
            if (button != null && button.getListener() != null) {
                button.getListener().onClick(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (shouldIgnoreInventoryEvent(event.getInventory())) return;
        event.setCancelled(true);
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (shouldIgnoreInventoryEvent(event.getInventory())) return;
        Menu clickedGui = (Menu) event.getInventory().getHolder();
        if (!clickedGui.getOwner().equals(owner)) return;
    }
}
