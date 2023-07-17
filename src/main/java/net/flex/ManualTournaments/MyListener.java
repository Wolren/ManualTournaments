package net.flex.ManualTournaments;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.Fight;
import net.flex.ManualTournaments.commands.Spectate;
import net.flex.ManualTournaments.events.PlayerJumpEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.commands.Fight.*;
import static net.flex.ManualTournaments.utils.SharedMethods.*;
import static net.flex.ManualTournaments.utils.SqlMethods.*;


public final class MyListener implements Listener {
    static FileConfiguration config = getPlugin().getConfig();
    public static Collection<String> winners = new ArrayList<>();
    public static double regeneratedTeam1 = 0;
    public static double regeneratedTeam2 = 0;
    public static double damageTeam1 = 0;
    public static double damageTeam2 = 0;
    public static int stopper;

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        removeEntries();
        if (Fight.team1.contains(player.getUniqueId()) || Fight.team2.contains(player.getUniqueId())) {
            event.setDroppedExp(0);
            event.setDeathMessage("");
            if (!config.getBoolean("drop-on-death")) event.getDrops().clear();
            if (killer != null && !cancelled) {
                if (Fight.team1.contains(killer.getUniqueId()) || Fight.team2.contains(killer.getUniqueId())) {
                    String replacePlayer = Objects.requireNonNull(config.getString("fight-death")).replace("{player}", player.getDisplayName());
                    String replaceKiller = replacePlayer.replace("{killer}", killer.getDisplayName());
                    event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', replaceKiller));
                }
            }
        }
        if (!cancelled) {
            teamRemover(player, Fight.team1, Fight.team2);
            teamRemover(player, Fight.team2, Fight.team1);
        }
        if (config.getBoolean("create-fights-folder")) endCounter();
    }

    @SneakyThrows
    private void endCounter() {
        if (Fight.team1.isEmpty() && Fight.team2.isEmpty()) {
            FightsConfig.load(Fight.FightsConfigFile);
            FightsConfig.set("duration", Fight.duration - 3);
            durationUpdate(duration - 3);
            stopper = 1;
            FightsConfig.save(Fight.FightsConfigFile);
        }
    }

    @SneakyThrows
    private void teamRemover(Player player, Collection<UUID> team1, Collection<UUID> team2) {
        if (team1.contains(player.getUniqueId())) {
            team1.remove(player.getUniqueId());
            if (team1.isEmpty() && !team2.isEmpty()) {
                winners.clear();
                regeneratedUpdate(regeneratedTeam1, regeneratedTeam2);
                damageUpdate(damageTeam1, damageTeam2);
                FightsConfig.set("damageTeam1", damageTeam1);
                FightsConfig.set("damageTeam2", damageTeam2);
                FightsConfig.set("regeneratedTeam1", regeneratedTeam1);
                FightsConfig.set("regeneratedTeam2", regeneratedTeam2);
                FightsConfig.save(Fight.FightsConfigFile);
                regeneratedTeam1 = 0;
                regeneratedTeam2 = 0;
                damageTeam1 = 0;
                damageTeam2 = 0;
                if (Fight.team1.isEmpty()) {
                    winnersUpdate(teamList(Fight.team2, winners));
                    winners.clear();
                    if (config.getBoolean("create-fights-folder")) {
                        FightsConfig.load(Fight.FightsConfigFile);
                        FightsConfig.set("winners", teamList(Fight.team2, winners));
                        FightsConfig.save(Fight.FightsConfigFile);
                    }
                }
                else if (Fight.team2.isEmpty()) {
                    winnersUpdate(teamList(Fight.team1, winners));
                    winners.clear();
                    if (config.getBoolean("create-fights-folder")) {
                        FightsConfig.load(Fight.FightsConfigFile);
                        FightsConfig.set("winners", teamList(Fight.team1, winners));
                        FightsConfig.save(Fight.FightsConfigFile);
                    }
                }
                Collection<String> array = new ArrayList<>();
                for (UUID uuid : team2) array.add(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName());
                String listString = String.join(", ", array);
                new BukkitRunnable() {
                    int i = config.getInt("teleport-countdown-time");

                    @SneakyThrows
                    public void run() {
                        if (i == 0) {
                            String replace = Objects.requireNonNull(config.getString("fight-winners")).replace("{team}", listString);
                            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', replace));
                            if (config.getBoolean("kill-on-fight-end")) {
                                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                    if (team2.contains(p.getUniqueId())) p.setHealth(0);
                                }
                            } else if (!config.getBoolean("kill-on-fight-end")) {
                                String path = "fight-end-spawn.";
                                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                    if (team2.contains(p.getUniqueId()) && config.isSet(path)) {
                                        clear(player);
                                        p.teleport(location(path, config));
                                    }
                                }
                            }
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
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getEntityType() == EntityType.PLAYER) {
            if (Fight.team1.contains(player.getUniqueId())) damageTeam1 += event.getFinalDamage();
            else if (Fight.team2.contains(player.getUniqueId())) damageTeam2 += event.getFinalDamage();
        }
        if (Spectate.spectators.contains(player)) {
            if (player.getHealth() - event.getFinalDamage() > 0) return;
            event.setCancelled(true);
            teleportSpawn(player);
            Spectate.spectators.remove(player);
        }
    }

    @EventHandler
    public void onHealthRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (Fight.team1.contains(player.getUniqueId())) {
            regeneratedTeam1 += event.getAmount();
        } else if (Fight.team2.contains(player.getUniqueId())) {
            regeneratedTeam2 += event.getAmount();
        }
    }

    private void teleportSpawn(Player player) {
        Location respawnLocation = player.getBedSpawnLocation();
        if (respawnLocation == null) respawnLocation = player.getWorld().getSpawnLocation();
        player.teleport(respawnLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }
}
