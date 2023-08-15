package net.flex.ManualTournaments.listeners;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.factories.ArenaFactory;
import net.flex.ManualTournaments.factories.KitFactory;
import net.flex.ManualTournaments.guis.ArenaGUI;
import net.flex.ManualTournaments.guis.KitGUI;
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
        String message = event.getMessage();
        if (!message.startsWith("*")) return;
        if (ArenaGUI.opener && player.hasPermission("mt.arena")) {
            handleArenaGUI(player, message);
            event.setCancelled(true);
        } else if (KitGUI.opener && player.hasPermission("mt.kit")) {
            handleKitGUI(player, message);
            event.setCancelled(true);
        }
    }

    private void handleArenaGUI(Player player, String message) {
        if (message.endsWith("cancel")) {
            ArenaGUI.opener = false;
            send(player, "gui-arena-creation-cancelled");
        } else {
            String arenaName = message.replace("*", "");
            ArenaFactory.getCommand("CREATE").execute(player, arenaName, Main.arenaNames.contains(arenaName));
            Bukkit.getScheduler().runTask(getPlugin(), () -> ArenaGUI.arenaGUI(player));
        }
    }

    private void handleKitGUI(Player player, String message) {
        if (message.endsWith("cancel")) {
            KitGUI.opener = false;
            send(player, "gui-kit-creation-cancelled");
        } else {
            String kitName = message.replace("*", "");
            KitFactory.getCommand("CREATE").execute(player, kitName, Main.kitNames.contains(kitName));
            Bukkit.getScheduler().runTask(getPlugin(), () -> KitGUI.kitGUI(player));
        }
    }
}

