package net.flex.ManualTournaments;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.events.PlayerJumpEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.General.message;
import static net.flex.ManualTournaments.utils.Locations.location;


final class MyListener implements Listener {
    static FileConfiguration config = getPlugin().getConfig();
    static int stopper;
    static boolean clearing = false;

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        removeEntries();
        if (Fight.team1.contains(player.getUniqueId()) || Fight.team2.contains(player.getUniqueId())) {
            event.setDroppedExp(0);
            if (!config.getBoolean("drop-on-death")) event.getDrops().clear();
        }
        teamRemover(player, Fight.team1, Fight.team2);
        teamRemover(player, Fight.team2, Fight.team1);
        if (config.getBoolean("create-fights-folder")) endCounter();
    }

    @SneakyThrows
    private void endCounter() {
        if (Fight.team1.isEmpty() && Fight.team2.isEmpty()) {
            Fight.FightsConfig.load(Fight.FightsConfigFile);
            Fight.FightsConfig.set("net.flex.ManualTournaments.Fight-duration", Fight.duration - 3);
            stopper = 1;
            Fight.FightsConfig.save(Fight.FightsConfigFile);
        }
    }

    private void removeEntries() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (Fight.team1.contains(player.getUniqueId())) Fight.team1Board.removeEntry(player.getDisplayName());
            else if (Fight.team2.contains(player.getUniqueId())) Fight.team2Board.removeEntry(player.getDisplayName());
        }
    }

    @SneakyThrows
    private void teamRemover(Player player, Collection<UUID> team1, Collection<UUID> team2) {
        if (team1.contains(player.getUniqueId())) {
            team1.remove(player.getUniqueId());
            if (team1.isEmpty() && !team2.isEmpty()) {
                clearing = true;
                if (config.getBoolean("create-fights-folder")) {
                    Collection<String> h = new ArrayList<>();
                    Fight.FightsConfig.load(Fight.FightsConfigFile);
                    Fight.FightsConfig.set("net.flex.ManualTournaments.Fight-winners", Fight.teamList(team2, h));
                    Fight.FightsConfig.save(Fight.FightsConfigFile);
                }
                new BukkitRunnable() {
                    int i = config.getInt("teleport-countdown-time");

                    @SneakyThrows
                    public void run() {
                        if (i == 0) {
                            Collection<String> array = new ArrayList<>();
                            for (UUID uuid : team2)
                                array.add(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName());
                            String listString = String.join(", ", array);
                            String replace = Objects.requireNonNull(config.getString("fight-winners")).replace("{team}", listString);
                            Bukkit.getServer().broadcastMessage(replace);
                            if (config.getBoolean("kill-on-fight-end")) {
                                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                    if (team2.contains(p.getUniqueId())) p.damage(10000);
                                }
                            } else {
                                String path = "fight-end-spawn.";
                                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                    if (team2.contains(p.getUniqueId()) && config.isSet(path)) {
                                        clear(player);
                                        p.teleport(location(path, config));
                                    }
                                }
                            }
                            clearing = false;
                            cancel();
                        }
                        --i;
                    }
                }.runTaskTimer(getPlugin(), 0L, 20L);
            }
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (Fight.temporary.contains(player)) {
            Location from = event.getFrom();
            if (from.getX() != Objects.requireNonNull(event.getTo()).getX() || from.getY() != event.getTo().getY())
                player.teleport(from);
        }
    }

    @EventHandler
    private void onJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        if (Fight.temporary.contains(player)) event.setCancelled(true);
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (Fight.team1.contains(player.getUniqueId()) || Fight.team2.contains(player.getUniqueId())) {
            if (!config.getBoolean("drop-items")) event.setCancelled(true);
        }
        if (Spectate.spectators.contains(player)) event.setCancelled(true);
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (Fight.team1.contains(player.getUniqueId()) || Fight.team2.contains(player.getUniqueId())) {
            if (!config.getBoolean("break-blocks")) event.setCancelled(true);
        }
        if (Spectate.spectators.contains(player)) event.setCancelled(true);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (config.getBoolean("default-gamemode-on-join")) player.setGameMode(Bukkit.getServer().getDefaultGameMode());
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (Fight.temporary.contains(player) || Fight.team1.contains(player.getUniqueId()) || Fight.team2.contains(player.getUniqueId()) || Spectate.spectators.contains(player)) {
            if (config.getBoolean("kill-on-fight-end")) {
                player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                player.setHealth(0);
                player.setWalkSpeed(0.2f);
            } else {
                String path = "fight-end-spawn.";
                if (config.isSet(path)) {
                    player.setGameMode(Bukkit.getServer().getDefaultGameMode());
                    player.teleport(location(path, config));
                    player.setWalkSpeed(0.2f);
                }
            }
        }
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (Spectate.spectators.contains(player)) {
            if (event.getMessage().startsWith("/spec") || event.getMessage().contains("spectate") || event.getMessage().startsWith("/mt_spec") || config.getStringList("spectator-allowed-commands").contains(event.getMessage())) {
                event.setCancelled(false);
            } else {
                player.sendMessage(message("not-allowed"));
                event.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (Spectate.spectators.contains(player)) {
            Spectate.spectators.remove(player);
            player.setGameMode(Bukkit.getServer().getDefaultGameMode());
            for (Player other : Bukkit.getServer().getOnlinePlayers()) other.showPlayer(player);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (Spectate.spectators.contains(player)) {
            if (player.getHealth() - event.getFinalDamage() > 0) return;
            event.setCancelled(true);
            teleportSpawn(player);
            Spectate.spectators.remove(player);
        } else if ((Fight.team1.contains(player.getUniqueId()) || Fight.team2.contains(player.getUniqueId())) && clearing) {
            if (player.getHealth() - event.getFinalDamage() > 0) return;
            event.setCancelled(true);
            Location respawnLocation = player.getBedSpawnLocation();
            if (respawnLocation == null) respawnLocation = player.getWorld().getSpawnLocation();
            clear(player);
            player.teleport(respawnLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    private void clear(Player player) {
        player.getInventory().clear();
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setAbsorptionAmount(0);
        player.setSaturation(0);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
    }

    private void teleportSpawn(Player player) {
        Location respawnLocation = player.getBedSpawnLocation();
        if (respawnLocation == null) respawnLocation = player.getWorld().getSpawnLocation();
        player.teleport(respawnLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }
}
