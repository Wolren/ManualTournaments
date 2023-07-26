package net.flex.ManualTournaments.listeners;

import net.flex.ManualTournaments.factories.ArenaFactory;
import net.flex.ManualTournaments.guis.ArenaGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class GUIListener implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (ArenaGUI.opener && player.hasPermission("mt.arena")) {
            String message = event.getMessage();
            if (message.startsWith("*")) {
                if (message.endsWith("cancel")) {
                    ArenaGUI.opener = false;
                    send(player, "gui-arena-creation-cancelled");
                } else {
                    String arenaName = message.replace("*", "");
                    ArenaGUI.newArenaName = arenaName;
                    ArenaFactory.getCommand("CREATE").execute(player, arenaName, getPlugin().arenaNames.contains(arenaName));
                    Bukkit.getScheduler().runTask(getPlugin(), () -> ArenaGUI.arenaGUI(player));
                }
                event.setCancelled(true);
            }
        }
    }
}
