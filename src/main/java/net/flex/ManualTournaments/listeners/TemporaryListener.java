package net.flex.ManualTournaments.listeners;

import net.flex.ManualTournaments.commands.fightCommands.DefaultFight;
import net.flex.ManualTournaments.events.PlayerJumpEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.clear;
import static net.flex.ManualTournaments.utils.SharedComponents.location;

public class TemporaryListener implements Listener {
    @EventHandler
    private void onJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        if (DefaultFight.temporary.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (DefaultFight.temporary.contains(player.getUniqueId())) {
            Location from = event.getFrom();
            if (from.getX() != Objects.requireNonNull(event.getTo()).getX() || from.getY() != event.getTo().getY()) {
                player.teleport(from);
            }
        }
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (DefaultFight.temporary.contains(player.getUniqueId())) {
            if (getPlugin().getConfig().getBoolean("kill-on-fight-end")) {
                player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                player.setHealth(0);
                player.setWalkSpeed(0.2f);
            } else {
                String path = "fight-end-spawn.";
                if (getPlugin().getConfig().isSet(path)) {
                    player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                    clear(player);
                    player.teleport(location(path, getPlugin().getConfig()));
                    player.setWalkSpeed(0.2f);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (DefaultFight.temporary.contains(player.getUniqueId())) event.setCancelled(true);
    }
}
