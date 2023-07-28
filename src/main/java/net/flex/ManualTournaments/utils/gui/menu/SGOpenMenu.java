package net.flex.ManualTournaments.utils.gui.menu;

import org.bukkit.entity.Player;

public class SGOpenMenu {

    private final SGMenu gui;

    private final Player player;

    public SGOpenMenu(SGMenu gui, Player player) {
        this.gui = gui;
        this.player = player;
    }

    public SGMenu getMenu() {
        return this.gui;
    }

    public Player getPlayer() {
        return this.player;
    }
}
