package net.flex.ManualTournaments.buttons.arenaButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.factories.ArenaFactory;
import net.flex.ManualTournaments.guis.ArenaGUI;
import net.flex.ManualTournaments.guis.ArenaSettingsGUI;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.arenaNames;
import static net.flex.ManualTournaments.utils.SharedComponents.config;

public class Pos1ArenaButton extends ButtonBuilder {
    public Pos1ArenaButton(Player sender, String arenaName, Menu menu) {
        super(sender, arenaName, menu);
    }

    @Override
    protected Button configureButton(Player sender, String name, Menu menu) {
        return new Button(new ItemBuilder(Material.MAP)
                .name(config.getString("gui-arena-settings-pos1-name"))
                .lore(new ArenaGUI().getLore("Arenas." + name + ".pos1."))
                .build()).withListener(event1 -> {
            ArenaFactory.getCommand("POS1").execute(sender, name, arenaNames.contains(name));
            sender.openInventory(ArenaGUI.arenaMenu.getInventory());
            ArenaSettingsGUI.arenaSettingsGUI(sender, name);
        });
    }
}
