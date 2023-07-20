package net.flex.ManualTournaments.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import static net.flex.ManualTournaments.commands.Spectate.spectators;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class SpectateListener implements Listener {
    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    private void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (spectators.contains(player.getUniqueId())) {
            if (event.getMessage().startsWith("/spec") || event.getMessage().contains("spectate") || event.getMessage().startsWith("/mt_spec") || config.getStringList("spectator-allowed-commands").contains(event.getMessage()) || player.isOp()) {
                event.setCancelled(false);
            } else {
                player.sendMessage(message("not-allowed"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (spectators.contains(player.getUniqueId())) event.setAmount(0);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (spectators.contains(player.getUniqueId())) {
            if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getMaterial() == Material.RED_DYE && event.getHand() == EquipmentSlot.HAND) {
                player.performCommand("spectate stop");
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (spectators.contains(player.getUniqueId())) {
            if (config.getBoolean("kill-on-fight-end")) {
                player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                player.setHealth(0);
            } else {
                String path = "fight-end-spawn.";
                if (config.isSet(path)) {
                    player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                    clear(player);
                    player.teleport(location(path, config));
                }
            }
        }
    }
}
