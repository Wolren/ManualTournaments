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

public class TeleportArenaButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();
    public TeleportArenaButton(Player sender, String arenaName) {
        super(sender, arenaName);
    }
    @Override
    protected Button configureButton(Player sender, String arenaName) {
        return new Button(new ItemBuilder(Material.COMPASS)
                .name(config.getString("gui-arena-settings-teleport-name"))
                .lore(config.getString("gui-arena-settings-teleport-lore"))
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        sender.closeInventory();
                        getPlugin();
                        ArenaFactory.getCommand("TELEPORT").execute(sender, arenaName, arenaNames.contains(arenaName));
                    }
                });
    }
}
