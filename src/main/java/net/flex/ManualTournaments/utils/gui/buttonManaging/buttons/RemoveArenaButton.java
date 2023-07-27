package net.flex.ManualTournaments.utils.gui.buttonManaging.buttons;

import net.flex.ManualTournaments.factories.ArenaFactory;
import net.flex.ManualTournaments.utils.gui.buttonManaging.Button;
import net.flex.ManualTournaments.utils.gui.buttonManaging.ButtonBuilder;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.arenaNames;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.guis.ArenaGUI.arenaGUI;

public class RemoveArenaButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();

    public RemoveArenaButton(Player sender, String arenaName) {
        super(sender, arenaName);
    }

    @Override
    protected Button configureButton(Player sender, String arenaName) {
        return new Button(new ItemBuilder(Material.REDSTONE_BLOCK)
                .name(config.getString("gui-arena-settings-remove-name"))
                .lore(config.getString("gui-arena-settings-remove-lore"))
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        sender.closeInventory();
                        ArenaFactory.getCommand("REMOVE").execute(sender, arenaName, arenaNames.contains(arenaName));
                        arenaGUI(sender);
                    }
                });
    }
}
