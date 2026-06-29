package net.flex.ManualTournaments.utils.tournament;

import net.flex.ManualTournaments.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;

/**
 * Reads scheduled tournament definitions from config.yml and auto-creates/starts them.
 *
 * Config format:
 *   tournament-schedules:
 *     my-weekly-event:
 *       schedule: "0 18 * * SAT"          # cron-like: minute hour * * dayOfWeek
 *       maxPlayers: 16
 *       arena: "my_arena"
 *       kit: "my_kit"
 *       enabled: true
 *       autoStart: true                   # also auto-start when full or at time
 *       announceMinutes: [60, 30, 15, 5]  # announce before start
 */
public class TournamentScheduler {

    private static BukkitRunnable tickTask;
    private static final Map<String, Tournament> scheduled = new LinkedHashMap<>();
    private static final Map<String, Long> lastStartTimes = new HashMap<>();
    // Cached schedule definitions, reloaded via /tournament reload
    private static final Map<String, CachedSchedule> scheduleCache = new LinkedHashMap<>();

    private static class CachedSchedule {
        final String schedule;
        final int maxPlayers;
        final String arena;
        final String kit;
        final boolean autoStart;
        CachedSchedule(String schedule, int maxPlayers, String arena, String kit, boolean autoStart) {
            this.schedule = schedule;
            this.maxPlayers = maxPlayers;
            this.arena = arena;
            this.kit = kit;
            this.autoStart = autoStart;
        }
    }

    private TournamentScheduler() {}

    /**
     * Start the scheduler tick. Called on plugin enable.
     */
    public static void start() {
        stop();
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        };
        int interval = Math.max(10, Math.min(600, Main.getPlugin().getConfig().getInt("tournament-scheduler-interval", 30)));
        tickTask.runTaskTimer(Main.getPlugin(), 100L, interval * 20L);
        Main.getPlugin().getLogger().info("[Scheduler] Tournament scheduler started (30s interval)");
    }

    /**
     * Stop the scheduler tick.
     */
    public static void stop() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    /**
     * Reload schedules from config.
     */
    public static void reload() {
        scheduled.clear();
        scheduleCache.clear();
        Main.getPlugin().reloadConfig();
        loadSchedules(Main.getPlugin().getConfig());
    }

    private static void loadSchedules(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("tournament-schedules");
        if (section == null) {
            Main.getPlugin().getLogger().info("[Scheduler] No tournament-schedules configured.");
            return;
        }

        for (String key : section.getKeys(false)) {
            String path = "tournament-schedules." + key + ".";
            boolean enabled = config.getBoolean(path + "enabled", true);
            if (!enabled) continue;

            String scheduleStr = config.getString(path + "schedule", "");
            if (scheduleStr.isEmpty()) {
                Main.getPlugin().getLogger().warning("[Scheduler] Schedule '" + key + "' has no schedule pattern.");
                continue;
            }

            int maxPlayers = config.getInt(path + "maxPlayers", 16);
            String arena = config.getString(path + "arena", "");
            String kit = config.getString(path + "kit", "");
            boolean autoStart = config.getBoolean(path + "autoStart", true);
            List<String> announceMinutes = config.getStringList(path + "announceMinutes");

            // Create the tournament definition
            ScheduledTournamentDef def = new ScheduledTournamentDef(
                    key, scheduleStr, maxPlayers, arena, kit, autoStart, announceMinutes
            );
            scheduled.put(key, null); // tournament instance created on first trigger
            lastStartTimes.put(key, 0L);
            scheduleCache.put(key, new CachedSchedule(scheduleStr, maxPlayers, arena, kit, autoStart));

            Main.getPlugin().getLogger().info("[Scheduler] Loaded schedule: '" + key + "' "
                    + scheduleStr + " (" + maxPlayers + "p, " + arena + "/" + kit + ")");
        }
    }

    /**
     * Main tick — check each schedule and trigger if due.
     */
    private static void tick() {
        if (scheduleCache.isEmpty()) return;

        for (Map.Entry<String, CachedSchedule> entry : scheduleCache.entrySet()) {
            String key = entry.getKey();
            CachedSchedule def = entry.getValue();

            // Check if this schedule should fire now
            if (!shouldFire(def.schedule, key)) continue;

            // Don't fire again if we already have a tournament with this name active
            String tName = "scheduled-" + key;
            Tournament existing = TournamentManager.getInstance().getTournament(tName);
            if (existing != null && existing.getPhase() == Tournament.Phase.REGISTRATION) {
                // Already exists — maybe start it if autoStart and full/time
                if (def.autoStart && existing.canStart()) {
                    attemptAutoStart(existing, tName);
                }
                continue;
            }
            if (existing != null && existing.getPhase() == Tournament.Phase.IN_PROGRESS) {
                continue; // already running
            }

            // Create new tournament
            boolean created = TournamentManager.getInstance().createTournament(
                    tName, def.maxPlayers, def.arena, def.kit
            );
            if (created) {
                Tournament t = TournamentManager.getInstance().getTournament(tName);
                if (t != null) {
                    t.setScheduledStartTime(System.currentTimeMillis());
                    Bukkit.broadcastMessage("§6[Tournament] §e" + tName
                            + " §7is now open! Join with §e/tournament join " + tName);
                }

                // Auto-start after creation if configured
                if (def.autoStart) {
                    attemptAutoStart(t, tName);
                }
            }
        }
    }

    private static boolean shouldFire(String scheduleStr, String key) {
        // Parse schedule format: "minute hour * * dayOfWeek" or ISO time "HH:mm"
        try {
            if (scheduleStr.contains(":")) {
                // Simple time format "HH:mm" — runs once per day at that time
                String[] parts = scheduleStr.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                ZonedDateTime now = ZonedDateTime.now();
                if (now.getHour() != hour || now.getMinute() != minute) return false;

                // Only fire once per hour bucket
                long bucket = now.toEpochSecond() / 3600;
                Long lastBucket = lastStartTimes.get(key);
                if (lastBucket != null && lastBucket == bucket) return false;
                lastStartTimes.put(key, bucket);
                return true;
            }
        } catch (NumberFormatException ignored) {}

        return false;
    }

    private static void attemptAutoStart(Tournament t, String name) {
        if (t == null || t.getPhase() != Tournament.Phase.REGISTRATION) return;
        try {
            TournamentManager.getInstance().startTournament(name);
            Bukkit.broadcastMessage("§6[Tournament] §e" + name + " §7has started automatically!");
        } catch (Exception e) {
            Main.getPlugin().getLogger().warning("[Scheduler] Auto-start failed for '" + name + "': " + e.getMessage());
        }
    }

    private static boolean configBoolean(ConfigurationSection section, String key, String field, boolean def) {
        if (section.isSet(key + "." + field)) {
            return section.getBoolean(key + "." + field);
        }
        return def;
    }

    /**
     * Internal definition for scheduled tournaments.
     */
    private static class ScheduledTournamentDef {
        final String name;
        final String schedule;
        final int maxPlayers;
        final String arena;
        final String kit;
        final boolean autoStart;
        final List<String> announceMinutes;

        ScheduledTournamentDef(String name, String schedule, int maxPlayers,
                               String arena, String kit, boolean autoStart,
                               List<String> announceMinutes) {
            this.name = name;
            this.schedule = schedule;
            this.maxPlayers = maxPlayers;
            this.arena = arena;
            this.kit = kit;
            this.autoStart = autoStart;
            this.announceMinutes = announceMinutes;
        }
    }
}
