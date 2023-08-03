package net.flex.ManualTournaments.listeners;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.Fight;
import net.flex.ManualTournaments.commands.fightCommands.TeamFight;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
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
import org.bukkit.scoreboard.Team;

import java.util.*;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.commands.fightCommands.TeamFight.FightsConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.*;
import static net.flex.ManualTournaments.utils.SqlMethods.*;


public class TeamFightListener implements Listener {
    static FileConfiguration config = getPlugin().getConfig();
    public static double regeneratedTeam1 = 0;
    public static double regeneratedTeam2 = 0;
    public static double damageTeam1 = 0;
    public static double damageTeam2 = 0;
    public static boolean stopper;

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        removeEntries();
        if (Fight.playerIsInTeam(player.getUniqueId())) {
            event.setDroppedExp(0);
            event.setDeathMessage(null);
            if (!config.getBoolean("drop-on-death")) event.getDrops().clear();
            if (!TeamFight.cancelled.get()) {
                teamRemover(player);
                if (killer != null && Fight.playerIsInTeam(killer.getUniqueId())) {
                    String replacePlayer = Objects.requireNonNull(config.getString("fight-death")).replace("{player}", player.getDisplayName());
                    String replaceKiller = replacePlayer.replace("{killer}", killer.getDisplayName());
                    event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', replaceKiller));
                }
            }
        }
    }

    @SneakyThrows
    private static void endCounter() {
        if (Fight.teams.values().isEmpty()) {
            FightsConfig.load(TeamFight.FightsConfigFile);
            FightsConfig.set("duration", TeamFight.duration - 3);
            durationUpdate(TeamFight.duration - 3);
            stopper = false;
            FightsConfig.save(TeamFight.FightsConfigFile);
        }
    }

    @SneakyThrows
    private void teamRemover(Player player) {
        Set<UUID> team1 = Collections.emptySet();
        Set<UUID> team2 = Collections.emptySet();
        Set<UUID> ffaTeam = Collections.emptySet();
        for (Map.Entry<Team, Set<UUID>> entry : Fight.teams.entrySet()) {
            if (entry.getValue().contains(player.getUniqueId())) {
                entry.getValue().remove(player.getUniqueId());
            }
            if (entry.getKey().getName().equals("1")) {
                team1 = entry.getValue();
            } else if (entry.getKey().getName().equals("2")) {
                team2 = entry.getValue();
            } else {
                ffaTeam = entry.getValue();
            }
        }
        if (team1.isEmpty() && !team2.isEmpty()) {
            teamRemoverHandler(team2);
        } else if (!team1.isEmpty() && team2.isEmpty()) {
            teamRemoverHandler(team1);
        }
    }

    @SneakyThrows
    private static void teamRemoverHandler(Set<UUID> team) {
        if (config.getBoolean("mysql-enabled")) {
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
        if (config.getBoolean("mysql-enabled")) {
            winnersUpdate(teamList());
        }
        if (config.getBoolean("create-fights-folder")) {
            FightsConfig.load(TeamFight.FightsConfigFile);
            FightsConfig.set("winners", teamList());
            FightsConfig.set("cancelled", false);
            FightsConfig.save(TeamFight.FightsConfigFile);
        }
        Collection<String> array = new ArrayList<>();
        for (UUID uuid : team) array.add(Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName());
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
                            if (team.contains(p.getUniqueId())) p.setHealth(0);
                        }
                    } else if (!config.getBoolean("kill-on-fight-end")) {
                        String path = "fight-end-spawn.";
                        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                            if (team.contains(p.getUniqueId()) && config.isSet(path)) {
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

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (Fight.playerIsInTeam(player.getUniqueId())) {
            if (!config.getBoolean("drop-items")) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (Fight.playerIsInTeam(player.getUniqueId())) {
            if (!config.getBoolean("break-blocks")) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (Fight.playerIsInTeam(player.getUniqueId())) {
            if (!config.getBoolean("place-blocks")) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (Fight.playerIsInTeam(player.getUniqueId())) {
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
            teamRemover(player);
        }
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (Fight.playerIsInTeam(player.getUniqueId())) {
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
        for (Map.Entry<Team, Set<UUID>> entry : Fight.teams.entrySet()) {
            Team team = entry.getKey();
            if (entry.getValue().contains(player.getUniqueId())) {
                if (team.getName().equals("1")) {
                    damageTeam1 += event.getFinalDamage();
                } else if (team.getName().equals("2")) {
                    damageTeam2 += event.getFinalDamage();
                }
                break;
            }
        }
    }

    @EventHandler
    public void onHealthRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        for (Map.Entry<Team, Set<UUID>> entry : Fight.teams.entrySet()) {
            Team team = entry.getKey();
            if (entry.getValue().contains(player.getUniqueId())) {
                if (team.getName().equals("1")) {
                    regeneratedTeam1 += event.getAmount();
                } else if (team.getName().equals("2")) {
                    regeneratedTeam2 += event.getAmount();
                }
                break;
            }
        }
    }
}
