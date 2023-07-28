package net.flex.ManualTournaments.buttons.arenaButtons;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.factories.ArenaFactory;
import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;

public class ValidateArenaButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();

    public ValidateArenaButton(Player sender, String arenaName) {
        super(sender, arenaName);
    }

    @Override
    protected Button configureButton(Player sender, String name) {
        return new Button(new ItemBuilder(Material.SHEARS)
                .name(config.getString("gui-arena-settings-validate-name"))
                .lore(config.getString("gui-arena-settings-validate-lore"))
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        ArenaFactory.getCommand("VALIDATE").execute(sender, name, Main.arenaNames.contains(name));
                    }
                });
    }
}
