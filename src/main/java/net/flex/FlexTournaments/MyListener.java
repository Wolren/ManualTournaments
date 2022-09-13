package net.flex.FlexTournaments;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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
                            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("fight-winners") + listString + config.getString("fight-winners2")));
                            if (config.getBoolean("kill-on-fight-end")) {
                                for(Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team2.contains(p1.getUniqueId())) {
                                        p1.setHealth(0);
                                    }
                                }
                            } else {
                                String path = "fight-end-spawn.";
                                for(Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team2.contains(p1.getUniqueId())) {
                                        p1.teleport(Arena.pathing(path, config));
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
                            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("fight-winners") + listString + config.getString("fight-winners2")));
                            if (config.getBoolean("kill-on-fight-end")) {
                                for(Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team1.contains(p2.getUniqueId())) {
                                        p2.setHealth(0);
                                    }
                                }
                            } else {
                                String path = "fight-end-spawn.";
                                for(Player p2 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (Fight.team1.contains(p2.getUniqueId())) {
                                        p2.teleport(Arena.pathing(path, config));
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
}
