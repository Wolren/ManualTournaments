package net.flex.FlexTournaments;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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
                            Bukkit.getServer().broadcastMessage(Main.conf("fight-winners") + listString + Main.conf("fight-winners2"));
                            if (config.getBoolean("kill-on-fight-end")) {
                                for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team2.contains(p1.getUniqueId())) {
                                        p1.setHealth(0);
                                        Fight.teamB.unregister();
                                    }
                                }
                            } else {
                                String path = "fight-end-spawn.";
                                for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team2.contains(p1.getUniqueId())) {
                                        p1.teleport(Arena.pathing(path, config));
                                        Fight.teamB.unregister();
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
                                    }
                                }
                            } else {
                                String path = "fight-end-spawn.";
                                for (Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team1.contains(p2.getUniqueId())) {
                                        p2.teleport(Arena.pathing(path, config));
                                        Fight.teamA.unregister();
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
        Player player = e.getPlayer();
        if (Fight.temporary.contains(player)) {
            Location from = e.getFrom();
            if (from.getZ() != Objects.requireNonNull(e.getTo()).getZ() && from.getX() != e.getTo().getX()) {
                player.teleport(e.getFrom());
            }
        }
    }
}
