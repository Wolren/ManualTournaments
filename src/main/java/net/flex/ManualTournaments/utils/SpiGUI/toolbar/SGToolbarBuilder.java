package net.flex.ManualTournaments.utils.SpiGUI.toolbar;

import net.flex.ManualTournaments.utils.SpiGUI.buttons.SGButton;
import net.flex.ManualTournaments.utils.SpiGUI.menu.SGMenu;

public interface SGToolbarBuilder {

    /**
     * Specifies the toolbar button builder for an {@link SGMenu}.
     * This can be customized to render different toolbar buttons for a GUI.
     *
     * @param slot The slot being rendered.
     * @param page The current page of the inventory being rendered.
     * @param defaultType The default button type of the current slot.
     * @param menu The inventory the toolbar is being rendered in.
     * @return The button to be rendered for that slot, or null if no
     * button should be rendered.
     */
    SGButton buildToolbarButton(int slot, int page, SGToolbarButtonType defaultType, SGMenu menu);

}
