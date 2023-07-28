package net.flex.ManualTournaments.buttons.kitButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.guis.KitGUI;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class CreateKitButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();

    public CreateKitButton(Player sender) {
        super(sender);
    }

    @Override
    protected Button configureButton(Player sender) {
        return new Button(new ItemBuilder(Material.EMERALD_BLOCK)
                .name(config.getString("gui-kit-create-name"))
                .lore(config.getString("gui-kit-create-lore")).build()).withListener(event -> {
            if (event.isLeftClick()) {
                sender.closeInventory();
                send(sender, "gui-kit-create-message");
                KitGUI.opener = true;
            }
        });
    }
}
