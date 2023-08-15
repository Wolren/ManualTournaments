package net.flex.ManualTournaments.utils.gui;

import lombok.Getter;
import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.Menu;
import net.flex.ManualTournaments.utils.gui.menu.MenuListener;
import net.flex.ManualTournaments.utils.gui.toolbar.ToolbarBuilder;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class GUI {
    private final JavaPlugin plugin;
    private boolean blockDefaultInteractions = true;
    private boolean enableAutomaticPagination = true;

    @Getter
    private ToolbarBuilder defaultToolbarBuilder = (slot, page, type, menu) -> {
        switch (type) {
            case PREV_BUTTON:
                if (menu.getCurrentPage() > 0)
                    return new Button(new ItemBuilder(Material.ARROW)
                            .name("&a&l← Previous Page")
                            .lore("&aClick to move back to", "&apage " + menu.getCurrentPage() + ".")
                            .build()).withListener(event -> {
                        event.setCancelled(true);
                        menu.previousPage(event.getWhoClicked());
                    });
                else return null;
            case CURRENT_BUTTON:
                return new Button(new ItemBuilder(Material.NAME_TAG)
                        .name("&7&lPage " + (menu.getCurrentPage() + 1) + " of " + menu.getMaxPage())
                        .lore("&7You are currently viewing", "&7page " + (menu.getCurrentPage() + 1) + ".").build())
                        .withListener(event -> event.setCancelled(true));
            case NEXT_BUTTON:
                if (menu.getCurrentPage() < menu.getMaxPage() - 1)
                    return new Button(new ItemBuilder(Material.ARROW)
                            .name("&a&lNext Page →")
                            .lore("&aClick to move forward to", "&apage " + (menu.getCurrentPage() + 2) + ".")
                            .build()).withListener(event -> {
                        event.setCancelled(true);
                        menu.nextPage(event.getWhoClicked());
                    });
                else return null;
            case UNASSIGNED:
            default:
                return null;
        }
    };

    public GUI(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new MenuListener(plugin, this), plugin);
    }

    public Menu create(String name, int rows) {
        return new Menu(plugin, this, name, rows);
    }

    public void setBlockDefaultInteractions(boolean blockDefaultInteractions) {
        this.blockDefaultInteractions = blockDefaultInteractions;
    }

    public boolean areDefaultInteractionsBlocked() {
        return blockDefaultInteractions;
    }

    public void setEnableAutomaticPagination(boolean enableAutomaticPagination) {
        this.enableAutomaticPagination = enableAutomaticPagination;
    }

    public boolean isAutomaticPaginationEnabled() {
        return enableAutomaticPagination;
    }

    public void setDefaultToolbarBuilder(ToolbarBuilder defaultToolbarBuilder) {
        this.defaultToolbarBuilder = defaultToolbarBuilder;
    }
}
