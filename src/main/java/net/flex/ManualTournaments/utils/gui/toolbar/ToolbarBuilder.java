package net.flex.ManualTournaments.utils.gui.toolbar;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.utils.gui.menu.Menu;

public interface ToolbarBuilder {
    Button buildToolbarButton(int slot, int page, ToolbarButtonType defaultType, Menu menu);
}
