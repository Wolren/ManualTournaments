package net.flex.ManualTournaments.utils.tournament;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.Spectate;
import net.flex.ManualTournaments.commands.kitCommands.GiveKit;
import net.flex.ManualTournaments.factories.FightFactory;
import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.listeners.TeamFightListener;
import net.flex.ManualTournaments.listeners.TemporaryListener;
import net.flex.ManualTournaments.utils.FightContext;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.function.Consumer;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class TournamentFight implements FightType {

    private FightContext context;
    private TeamFightListener listener;
    private final String arenaName;
    private final String kitName;
    private final Consumer<UUID> onComplete;
    private UUID winnerId;
    private BukkitRunnable timeoutTask;
    private Player player1;
    private Player player2;
    private int timeoutSeconds;

    public TournamentFight(String arenaName, String kitName, Consumer<UUID> onComplete) {
        this.arenaName = arenaName;
        this.kitName = kitName;
        this.onComplete = onComplete;
    }

    /**
     * Start a tournament match between two teams (each with 1+ players).
     */
    @SneakyThrows
    public void startMatch(List<Player> team1Players, List<Player> team2Players) {
        if (team1Players.isEmpty() || team2Players.isEmpty()) {
            Bukkit.getLogger().warning("[ManualTournaments] Cannot start match with empty team");
            onComplete.accept(team1Players.isEmpty() ? team2Players.get(0).getUniqueId() : team1Players.get(0).getUniqueId());
            return;
        }

        this.player1 = team1Players.get(0); // captain
        this.player2 = team2Players.get(0); // captain

        // Quick validation
        if (!getArenaConfig().isSet("Arenas." + arenaName + ".pos1")
                || !getArenaConfig().isSet("Arenas." + arenaName + ".pos2")
                || !getArenaConfig().isSet("Arenas." + arenaName + ".spectator")) {
            Bukkit.getLogger().warning("[ManualTournaments] Arena '" + arenaName
                    + "' not fully configured for tournament match. Forfeiting.");
            onComplete.accept(team1Players.get(0).getUniqueId());
            return;
        }

        // Determine timeout from tournament setting or config default
        this.timeoutSeconds = Main.getPlugin().getConfig().getInt("tournament-match-timeout", 900);

        // Save current config
        String savedArena = config.getString("current-arena");
        String savedKit = config.getString("current-kit");

        config.set("current-arena", arenaName);
        config.set("current-kit", kitName);

        Map<Team, Set<UUID>> teams = new HashMap<>();
        Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

        context = new FightContext(teams, board);
        FightFactory.registerFight(this);
        listener = new TeamFightListener(this, context);
        TemporaryListener temporaryListener = new TemporaryListener(context.frozen);
        Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin());
        Bukkit.getPluginManager().registerEvents(temporaryListener, Main.getPlugin());
        Main.getPlugin().addFightListener(listener);
        Main.getPlugin().addTemporaryListener(temporaryListener);

        List<Player> allFighters = new ArrayList<>();
        allFighters.addAll(team1Players);
        allFighters.addAll(team2Players);
        context.distinctFighters.addAll(allFighters);

        board.getTeams().forEach(Team::unregister);
        context.cancelled.set(false);
        context.distinctFighters.clear();

        context.team1 = board.registerNewTeam("1");
        context.team2 = board.registerNewTeam("2");
        setupTeam(context.team1);
        setupTeam(context.team2);

        Bukkit.getOnlinePlayers().forEach(online -> online.setScoreboard(board));

        // Set up all team 1 members
        for (Player p : team1Players) {
            p.setGameMode(GameMode.SURVIVAL);
            Spectate.stopWithoutKill(p);
            context.team1.addEntry(p.getName());
            teams.computeIfAbsent(context.team1, k -> new HashSet<>()).add(p.getUniqueId());
            p.teleport(location("Arenas." + arenaName + ".pos1.", getArenaConfig()));
            GiveKit.setKit(p, kitName);
        }

        // Set up all team 2 members
        for (Player p : team2Players) {
            p.setGameMode(GameMode.SURVIVAL);
            Spectate.stopWithoutKill(p);
            context.team2.addEntry(p.getName());
            teams.computeIfAbsent(context.team2, k -> new HashSet<>()).add(p.getUniqueId());
            p.teleport(location("Arenas." + arenaName + ".pos2.", getArenaConfig()));
            GiveKit.setKit(p, kitName);
        }

        // Set the onFightEnd hook — determines winner from team survivors
        context.onFightEnd = this::endFight;

        // Freeze/countdown
        if (config.getBoolean("freeze-on-start")) {
            freezeCountdown(allFighters);
        } else if (config.getBoolean("fight-good-luck-enabled")) {
            Bukkit.broadcastMessage(message("fight-good-luck"));
        }

        // Schedule match timeout
        scheduleTimeout();

        // Restore config after fight setup
        config.set("current-arena", savedArena);
        config.set("current-kit", savedKit);
    }

    /**
     * Called when the fight ends naturally or via timeout.
     * Determines winner by checking which team still has living members.
     */
    private void endFight() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }

        UUID winner = determineWinner();
        this.winnerId = winner;

        // Notify players
        String winnerName = winner.equals(player1.getUniqueId()) ? player1.getName() : player2.getName();
        String msg = "§7Tournament match ended. Winner: §e" + winnerName;
        player1.sendMessage(msg);
        player2.sendMessage(msg);

        onComplete.accept(winner);
    }

    /**
     * Determine the winner from team survivor state.
     * Falls back to health comparison if both players are alive (timeout scenario).
     */
    private UUID determineWinner() {
        // Check context.teams for surviving members
        if (context != null && context.teams != null) {
            for (Map.Entry<Team, Set<UUID>> entry : context.teams.entrySet()) {
                Set<UUID> members = entry.getValue();
                if (members != null && !members.isEmpty()) {
                    UUID survivor = members.iterator().next();
                    // Verify this player is actually online and alive
                    Player p = Bukkit.getPlayer(survivor);
                    if (p != null && p.isOnline() && !p.isDead()) {
                        return survivor;
                    }
                }
            }
        }

        // Fallback: whoever has more health
        boolean p1Online = player1 != null && player1.isOnline();
        boolean p2Online = player2 != null && player2.isOnline();

        if (p1Online && !p2Online) return player1.getUniqueId();
        if (!p1Online && p2Online) return player2.getUniqueId();

        double h1 = p1Online ? player1.getHealth() : 0;
        double h2 = p2Online ? player2.getHealth() : 0;
        return h1 >= h2 ? player1.getUniqueId() : player2.getUniqueId();
    }

    /**
     * Schedule a timeout that force-ends the match if it runs too long.
     */
    private void scheduleTimeout() {
        if (timeoutSeconds <= 0) return;

        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (context == null || context.cancelled.get()) return;

                // Timeout reached — force-end the fight
                Bukkit.getLogger().info("[ManualTournaments] Match timeout reached ("
                        + timeoutSeconds + "s) — forcibly ending fight");

                // Use health check since both players might still be alive
                UUID winner;
                boolean p1Alive = player1 != null && player1.isOnline() && !player1.isDead();
                boolean p2Alive = player2 != null && player2.isOnline() && !player2.isDead();

                if (p1Alive && !p2Alive) {
                    winner = player1.getUniqueId();
                } else if (!p1Alive && p2Alive) {
                    winner = player2.getUniqueId();
                } else {
                    // Both alive or both dead — health decides
                    double h1 = p1Alive ? player1.getHealth() : 0;
                    double h2 = p2Alive ? player2.getHealth() : 0;
                    winner = h1 >= h2 ? player1.getUniqueId() : player2.getUniqueId();
                }

                String wName = winner.equals(player1.getUniqueId())
                        ? player1.getName() : player2.getName();
                Bukkit.broadcastMessage("§6[§e" + "Match" + "§6] §7Timeout — §e" + wName + " §7wins by default");

                // Trigger fight end with our determined winner
                // We need to cancel the fight and run the callback
                // Since context.onFightEnd runs the normal callback, we set winnerId
                // and then cancel the fight (which triggers the listener which calls onFightEnd)
                TournamentFight.this.winnerId = winner;
                context.cancelled.set(true);
                stopFight();

                // Direct callback in case onFightEnd didn't fire
                if (winnerId != null) {
                    onComplete.accept(winnerId);
                }
            }
        };
        timeoutTask.runTaskLater(Main.getPlugin(), timeoutSeconds * 20L);
    }

    private void setupTeam(Team team) {
        if (team.getName().equals("1")) team.setPrefix(message("team1-prefix"));
        else if (team.getName().equals("2")) team.setPrefix(message("team2-prefix"));
        if (Main.version >= 14) team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        if (!config.getBoolean("friendly-fire")) team.setAllowFriendlyFire(false);
        else team.setAllowFriendlyFire(true);
    }

    private void freezeCountdown(List<Player> fighters) {
        new BukkitRunnable() {
            int countdownTime = config.getInt("countdown-time");
            public void run() {
                for (Player fighter : fighters) {
                    context.frozen.add(fighter.getUniqueId());
                    fighter.setWalkSpeed(0.0F);
                }
                if (countdownTime == 0) {
                    context.frozen.clear();
                    for (Player fighter : fighters) {
                        fighter.setWalkSpeed(0.2F);
                        playSound(fighter);
                    }
                    if (config.getBoolean("fight-good-luck-enabled")) {
                        for (Player fighter : fighters) {
                            fighter.sendMessage(message("fight-good-luck"));
                        }
                    }
                    cancel();
                } else if (context.cancelled.get()) {
                    context.frozen.clear();
                    for (Player fighter : fighters) {
                        fighter.setWalkSpeed(0.2F);
                    }
                    cancel();
                } else {
                    for (Player fighter : fighters) {
                        fighter.sendMessage(String.format(message("fight-will-start"), countdownTime));
                        playNote(fighter);
                    }
                }
                countdownTime--;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
    }

    private static void playSound(Player fighter) {
        if (Main.version >= 18) {
            fighter.playSound(fighter.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        } else {
            fighter.playNote(fighter.getEyeLocation(), Instrument.PIANO, Note.sharp(0, Note.Tone.G));
        }
    }

    private static void playNote(Player fighter) {
        if (Main.version >= 18) {
            fighter.playSound(fighter.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        } else {
            fighter.playNote(fighter.getEyeLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
        }
    }

    // --- FightType interface methods ---

    @Override
    public void startFight(Player player, List<Player> fighters, String arenaName_ignored,
                           Map<Team, Set<UUID>> teams, Scoreboard board) {
        // Not used directly — use startMatch() instead
    }

    @Override
    public void stopFight() {
        if (context == null) return;
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }
        Bukkit.getServer().getOnlinePlayers().forEach(context::removeEntry);
        context.cancelled.set(true);
        Bukkit.getServer().getOnlinePlayers().stream()
                .filter(online -> context.playerIsInTeam(online.getUniqueId()))
                .forEach(online -> {
                    online.setWalkSpeed(0.2F);
                    if (version <= 13) collidableReflection(online, true);
                    if (config.getBoolean("kill-on-fight-end")) online.setHealth(0);
                    else {
                        String path = "fight-end-spawn.";
                        clear(online);
                        if (config.isSet(path)) {
                            online.teleport(location(path, config));
                        }
                    }
                });
        if (listener != null) listener.triggerBlockResetAsync();
        FightFactory.unregisterFight(this);
    }

    @Override
    public boolean canStartFight(String type, Player sender) {
        return true;
    }

    @Override
    public FightContext getContext() {
        return context;
    }

    public UUID getWinnerId() {
        return winnerId;
    }
}
