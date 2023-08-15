package net.flex.ManualTournaments.utils.gui.menu;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.utils.gui.GUI;
import net.flex.ManualTournaments.utils.gui.toolbar.ToolbarBuilder;
import net.flex.ManualTournaments.utils.gui.toolbar.ToolbarButtonType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Menu implements InventoryHolder {
    private final JavaPlugin owner;
    private final GUI spiGUI;
    private final String name;
    private final int rowsPerPage;
    private final Map<Integer, Button> items;
    private final HashSet<Integer> stickiedSlots;
    private int currentPage;
    private Boolean blockDefaultInteractions;
    private Boolean enableAutomaticPagination;
    private ToolbarBuilder toolbarBuilder;

    public Menu(JavaPlugin owner, GUI spiGUI, String name, int rowsPerPage) {
        this.owner = owner;
        this.spiGUI = spiGUI;
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.rowsPerPage = rowsPerPage;

        this.items = new HashMap<>();
        this.stickiedSlots = new HashSet<>();

        this.currentPage = 0;
    }

    public void setBlockDefaultInteractions(boolean blockDefaultInteractions) {
        this.blockDefaultInteractions = blockDefaultInteractions;
    }

    public Boolean areDefaultInteractionsBlocked() {
        return blockDefaultInteractions;
    }

    public void setAutomaticPaginationEnabled(boolean enableAutomaticPagination) {
        this.enableAutomaticPagination = enableAutomaticPagination;
    }

    public Boolean isAutomaticPaginationEnabled() {
        return enableAutomaticPagination;
    }

    public ToolbarBuilder getToolbarBuilder() {
        return this.toolbarBuilder;
    }

    public void setToolbarBuilder(ToolbarBuilder toolbarBuilder) {
        this.toolbarBuilder = toolbarBuilder;
    }

    public JavaPlugin getOwner() {
        return owner;
    }

    public int getPageSize() {
        return rowsPerPage * 9;
    }

    public void addButton(Button button) {
        if (getHighestFilledSlot() == 0 && getButton(0) == null) {
            setButton(0, button);
            return;
        }
        setButton(getHighestFilledSlot() + 1, button);
    }

    public void setButton(int slot, Button button) {
        items.put(slot, button);
    }

    public Button getButton(int slot) {
        if (slot < 0 || slot > getHighestFilledSlot()) return null;
        return items.get(slot);
    }

    public Button getButton(int page, int slot) {
        if (slot < 0 || slot > getPageSize()) return null;
        return getButton((page * getPageSize()) + slot);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getMaxPage() {
        return (int) Math.ceil(((double) getHighestFilledSlot() + 1) / ((double) getPageSize()));
    }

    public int getHighestFilledSlot() {
        int slot = 0;
        for (int nextSlot : items.keySet()) {
            if (items.get(nextSlot) != null && nextSlot > slot) {
                slot = nextSlot;
            }
        }
        return slot;
    }

    public void nextPage(HumanEntity viewer) {
        if (currentPage < getMaxPage() - 1) {
            currentPage++;
            refreshInventory(viewer);
        }
    }

    public void previousPage(HumanEntity viewer) {
        if (currentPage > 0) {
            currentPage--;
            refreshInventory(viewer);
        }
    }

    public boolean isStickiedSlot(int slot) {
        if (slot < 0 || slot >= getPageSize()) return false;
        return this.stickiedSlots.contains(slot);
    }

    public void clearAllButStickiedSlots() {
        this.currentPage = 0;
        items.entrySet().removeIf(item -> !isStickiedSlot(item.getKey()));
    }

    public void refreshInventory(HumanEntity viewer) {
        if (!(viewer.getOpenInventory().getTopInventory().getHolder() instanceof Menu) || viewer.getOpenInventory().getTopInventory().getHolder() != this) {
            return;
        }
        if (viewer.getOpenInventory().getTopInventory().getSize() != getPageSize() + (getMaxPage() > 0 ? 9 : 0)) {
            viewer.openInventory(getInventory());
            return;
        }
        String newName = name.replace("{currentPage}", String.valueOf(currentPage + 1)).replace("{maxPage}", String.valueOf(getMaxPage()));
        if (!viewer.getOpenInventory().getTitle().equals(newName)) {
            viewer.openInventory(getInventory());
            return;
        }
        viewer.getOpenInventory().getTopInventory().setContents(getInventory().getContents());
    }

    @Override
    public @NotNull Inventory getInventory() {
        boolean isAutomaticPaginationEnabled = spiGUI.isAutomaticPaginationEnabled();
        if (isAutomaticPaginationEnabled() != null) {
            isAutomaticPaginationEnabled = isAutomaticPaginationEnabled();
        }
        boolean needsPagination = getMaxPage() > 0 && isAutomaticPaginationEnabled;
        Inventory inventory = Bukkit.createInventory(this, ((needsPagination) ? getPageSize() + 9 : getPageSize()),
                name.replace("{currentPage}", String.valueOf(currentPage + 1)).replace("{maxPage}", String.valueOf(getMaxPage())));
        for (int key = currentPage * getPageSize(); key < (currentPage + 1) * getPageSize(); key++) {
            if (key > getHighestFilledSlot()) break;
            if (items.containsKey(key)) {
                inventory.setItem(key - (currentPage * getPageSize()), items.get(key).getIcon());
            }
        }
        stickiedSlots.forEach(stickiedSlot -> inventory.setItem(stickiedSlot, items.get(stickiedSlot).getIcon()));
        if (needsPagination) {
            ToolbarBuilder toolbarButtonBuilder = spiGUI.getDefaultToolbarBuilder();
            if (getToolbarBuilder() != null) {
                toolbarButtonBuilder = getToolbarBuilder();
            }
            int pageSize = getPageSize();
            for (int i = pageSize; i < pageSize + 9; i++) {
                int offset = i - pageSize;
                Button paginationButton = toolbarButtonBuilder.buildToolbarButton(offset, getCurrentPage(), ToolbarButtonType.getDefaultForSlot(offset), this);
                inventory.setItem(i, paginationButton != null ? paginationButton.getIcon() : null);
            }
        }

        return inventory;
    }
}