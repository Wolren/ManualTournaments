package net.flex.ManualTournaments.listeners;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.fightCommands.TeamFight;
import net.flex.ManualTournaments.interfaces.FightType;
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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.commands.fightCommands.TeamFight.FightsConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.*;
import static net.flex.ManualTournaments.utils.SqlMethods.*;


public class TeamFightListener implements Listener {
    public final FightType fight;
    public final AtomicBoolean cancelled;
    public final Map<Team, Set<UUID>> teams;
    public double regeneratedTeam1 = 0;
    public double regeneratedTeam2 = 0;
    public double damageTeam1 = 0;
    public double damageTeam2 = 0;
    public boolean isForcedDeath = false;
    public static boolean stopper;
    private final HashMap<Location, Material> blockStates = new HashMap<>();
    private final Scoreboard scoreboard;

    public TeamFightListener(FightType fight, AtomicBoolean cancelled, Map<Team, Set<UUID>> teams, Scoreboard board) {
        this.fight = fight;
        this.cancelled = cancelled;
        this.teams = teams;
        this.scoreboard = board;
    }


    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (playerIsInTeam(player.getUniqueId())) {
            event.setDroppedExp(0);
            event.setDeathMessage(null);
            if (!config.getBoolean("drop-on-death")) event.getDrops().clear();
            if (!cancelled.get()) {
                if (killer != null && playerIsInTeam(killer.getUniqueId())) {
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
        if (teams.values().isEmpty()) {
            FightsConfig.load(TeamFight.FightsConfigFile);
            FightsConfig.set("duration", TeamFight.duration - 3);
            durationUpdate(TeamFight.duration - 3);
            stopper = false;
            FightsConfig.save(TeamFight.FightsConfigFile);
        }
    }

    @SneakyThrows
    private void teamRemover(Player player) {
        Set<UUID> team1 = new HashSet<>();
        Set<UUID> team2 = new HashSet<>();
        Map<Team, Set<UUID>> ffaTeams = new HashMap<>();
        for (Map.Entry<Team, Set<UUID>> entry : teams.entrySet()) {
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
            winnersUpdate(teamList(winner));
        }
        if (config.getBoolean("create-fights-folder")) {
            FightsConfig.load(TeamFight.FightsConfigFile);
            FightsConfig.set("winners", teamList(winner));
            FightsConfig.set("cancelled", false);
            FightsConfig.save(TeamFight.FightsConfigFile);
        }
        scoreboard.getTeams().forEach(Team::unregister);
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
                            clear(player);
                            p.teleport(location(path, config));
                        });
                        teams.clear();
                    }
                    triggerBlockResetAsync();
                    cancel();
                }
                --i;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
    }

    private void clearTeams() {
        scoreboard.getTeams().forEach(Team::unregister);
        teams.clear();
    }

    private void teamFfaHandler(Set<UUID> team) {
        scoreboard.getTeams().forEach(Team::unregister);
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
                            clear(player);
                            p.teleport(location(path, config));
                            p.setWalkSpeed(0.2F);
                        });
                        teams.clear();
                    }
                    triggerBlockResetAsync();
                    cancel();
                }
                --i;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (playerIsInTeam(player.getUniqueId())) {
            if (!config.getBoolean("drop-items")) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (playerIsInTeam(player.getUniqueId())) {
            if (!config.getBoolean("break-blocks")) event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (playerIsInTeam(player.getUniqueId())) {
            if (!config.getBoolean("place-blocks")) {
                event.setCancelled(true);
            } else {
                Location blockLocation = event.getBlock().getLocation();
                if (!blockStates.containsKey(blockLocation)) {
                    blockStates.put(blockLocation, event.getBlockReplacedState().getType());
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (playerIsInTeam(player.getUniqueId())) {
            event.setDropItems(false);
            Material initialBlockType = event.getBlock().getType();
            Location blockLocation = event.getBlock().getLocation();
            if (!blockStates.containsKey(blockLocation)) {
                blockStates.put(blockLocation, initialBlockType);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().forEach(block -> {
            Material initialBlockType = block.getType();
            Location blockLocation = block.getLocation();
            if (!blockStates.containsKey(blockLocation)) {
                blockStates.put(blockLocation, initialBlockType);
            }
        });
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (playerIsInTeam(player.getUniqueId())) {
            Material initialBlockType = event.getBlock().getType();
            Location blockLocation = event.getBlock().getLocation();
            if (!blockStates.containsKey(blockLocation)) {
                blockStates.put(blockLocation, initialBlockType);
            }
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        Material initialBlockType = event.getBlock().getType();
        Location blockLocation = event.getBlock().getLocation();
        if (!blockStates.containsKey(blockLocation)) {
            blockStates.put(blockLocation, initialBlockType);
        }
    }

    @EventHandler
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            Material initialBlockType = event.getBlock().getType();
            Location landedLocation = event.getBlock().getLocation();
            if (!blockStates.containsKey(landedLocation)) {
                blockStates.put(landedLocation, initialBlockType);
            }
        }
    }

    @EventHandler
    public void onBlockFormLand(BlockFormEvent event) {
        Material initialBlockType = event.getBlock().getType();
        Location blockLocation = event.getBlock().getLocation();
        if (!blockStates.containsKey(blockLocation)) {
            blockStates.put(blockLocation, initialBlockType);
        }
    }
    
    public void triggerBlockResetAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            for (Map.Entry<Location, Material> entry : blockStates.entrySet()) {
                Location blockLocation = entry.getKey();
                Material initialBlockType = entry.getValue();

                Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
                    if (blockStates.containsKey(blockLocation) && blockStates.get(blockLocation) == initialBlockType) {
                        blockLocation.getBlock().setType(initialBlockType);
                        blockStates.remove(blockLocation);
                    }
                });
            }
        });
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerIsInTeam(player.getUniqueId())) {
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
        if (playerIsInTeam(player.getUniqueId())) {
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
        for (Map.Entry<Team, Set<UUID>> entry : teams.entrySet()) {
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
        for (Map.Entry<Team, Set<UUID>> entry : teams.entrySet()) {
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

    public boolean playerIsInTeam(UUID player) {
        return teams.values().stream().anyMatch(list -> list.contains(player));
    }
}
