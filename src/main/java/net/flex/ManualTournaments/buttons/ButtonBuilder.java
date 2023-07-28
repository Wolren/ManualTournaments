package net.flex.ManualTournaments.buttons;

import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.entity.Player;

public abstract class ButtonBuilder {
    protected Button button;

    protected Button configureButton(Player sender) {
       return null;
    }

    protected Button configureButton(Player sender, String name) {
        return null;
    }

    protected Button configureButton(Player sender, String name, SGMenu menu) {
        return null;
    }

    public ButtonBuilder(Player sender) {
        button = configureButton(sender);
    }

    public ButtonBuilder(Player sender, String name) {
        button = configureButton(sender, name);
    }

    public ButtonBuilder(Player sender, String name, SGMenu menu) {
        button = configureButton(sender, name, menu);
    }

    public Button buildButton() {
        return button;
    }
}

