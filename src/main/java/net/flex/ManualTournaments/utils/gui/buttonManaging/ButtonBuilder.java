package net.flex.ManualTournaments.utils.gui.buttonManaging;

import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.entity.Player;

public abstract class ButtonBuilder {
    protected Button button;

    protected Button configureButton(Player sender) {
        throw new UnsupportedOperationException("You used the wrong overload");
    }

    protected Button configureButton(Player sender, String arenaName) {
        throw new UnsupportedOperationException("You used the wrong overload");
    }

    protected Button configureButton(Player sender, String arenaName, SGMenu menu) {
        throw new UnsupportedOperationException("You used the wrong overload");
    }

    public ButtonBuilder(Player sender) {
        button = configureButton(sender);
    }

    public ButtonBuilder(Player sender, String arenaName) {
        button = configureButton(sender, arenaName);
    }

    public ButtonBuilder(Player sender, String arenaName, SGMenu menu) {
        button = configureButton(sender, arenaName, menu);
    }

    public Button buildButton() {
        return button;
    }
}

