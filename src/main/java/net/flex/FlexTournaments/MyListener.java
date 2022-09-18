package net.flex.FlexTournaments;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class MyListener implements Listener {
    private static final FileConfiguration config = Main.getPlugin().getConfig();

    public MyListener() {
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (Fight.team1.contains(p.getUniqueId()) || Fight.team2.contains(p.getUniqueId())) {
            e.setDroppedExp(0);
            if (!config.getBoolean("drop-on-death")) {
                e.getDrops().clear();
            }
        }
        if (Fight.team1.contains(p.getUniqueId())) {
            Fight.team1.remove(p.getUniqueId());
            if (Fight.team1.isEmpty() & !Fight.team2.isEmpty()) {
                (new BukkitRunnable() {
                    int i = config.getInt("teleport-countdown-time");

                    public void run() {
                        if (this.i == 0) {
                            ArrayList<String> a = new ArrayList<>();
                            for (UUID uuid : Fight.team2) {
                                a.add(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName());
                            }
                            String listString = String.join(", ", a);
                            String joined = Objects.requireNonNull(config.getString("fight-winners")).replace("{won-team}", listString);
                            Bukkit.getServer().broadcastMessage(Main.conf("fight-winners"));
                            if (config.getBoolean("kill-on-fight-end")) {
                                for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team2.contains(p1.getUniqueId())) {
                                        p1.setHealth(0);
                                        Fight.teamA.unregister();
                                        Fight.teamB.unregister();
                                    }
                                }
                            } else {
                                String path = "fight-end-spawn.";
                                for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team2.contains(p1.getUniqueId())) {
                                        if (config.isSet(path)) {
                                            p1.teleport(Arena.pathing(path, config));
                                            Fight.teamA.unregister();
                                            Fight.teamB.unregister();
                                        }
                                    }
                                }
                            }
                            this.cancel();
                        }

                        --this.i;
                    }
                }).runTaskTimer(Main.getPlugin(), 0L, 20L);
            }
        }
        if (Fight.team2.contains(p.getUniqueId())) {
            Fight.team2.remove(p.getUniqueId());
            if (Fight.team2.isEmpty() & !Fight.team1.isEmpty()) {
                (new BukkitRunnable() {
                    int i = config.getInt("teleport-countdown-time");

                    public void run() {
                        if (this.i == 0) {
                            ArrayList<String> b = new ArrayList<>();
                            for (UUID uuid : Fight.team1) {
                                b.add(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName());
                            }
                            String listString = String.join(", ", b);
                            Bukkit.getServer().broadcastMessage(Main.conf("fight-winners") + listString + Main.conf("fight-winners2"));
                            if (config.getBoolean("kill-on-fight-end")) {
                                for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team1.contains(p2.getUniqueId())) {
                                        p2.setHealth(0);
                                        Fight.teamA.unregister();
                                        Fight.teamB.unregister();
                                    }
                                }
                            } else {
                                String path = "fight-end-spawn.";
                                for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team1.contains(p2.getUniqueId())) {
                                        if (config.isSet(path)) {
                                            p2.teleport(Arena.pathing(path, config));
                                            Fight.teamA.unregister();
                                            Fight.teamB.unregister();
                                        }
                                    }
                                }
                            }
                            this.cancel();
                        }

                        --this.i;
                    }
                }).runTaskTimer(Main.getPlugin(), 0L, 20L);
            }
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (Fight.temporary.contains(p)) {
            Location from = e.getFrom();
            if (from.getZ() != Objects.requireNonNull(e.getTo()).getZ() && from.getX() != e.getTo().getX()) {
                p.teleport(e.getFrom());
            }
        }
    }

    private void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (Fight.team1.contains(p.getUniqueId()) || Fight.team2.contains(p.getUniqueId())) {
            if (!config.getBoolean("drop-items")) {
                e.setCancelled(true);
            }
        }
    }

    private void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (Fight.team1.contains(p.getUniqueId()) || Fight.team2.contains(p.getUniqueId())) {
            if (!config.getBoolean("break-blocks")) {
                e.setCancelled(true);
            }
        }
    }
}
