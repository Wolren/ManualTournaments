package net.flex.ManualTournaments.utils.gui.buttonManaging;

import lombok.Getter;
import net.flex.ManualTournaments.interfaces.ButtonListener;
import org.bukkit.inventory.ItemStack;

public class Button {
    @Getter
    private ButtonListener listener;

    @Getter
    private ItemStack icon;

    public Button(ItemStack icon) {
        this.icon = icon;
    }

    public void setListener(ButtonListener listener) {
        this.listener = listener;
    }

    public Button withListener(ButtonListener listener) {
        this.listener = listener;
        return this;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }
}
