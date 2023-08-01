package net.flex.ManualTournaments.utils.gui.toolbar;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.utils.gui.menu.Menu;

public interface SGToolbarBuilder {

    /**
     * Specifies the toolbar button builder for an {@link Menu}.
     * This can be customized to render different toolbar buttonManaging for a gui.
     *
     * @param slot The slot being rendered.
     * @param page The current page of the inventory being rendered.
     * @param defaultType The default button type of the current slot.
     * @param menu The inventory the toolbar is being rendered in.
     * @return The button to be rendered for that slot, or null if no
     * button should be rendered.
     */
    Button buildToolbarButton(int slot, int page, SGToolbarButtonType defaultType, Menu menu);
}
