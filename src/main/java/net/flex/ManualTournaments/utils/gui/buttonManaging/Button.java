package net.flex.ManualTournaments.utils.gui.buttonManaging;

import net.flex.ManualTournaments.interfaces.ButtonListener;
import org.bukkit.inventory.ItemStack;

/**
 * An SGButton represents a clickable item in an SGMenu (GUI).
 * It consists of an icon ({@link ItemStack}) and a listener ({@link Button}).
 * <p>
 * When the icon is clicked in the SGMenu, the listener is called, thus allowing
 * for rudimentary menus to be built by displaying icons and overriding their behavior.
 * <p>
 * This somewhat resembles the point-and-click nature of Graphical User Interfaces (GUIs)
 * popularized by Operating Systems developed in the late 80s and 90s which is where the
 * name of the concept in Spigot plugins was derived.
 */
public class Button {

    private ButtonListener listener;
    private ItemStack icon;

    /**
     * Creates an SGButton with the specified {@link ItemStack} as it's 'icon' in the inventory.
     *
     * @param icon The desired 'icon' for the SGButton.
     */
    public Button(ItemStack icon){
        this.icon = icon;
    }

    /**
     * Sets the {@link ButtonListener} to be called when the button is clicked.
     * @param listener The listener to be called when the button is clicked.
     */
    public void setListener(ButtonListener listener) {
        this.listener = listener;
    }

    /**
     * A chainable alias of {@link #setListener(ButtonListener)}.
     *
     * @param listener The listener to be called when the button is clicked.
     * @return The {@link Button} the listener was applied to.
     */
    public Button withListener(ButtonListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Returns the {@link ButtonListener} that is to be executed when the button
     * is clicked.<br>
     * This is typically intended for use by the API.
     *
     * @return The listener to be called when the button is clicked.
     */
    public ButtonListener getListener() {
        return listener;
    }

    /**
     * Returns the {@link ItemStack} that will be used as the SGButton's icon in the
     * SGMenu (GUI).
     *
     * @return The icon ({@link ItemStack}) that will be used to represent the button.
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * Changes the SGButton's icon.
     *
     * @param icon The icon ({@link ItemStack}) that will be used to represent the button.
     */
    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }
}
