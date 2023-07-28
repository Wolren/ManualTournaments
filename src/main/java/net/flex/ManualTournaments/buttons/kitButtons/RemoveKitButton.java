package net.flex.ManualTournaments.buttons.kitButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.factories.KitFactory;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.Main.kitNames;
import static net.flex.ManualTournaments.guis.KitGUI.kitGUI;

public class RemoveKitButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();

    public RemoveKitButton(Player sender, String arenaName) {
        super(sender, arenaName);
    }

    @Override
    protected Button configureButton(Player sender, String name) {
        return new Button(new ItemBuilder(Material.REDSTONE_BLOCK)
                .name(config.getString("gui-kit-settings-remove-name"))
                .lore(config.getString("gui-kit-settings-remove-lore"))
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        sender.closeInventory();
                        KitFactory.getCommand("REMOVE").execute(sender, name, kitNames.contains(name));
                        kitGUI(sender);
                    }
                });
    }
}
