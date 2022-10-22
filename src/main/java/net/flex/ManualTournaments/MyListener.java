package net.flex.ManualTournaments;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("deprecation")
class MyListener implements Listener {
    static final FileConfiguration config = Main.getPlugin().getConfig();
    static int y;

    @EventHandler
    private void onDeath(final PlayerDeathEvent e) {
        final Player p = e.getEntity();
        removeEntries();
        if (Fight.team1.contains(p.getUniqueId()) || Fight.team2.contains(p.getUniqueId())) {
            e.setDroppedExp(0);
            if (!config.getBoolean("drop-on-death")) {
                e.getDrops().clear();
            }
        }
        teamRemover(p, Fight.team1, Fight.team2);
        teamRemover(p, Fight.team2, Fight.team1);
        if (config.getBoolean("create-fights-folder")) {
            endCounter();
        }
    }

    private void endCounter() {
        if (Fight.team1.isEmpty() && Fight.team2.isEmpty()) {
            try {
                Fight.FightsConfig.load(Fight.FightsConfigFile);
            } catch (final IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
            Fight.FightsConfig.set("Fight-duration", Fight.j - 3);
            y = 1;
            try {
                Fight.FightsConfig.save(Fight.FightsConfigFile);
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void removeEntries() {
        for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (Fight.team1.contains(p.getUniqueId())) {
                Fight.teamA.removeEntry(p.getDisplayName());
            } else if (Fight.team2.contains(p.getUniqueId())) {
                Fight.teamB.removeEntry(p.getDisplayName());
            }
        }
    }

    @SneakyThrows
    private void teamRemover(final Player p, final Collection<UUID> team1, final Collection<UUID> team2) {
        if (team1.contains(p.getUniqueId())) {
            team1.remove(p.getUniqueId());
            if (team1.isEmpty() && !team2.isEmpty()) {
                if (config.getBoolean("create-fights-folder")) {
                    final Collection<String> h = new ArrayList<>();
                    Fight.FightsConfig.load(Fight.FightsConfigFile);
                    Fight.FightsConfig.set("Fight-winners", Fight.teamList(team2, h));
                    Fight.FightsConfig.save(Fight.FightsConfigFile);
                }
                new BukkitRunnable() {
                    int i = config.getInt("teleport-countdown-time");

                    @SneakyThrows
                    public void run() {
                        if (i == 0) {
                            final Collection<String> a = new ArrayList<>();
                            for (final UUID uuid : team2) {
                                a.add(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName());
                            }
                            final String listString = String.join(", ", a);
                            final String replace = Objects.requireNonNull(Main.getPlugin().getConfig().getString("fight-winners")).replace("{team}", listString);
                            Bukkit.getServer().broadcastMessage(replace);
                            if (config.getBoolean("kill-on-fight-end")) {
                                for (final Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (team2.contains(p1.getUniqueId())) {
                                        p1.setHealth(0);
                                    }
                                }
                            } else {
                                final String path = "fight-end-spawn.";
                                for (final Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                                    if (team2.contains(p1.getUniqueId())) {
                                        if (config.isSet(path)) {
                                            p1.teleport(Arena.pathing(path, config));
                                        }
                                    }
                                }
                            }
                            cancel();
                        }

                        --i;
                    }
                }.runTaskTimer(Main.getPlugin(), 0L, 20L);
            }
        }
    }

    @EventHandler
    private void onMove(final PlayerMoveEvent e) {
        final Player p = e.getPlayer();
        if (Fight.temporary.contains(p)) {
            final Location from = e.getFrom();
            if (from.getX() != Objects.requireNonNull(e.getTo()).getX() || from.getY() != e.getTo().getY()) {
                p.teleport(from);
            }
        }
    }

    @EventHandler
    private void onJump(final PlayerJumpEvent e) {
        final Player p = e.getPlayer();
        if (Fight.temporary.contains(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onDrop(final PlayerDropItemEvent e) {
        final Player p = e.getPlayer();
        if (Fight.team1.contains(p.getUniqueId()) || Fight.team2.contains(p.getUniqueId())) {
            if (!config.getBoolean("drop-items")) {
                e.setCancelled(true);
            }
        }
        if (Spectate.spectators.contains(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onBreak(final BlockBreakEvent e) {
        final Player p = e.getPlayer();
        if (Fight.team1.contains(p.getUniqueId()) || Fight.team2.contains(p.getUniqueId())) {
            if (!config.getBoolean("break-blocks")) {
                e.setCancelled(true);
            }
        }
        if (Spectate.spectators.contains(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (config.getBoolean("default-gamemode-on-join")) {
            p.setGameMode(Bukkit.getServer().getDefaultGameMode());
        }
    }

    @EventHandler
    private void onLeave(final PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        if (Fight.temporary.contains(p) || Fight.team1.contains(p.getUniqueId()) || Fight.team2.contains(p.getUniqueId()) || Spectate.spectators.contains(p)) {
            if (config.getBoolean("kill-on-fight-end")) {
                p.setGameMode(Bukkit.getServer().getDefaultGameMode());
                p.setHealth(0);
                p.setWalkSpeed(0.2f);
            } else {
                final String path = "fight-end-spawn.";
                if (config.isSet(path)) {
                    p.setGameMode(Bukkit.getServer().getDefaultGameMode());
                    p.teleport(Arena.pathing(path, config));
                    p.setWalkSpeed(0.2f);
                }
            }
        }
    }

    @EventHandler
    private void onCommand(final PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();
        if (Spectate.spectators.contains(p)) {
            if (e.getMessage().startsWith("spec") || e.getMessage().startsWith("mt_spec") || config.getStringList("spectator-allowed-commands").contains(e.getMessage())) {
                e.setCancelled(false);
            } else {
                p.sendMessage(Main.conf("not-allowed"));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onWorldChange(final PlayerChangedWorldEvent e) {
        final Player p = e.getPlayer();
        if (Spectate.spectators.contains(p)) {
            Spectate.spectators.remove(p);
            p.setGameMode(Bukkit.getServer().getDefaultGameMode());
            for (final Player other : Bukkit.getServer().getOnlinePlayers()) {
                other.showPlayer(p);
            }
        }
    }
}
