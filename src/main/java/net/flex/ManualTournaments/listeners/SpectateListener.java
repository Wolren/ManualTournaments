package net.flex.ManualTournaments.listeners;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.Spectate;
import net.flex.ManualTournaments.guis.SpectatorGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.commands.Spectate.spectators;
import static net.flex.ManualTournaments.commands.Spectate.stopSpectator;
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
    private void onEntityInteract(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    private void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLivingEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player) {
            Player player = (Player) event.getTarget();
            if (spectators.contains(player.getUniqueId())) event.setCancelled(true);
        }
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
            if (event.getMessage().startsWith("/spec") || event.getMessage().contains("spectate") || event.getMessage().startsWith("/mt_spec")
                    || getPlugin().getConfig().getStringList("spectator-allowed-commands").contains(event.getMessage()) || player.isOp()) {
                event.setCancelled(false);
            } else {
                player.sendMessage(message("not-allowed"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (spectators.contains(player.getUniqueId())) {
            event.setDeathMessage(null);
            player.setGameMode(Bukkit.getServer().getDefaultGameMode());
            if (Main.version <= 13) collidableReflection(player, true);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.getInventory().clear();
            Bukkit.getServer().getOnlinePlayers().forEach(other -> other.showPlayer(player));
            Spectate.spectatorsBoard.removeEntry(player.getName());
            if (Main.version >= 14) player.setCollidable(true);
            spectators.remove(player.getUniqueId());
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
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (event.getMaterial() == Material.REDSTONE_BLOCK) {
                    stopSpectator(player);
                } else if (event.getMaterial() == Material.COMPASS) {
                    SpectatorGUI.teleportationGUI(player);
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (spectators.contains(player.getUniqueId())) {
            if (event.isLeftClick()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getServer().getOnlinePlayers().stream().filter(other -> spectators.contains(other.getUniqueId())).forEachOrdered(other -> {
            SpectatorGUI.spectatorMenu.refreshInventory(other);
            player.hidePlayer(other);
        });
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);
        if (spectators.contains(player.getUniqueId())) {
            if (getPlugin().getConfig().getBoolean("kill-on-fight-end")) {
                player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                player.setHealth(0);
            } else {
                String path = "fight-end-spawn.";
                if (getPlugin().getConfig().isSet(path)) {
                    player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                    clear(player);
                    player.teleport(location(path, getPlugin().getConfig()));
                }
            }
        }
        Spectate.stopSpectator(player);
    }
}
