package net.flex.ManualTournaments.buttons;

import net.flex.ManualTournaments.interfaces.ButtonListener;
import org.bukkit.inventory.ItemStack;

public class Button {
    private ButtonListener listener;
    private ItemStack icon;

    public Button(ItemStack icon) {
        this.icon = icon;
    }

    public Button withListener(ButtonListener listener) {
        this.listener = listener;
        return this;
    }

    public ButtonListener getListener() {
        return listener;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }
}
