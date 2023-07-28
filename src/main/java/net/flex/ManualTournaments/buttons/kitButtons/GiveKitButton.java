package net.flex.ManualTournaments.buttons.kitButtons;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.factories.KitFactory;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;

public class GiveKitButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();

    public GiveKitButton(Player sender, String kitName) {
        super(sender, kitName);
    }

    @Override
    protected Button configureButton(Player sender, String name) {
        return new Button(new ItemBuilder(Material.SHEARS)
                .name(config.getString("gui-kit-settings-give-name"))
                .lore(config.getString("gui-kit-settings-give-lore"))
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        KitFactory.getCommand("GIVE").execute(sender, name, Main.kitNames.contains(name));
                    }
                });
    }
}
