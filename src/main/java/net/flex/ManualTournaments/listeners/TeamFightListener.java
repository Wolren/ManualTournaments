package net.flex.ManualTournaments.listeners;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.utils.FightContext;
import net.flex.ManualTournaments.utils.SqlMethods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class TeamFightListener implements Listener {
    private final FightType fight;
    private final FightContext context;
    public double regeneratedTeam1 = 0;
    public double regeneratedTeam2 = 0;
    public double damageTeam1 = 0;
    public double damageTeam2 = 0;
    public boolean isForcedDeath = false;
    private final HashMap<Location, Material> blockStates = new HashMap<>();
    private int maxBlockStates;

    public TeamFightListener(FightType fight, FightContext context) {
        this.fight = fight;
        this.context = context;
        this.maxBlockStates = Math.max(100, Math.min(50000, Main.getPlugin().getConfig().getInt("tournament-block-state-limit", 5000)));
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (context.playerIsInTeam(player.getUniqueId())) {
            event.setDroppedExp(0);
            event.setDeathMessage(null);
            if (!config.getBoolean("drop-on-death")) event.getDrops().clear();
            if (!context.cancelled.get()) {
                if (killer != null && context.playerIsInTeam(killer.getUniqueId())) {
                    String replacePlayer = Objects.requireNonNull(config.getString("fight-death")).replace("{player}", player.getDisplayName());
                    String replaceKiller = replacePlayer.replace("{killer}", killer.getDisplayName());
                    event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', replaceKiller));
                }
                teamRemover(player);
            }
        }
    }

    @SneakyThrows
    private void endCounter() {
        if (context.teams.values().isEmpty()) {
            context.fightsConfig.load(context.fightsConfigFile);
            context.fightsConfig.set("duration", context.duration - 3);
            SqlMethods.durationUpdate(context.duration - 3);
            context.stopper = false;
            context.fightsConfig.save(context.fightsConfigFile);
        }
    }

    @SneakyThrows
    private void teamRemover(Player player) {
        Set<UUID> team1 = new HashSet<>();
        Set<UUID> team2 = new HashSet<>();
        Map<Team, Set<UUID>> ffaTeams = new HashMap<>();
        for (Map.Entry<Team, Set<UUID>> entry : context.teams.entrySet()) {
            if (entry.getValue().contains(player.getUniqueId())) {
                entry.getValue().remove(player.getUniqueId());
            }
            if (entry.getKey().getName().equals("1")) {
                team1 = entry.getValue();
            } else if (entry.getKey().getName().equals("2")) {
                team2 = entry.getValue();
            } else {
                ffaTeams.put(entry.getKey(), entry.getValue());
            }
        }
        if (!isForcedDeath) {
            if (team1.isEmpty() && !team2.isEmpty()) {
                teamHandler(team2, "2");
            } else if (!team1.isEmpty() && team2.isEmpty()) {
                teamHandler(team1, "1");
            }
        }
        int filledTeams = 0;
        Map.Entry<Team, Set<UUID>> nonEmptyTeam = null;
        for (Map.Entry<Team, Set<UUID>> entry : ffaTeams.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                filledTeams++;
                nonEmptyTeam = entry;
            }
        }
        if (filledTeams == 1) {
            teamFfaHandler(nonEmptyTeam.getValue());
        }
    }

    @SneakyThrows
    private void teamHandler(Set<UUID> team, String winner) {
        if (config.getBoolean("mysql-enabled")) {
            SqlMethods.regeneratedUpdate(regeneratedTeam1, regeneratedTeam2);
            SqlMethods.damageUpdate(damageTeam1, damageTeam2);
        }
        if (config.getBoolean("create-fights-folder")) {
            context.fightsConfig.set("damageTeam1", damageTeam1);
            context.fightsConfig.set("damageTeam2", damageTeam2);
            context.fightsConfig.set("regeneratedTeam1", regeneratedTeam1);
            context.fightsConfig.set("regeneratedTeam2", regeneratedTeam2);
            context.fightsConfig.save(context.fightsConfigFile);
        }
        regeneratedTeam1 = 0;
        regeneratedTeam2 = 0;
        damageTeam1 = 0;
        damageTeam2 = 0;
        if (config.getBoolean("mysql-enabled")) {
            SqlMethods.winnersUpdate(context.teamList(winner));
        }
        if (config.getBoolean("create-fights-folder")) {
            context.fightsConfig.load(context.fightsConfigFile);
            context.fightsConfig.set("winners", context.teamList(winner));
            context.fightsConfig.set("cancelled", false);
            context.fightsConfig.save(context.fightsConfigFile);
        }
        context.board.getTeams().forEach(Team::unregister);
        Collection<String> array = team.stream().map(uuid -> Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName()).collect(Collectors.toList());
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
                        isForcedDeath = true;
                        Bukkit.getServer().getOnlinePlayers().stream().filter(p -> team.contains(p.getUniqueId())).forEachOrdered(p -> p.setHealth(0));
                        isForcedDeath = false;
                    } else if (!config.getBoolean("kill-on-fight-end")) {
                        String path = "fight-end-spawn.";
                        Bukkit.getServer().getOnlinePlayers().stream().filter(p -> team.contains(p.getUniqueId()) && config.isSet(path)).forEachOrdered(p -> {
                            clear(p);
                            p.teleport(location(path, config));
                        });
                        context.teams.clear();
                    }
                    triggerBlockResetAsync();
                    if (context.onFightEnd != null) context.onFightEnd.run();
                    cancel();
                }
                --i;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
    }

    private void teamFfaHandler(Set<UUID> team) {
        context.board.getTeams().forEach(Team::unregister);
        Collection<String> array = team.stream().map(uuid -> Objects.requireNonNull(Bukkit.getPlayer(uuid)).getDisplayName()).collect(Collectors.toList());
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
                        Bukkit.getServer().getOnlinePlayers().stream().filter(p -> team.contains(p.getUniqueId())).forEachOrdered(p -> {
                            p.setHealth(0);
                            p.setWalkSpeed(0.2F);
                        });
                    } else if (!config.getBoolean("kill-on-fight-end")) {
                        String path = "fight-end-spawn.";
                        Bukkit.getServer().getOnlinePlayers().stream().filter(p -> team.contains(p.getUniqueId()) && config.isSet(path)).forEachOrdered(p -> {
                            clear(p);
                            p.teleport(location(path, config));
                            p.setWalkSpeed(0.2F);
                        });
                        context.teams.clear();
                    }
                    triggerBlockResetAsync();
                    if (context.onFightEnd != null) context.onFightEnd.run();
                    cancel();
                }
                --i;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (context.playerIsInTeam(player.getUniqueId())) {
            if (!config.getBoolean("drop-items")) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (context.playerIsInTeam(player.getUniqueId())) {
            if (!config.getBoolean("break-blocks")) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (context.playerIsInTeam(player.getUniqueId())) {
            if (!config.getBoolean("place-blocks")) {
                event.setCancelled(true);
            } else {
                Location blockLocation = event.getBlock().getLocation();
                if (!blockStates.containsKey(blockLocation) && blockStates.size() < maxBlockStates) {
                    blockStates.put(blockLocation, event.getBlockReplacedState().getType());
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (context.playerIsInTeam(player.getUniqueId())) {
            event.setDropItems(false);
            Material initialBlockType = event.getBlock().getType();
            Location blockLocation = event.getBlock().getLocation();
            if (!blockStates.containsKey(blockLocation) && blockStates.size() < maxBlockStates) {
                blockStates.put(blockLocation, initialBlockType);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().forEach(block -> {
            Material initialBlockType = block.getType();
            Location blockLocation = block.getLocation();
            if (!blockStates.containsKey(blockLocation) && blockStates.size() < maxBlockStates) {
                blockStates.put(blockLocation, initialBlockType);
            }
        });
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (context.playerIsInTeam(player.getUniqueId())) {
            Material initialBlockType = event.getBlock().getType();
            Location blockLocation = event.getBlock().getLocation();
            if (!blockStates.containsKey(blockLocation) && blockStates.size() < maxBlockStates) {
                blockStates.put(blockLocation, initialBlockType);
            }
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        Material initialBlockType = event.getBlock().getType();
        Location blockLocation = event.getBlock().getLocation();
        if (!blockStates.containsKey(blockLocation) && blockStates.size() < maxBlockStates) {
            blockStates.put(blockLocation, initialBlockType);
        }
    }

    @EventHandler
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            Material initialBlockType = event.getBlock().getType();
            Location landedLocation = event.getBlock().getLocation();
            if (!blockStates.containsKey(landedLocation) && blockStates.size() < maxBlockStates) {
                blockStates.put(landedLocation, initialBlockType);
            }
        }
    }

    @EventHandler
    public void onBlockFormLand(BlockFormEvent event) {
        Material initialBlockType = event.getBlock().getType();
        Location blockLocation = event.getBlock().getLocation();
        if (!blockStates.containsKey(blockLocation) && blockStates.size() < maxBlockStates) {
            blockStates.put(blockLocation, initialBlockType);
        }
    }

    public void triggerBlockResetAsync() {
        final Map<Location, Material> snapshot = new HashMap<>(blockStates);
        blockStates.clear();
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            for (Map.Entry<Location, Material> entry : snapshot.entrySet()) {
                Location blockLocation = entry.getKey();
                Material initialBlockType = entry.getValue();

                Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
                    blockLocation.getBlock().setType(initialBlockType);
                });
            }
        });
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (context.playerIsInTeam(player.getUniqueId())) {
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
        if (context.playerIsInTeam(player.getUniqueId())) {
            if (config.getStringList("fight-allowed-commands").contains(event.getMessage()) || player.isOp()) {
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
        for (Map.Entry<Team, Set<UUID>> entry : context.teams.entrySet()) {
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
        for (Map.Entry<Team, Set<UUID>> entry : context.teams.entrySet()) {
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
