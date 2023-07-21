package net.flex.ManualTournaments.listeners;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.fightCommands.TeamFight;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.commands.fightCommands.TeamFight.FightsConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.*;
import static net.flex.ManualTournaments.utils.SqlMethods.*;


public class TeamFightListener implements Listener {
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
        if (TeamFight.team1.contains(player.getUniqueId()) || TeamFight.team2.contains(player.getUniqueId())) {
            event.setDroppedExp(0);
            event.setDeathMessage("");
            if (!config.getBoolean("drop-on-death")) event.getDrops().clear();
            if (killer != null && !TeamFight.cancelled) {
                if (TeamFight.team1.contains(killer.getUniqueId()) || TeamFight.team2.contains(killer.getUniqueId())) {
                    String replacePlayer = Objects.requireNonNull(config.getString("fight-death")).replace("{player}", player.getDisplayName());
                    String replaceKiller = replacePlayer.replace("{killer}", killer.getDisplayName());
                    event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', replaceKiller));
                }
            }
        }
        if (!TeamFight.cancelled) {
            teamRemover(player, TeamFight.team1, TeamFight.team2);
            teamRemover(player, TeamFight.team2, TeamFight.team1);
        }
    }

    @SneakyThrows
    private void endCounter() {
        if (TeamFight.team1.isEmpty() && TeamFight.team2.isEmpty()) {
            FightsConfig.load(TeamFight.FightsConfigFile);
            FightsConfig.set("duration", TeamFight.duration - 3);
            durationUpdate(TeamFight.duration - 3);
            stopper = 1;
            FightsConfig.save(TeamFight.FightsConfigFile);
        }
    }

    @SneakyThrows
    private void teamRemover(Player player, Collection<UUID> team1, Collection<UUID> team2) {
        if (team1.contains(player.getUniqueId())) {
            team1.remove(player.getUniqueId());
            if (team1.isEmpty() && !team2.isEmpty()) {
                if (config.getBoolean("mysql-enabled")) {
                    winners.clear();
                    regeneratedUpdate(regeneratedTeam1, regeneratedTeam2);
                    damageUpdate(damageTeam1, damageTeam2);
                }
                if (config.getBoolean("create-fights-folder")) {
                    FightsConfig.set("damageTeam1", damageTeam1);
                    FightsConfig.set("damageTeam2", damageTeam2);
                    FightsConfig.set("regeneratedTeam1", regeneratedTeam1);
                    FightsConfig.set("regeneratedTeam2", regeneratedTeam2);
                    FightsConfig.save(TeamFight.FightsConfigFile);
                }
                regeneratedTeam1 = 0;
                regeneratedTeam2 = 0;
                damageTeam1 = 0;
                damageTeam2 = 0;
                if (TeamFight.team1.isEmpty()) {
                    if (config.getBoolean("mysql-enabled")) {
                        winnersUpdate(teamList(TeamFight.team2, winners));
                        winners.clear();
                    }
                    if (config.getBoolean("create-fights-folder")) {
                        FightsConfig.load(TeamFight.FightsConfigFile);
                        FightsConfig.set("winners", teamList(TeamFight.team2, winners));
                        winners.clear();
                        FightsConfig.set("cancelled", false);
                        FightsConfig.save(TeamFight.FightsConfigFile);
                    }
                }
                else if (TeamFight.team2.isEmpty()) {
                    if (config.getBoolean("mysql-enabled")) {
                        winnersUpdate(teamList(TeamFight.team1, winners));
                        winners.clear();
                    }
                    if (config.getBoolean("create-fights-folder")) {
                        FightsConfig.load(TeamFight.FightsConfigFile);
                        FightsConfig.set("winners", teamList(TeamFight.team1, winners));
                        winners.clear();
                        FightsConfig.set("cancelled", false);
                        FightsConfig.save(TeamFight.FightsConfigFile);
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
                            if (config.getBoolean("create-fights-folder")) endCounter();
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
    private void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (TeamFight.team1.contains(player.getUniqueId()) || TeamFight.team2.contains(player.getUniqueId())) {
            if (!config.getBoolean("drop-items")) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (TeamFight.team1.contains(player.getUniqueId()) || TeamFight.team2.contains(player.getUniqueId())) {
            if (!config.getBoolean("break-blocks")) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (TeamFight.team1.contains(player.getUniqueId()) || TeamFight.team2.contains(player.getUniqueId())) {
            if (!config.getBoolean("place-blocks")) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (TeamFight.team1.contains(player.getUniqueId()) || TeamFight.team2.contains(player.getUniqueId())) {
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
            teamRemover(player, TeamFight.team1, TeamFight.team2);
            teamRemover(player, TeamFight.team2, TeamFight.team1);
        }
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (TeamFight.team1.contains(player.getUniqueId()) || TeamFight.team2.contains(player.getUniqueId())) {
            if (config.getStringList("spectator-allowed-commands").contains(event.getMessage()) || player.isOp()) {
                event.setCancelled(false);
            } else {
                player.sendMessage(message("not-allowed"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getEntityType() == EntityType.PLAYER) {
            if (TeamFight.team1.contains(player.getUniqueId())) damageTeam1 += event.getFinalDamage();
            else if (TeamFight.team2.contains(player.getUniqueId())) damageTeam2 += event.getFinalDamage();
        }
    }

    @EventHandler
    public void onHealthRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (TeamFight.team1.contains(player.getUniqueId())) {
            regeneratedTeam1 += event.getAmount();
        } else if (TeamFight.team2.contains(player.getUniqueId())) {
            regeneratedTeam2 += event.getAmount();
        }
    }
}
