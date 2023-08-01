package net.flex.ManualTournaments.utils.gui.menu;

import org.bukkit.entity.Player;

public class SGOpenMenu {

    private final Menu gui;

    private final Player player;

    public SGOpenMenu(Menu gui, Player player) {
        this.gui = gui;
        this.player = player;
    }

    public Menu getMenu() {
        return this.gui;
    }

    public Player getPlayer() {
        return this.player;
    }
}
