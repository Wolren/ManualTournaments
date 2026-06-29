package net.flex.ManualTournaments.utils.tournament;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.utils.tournament.events.*;
import net.flex.ManualTournaments.utils.tournament.stats.TournamentStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TournamentManager {

    private static TournamentManager instance;
    private final Map<String, Tournament> tournaments;
    private final Map<String, Set<UUID>> spectators;
    private File configFile;
    private FileConfiguration config;
    private boolean dirty;
    private int saveTaskId;

    private TournamentManager() {
        this.tournaments = new LinkedHashMap<>();
        this.spectators = new HashMap<>();
        this.dirty = false;
        this.saveTaskId = -1;
    }

    public static TournamentManager getInstance() {
        if (instance == null) {
            instance = new TournamentManager();
        }
        return instance;
    }

    public void initialize(File dataFolder) {
        configFile = new File(dataFolder, "tournaments.yml");
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (IOException e) {
                Main.getPlugin().getLogger().log(Level.SEVERE, "Could not create tournaments.yml", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        TournamentDatabase.getInstance().initialize();
        loadAll();
        TournamentStats.getInstance().initialize(dataFolder);
    }

    public void markDirty() {
        this.dirty = true;
        if (saveTaskId > 0) {
            Bukkit.getScheduler().cancelTask(saveTaskId);
        }
        int delay = Math.max(1, Main.getPlugin().getConfig().getInt("tournament-save-debounce", 2)) * 20;
        saveTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (dirty) {
                saveAll();
                dirty = false;
                saveTaskId = -1;
            }
        }, delay);
    }

    public void flush() {
        if (saveTaskId > 0) {
            Bukkit.getScheduler().cancelTask(saveTaskId);
            saveTaskId = -1;
        }
        if (dirty) {
            saveAll();
            dirty = false;
        }
    }

    public void saveAll() {
        if (TournamentDatabase.getInstance().isAvailable()) {
            for (Tournament t : tournaments.values()) {
                TournamentDatabase.getInstance().saveTournament(t);
            }
            return;
        }
        for (Tournament t : tournaments.values()) {
            String path = "tournaments." + t.getName() + ".";
            config.set(path + "name", t.getName());
            config.set(path + "phase", t.getPhase().name());
            config.set(path + "maxPlayers", t.getMaxPlayers());
            config.set(path + "teamSize", t.getTeamSize());
            config.set(path + "arenaName", t.getArenaName());
            config.set(path + "kitName", t.getKitName());
            config.set(path + "createdTime", t.getCreatedTime());
            config.set(path + "scheduledStartTime", t.getScheduledStartTime());
            config.set(path + "prizeCommands", t.getPrizeCommands());
            config.set(path + "paused", t.isPaused());
            config.set(path + "matchTimeout", t.getMatchTimeout());
            List<String> uuids = new ArrayList<>();
            for (UUID u : t.getPlayers()) uuids.add(u.toString());
            config.set(path + "players", uuids);
            if (t.getWinner() != null) {
                config.set(path + "winner", t.getWinner().toString());
            }
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            Main.getPlugin().getLogger().log(Level.SEVERE, "Could not save tournaments.yml", e);
        }
    }

    private void loadAll() {
        tournaments.clear();

        if (TournamentDatabase.getInstance().isAvailable()) {
            Map<String, Tournament> dbTournaments = TournamentDatabase.getInstance().loadAll();
            for (Map.Entry<String, Tournament> entry : dbTournaments.entrySet()) {
                tournaments.put(entry.getKey(), entry.getValue());
            }
            Main.getPlugin().getLogger().info("[Tournament] Loaded " + tournaments.size() + " tournament(s) from MySQL.");
            return;
        }

        if (config.getConfigurationSection("tournaments") == null) return;
        for (String key : config.getConfigurationSection("tournaments").getKeys(false)) {
            String path = "tournaments." + key + ".";
            String name = config.getString(path + "name");
            if (name == null) continue;

            int maxPlayers = config.getInt(path + "maxPlayers", 16);
            String arenaName = config.getString(path + "arenaName", "");
            String kitName = config.getString(path + "kitName", "");

            Tournament t = new Tournament(name, maxPlayers, arenaName, kitName);

            String phaseStr = config.getString(path + "phase", "REGISTRATION");
            try {
                t.setPhase(Tournament.Phase.valueOf(phaseStr));
            } catch (IllegalArgumentException ignored) {}

            if (config.isSet(path + "teamSize")) {
                t.setTeamSize(config.getInt(path + "teamSize"));
            }
            if (config.isSet(path + "scheduledStartTime")) {
                t.setScheduledStartTime(config.getLong(path + "scheduledStartTime"));
            }
            if (config.isSet(path + "prizeCommands")) {
                for (String cmd : config.getStringList(path + "prizeCommands")) {
                    t.addPrizeCommand(cmd);
                }
            }
            if (config.isSet(path + "paused")) {
                t.setPaused(config.getBoolean(path + "paused"));
            }
            if (config.isSet(path + "matchTimeout")) {
                t.setMatchTimeout(config.getInt(path + "matchTimeout"));
            }

            for (String s : config.getStringList(path + "players")) {
                try {
                    t.addPlayer(UUID.fromString(s));
                } catch (IllegalArgumentException ignored) {}
            }

            tournaments.put(name, t);
        }
        Main.getPlugin().getLogger().info("[Tournament] Loaded " + tournaments.size() + " tournament(s) from disk.");
    }

    // --- Tournament CRUD ---

    public boolean createTournament(String name, int maxPlayers, String arenaName, String kitName) {
        if (tournaments.containsKey(name)) return false;
        Tournament t = new Tournament(name, maxPlayers, arenaName, kitName);
        tournaments.put(name, t);
        markDirty();
        return true;
    }

    public Tournament getTournament(String name) {
        return tournaments.get(name);
    }

    public boolean removeTournament(String name) {
        Tournament t = tournaments.remove(name);
        if (t != null) {
            markDirty();
            return true;
        }
        return false;
    }

    public Map<String, Tournament> getTournaments() {
        return Collections.unmodifiableMap(tournaments);
    }

    public List<String> getTournamentNames() {
        return new ArrayList<>(tournaments.keySet());
    }

    // --- Player registration ---

    public boolean joinTournament(String name, UUID player) {
        Tournament t = tournaments.get(name);
        if (t == null) return false;
        boolean result = t.addPlayer(player);
        if (result) markDirty();
        return result;
    }

    public boolean leaveTournament(String name, UUID player) {
        Tournament t = tournaments.get(name);
        if (t == null) return false;

        if (t.getPhase() == Tournament.Phase.REGISTRATION) {
            boolean result = t.removePlayer(player);
            if (result) markDirty();
            return result;
        }

        if (t.getPhase() == Tournament.Phase.IN_PROGRESS) {
            for (List<TournamentMatch> round : t.getBracket()) {
                for (TournamentMatch match : round) {
                    if (match.isPlayed()) continue;
                    if (match.containsPlayer(player)) {
                        UUID winner = match.getPlayer1().equals(player)
                                ? match.getPlayer2()
                                : match.getPlayer1();
                        onMatchComplete(t, match, winner);
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    // --- Lifecycle ---

    public boolean startTournament(String name) {
        Tournament t = tournaments.get(name);
        if (t == null) return false;
        if (!t.canStart()) return false;
        t.generateBracket();
        markDirty();
        TournamentScoreboard.showBoard(t);
        tmsg(t, "tournament-started", new HashMap<String, String>() {{
            put("players", String.valueOf(t.getPlayerCount()));
            put("rounds", String.valueOf(t.getTotalRounds()));
        }});
        Bukkit.getPluginManager().callEvent(new TournamentStartEvent(t));
        scheduleNextMatches(t);
        return true;
    }

    public boolean cancelTournament(String name) {
        Tournament t = tournaments.get(name);
        if (t == null) return false;
        TournamentScoreboard.hideBoard(t);
        t.cancel();
        markDirty();
        tmsg(t, "tournament-cancelled", null);
        return true;
    }

    public void onMatchComplete(Tournament tournament, TournamentMatch match, UUID winner) {
        List<int[]> nextMatches = tournament.advanceWinner(match.getRound(), match.getMatchIndex(), winner);
        markDirty();

        UUID loser = winner.equals(match.getPlayer1()) ? match.getPlayer2() : match.getPlayer1();

        String wName = getName(winner);
        if (match.isBye()) {
            broadcast(tournament, "§7" + wName + " §7advances (bye)");
        } else {
            String loserName = getName(loser);
            broadcast(tournament, "§a" + wName + " §7defeated §c" + loserName);
        }

        if (!match.isBye() && loser != null) {
            Bukkit.getPluginManager().callEvent(new TournamentMatchEndEvent(tournament, match, winner, loser));
            Bukkit.getPluginManager().callEvent(new PlayerEliminatedEvent(tournament, loser, winner));
            TournamentStats.getInstance().recordLoss(loser);
            TournamentStats.getInstance().recordWin(winner);
        }

        for (int[] next : nextMatches) {
            startMatch(tournament, next[0], next[1]);
        }

        if (tournament.getPhase() == Tournament.Phase.FINISHED) {
            announceWinner(tournament);
            TournamentScoreboard.hideBoard(tournament);
        }
    }

    public void scheduleNextMatches(Tournament tournament) {
        if (tournament.getPhase() != Tournament.Phase.IN_PROGRESS) return;
        if (tournament.isPaused()) return;

        int activeRound = tournament.getActiveRound();
        for (int m = 0; m < tournament.getBracket().get(activeRound).size(); m++) {
            TournamentMatch match = tournament.getMatch(activeRound, m);
            if (match == null || match.isPlayed()) continue;

            if (match.isBye()) {
                UUID winner = match.getPlayer1() != null ? match.getPlayer1() : match.getPlayer2();
                onMatchComplete(tournament, match, winner);
            } else if (match.getPlayer1() != null && match.getPlayer2() != null) {
                startMatch(tournament, activeRound, m);
            }
        }
    }

    private void startMatch(Tournament tournament, int round, int matchIndex) {
        if (tournament.isPaused()) return;
        TournamentMatch match = tournament.getMatch(round, matchIndex);
        if (match == null || match.isPlayed()) return;
        if (match.getPlayer1() == null || match.getPlayer2() == null) return;

        UUID p1Id = match.getPlayer1();
        UUID p2Id = match.getPlayer2();

        List<UUID> team1Ids = tournament.getTeamMembers(p1Id);
        List<UUID> team2Ids = tournament.getTeamMembers(p2Id);

        List<Player> team1 = new ArrayList<>();
        List<Player> team2 = new ArrayList<>();

        for (UUID uid : team1Ids) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) team1.add(p);
        }
        for (UUID uid : team2Ids) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) team2.add(p);
        }

        if (team1.isEmpty() || team2.isEmpty()) {
            UUID winner = team1.isEmpty() ? p2Id : p1Id;
            String forfeiter = team1.isEmpty()
                    ? tournament.getTeamShortName(p1Id)
                    : tournament.getTeamShortName(p2Id);
            Bukkit.getLogger().info("[ManualTournaments] Match " + tournament.getName()
                    + " R" + round + "M" + matchIndex + " forfeited - " + forfeiter + " offline");
            broadcast(tournament, "§6Match forfeited — §e" + forfeiter + " §7was offline");
            onMatchComplete(tournament, match, winner);
            return;
        }

        String t1Name = tournament.isTeamTournament()
                ? tournament.getTeamShortName(p1Id)
                : (team1.get(0) != null ? team1.get(0).getName() : "?");
        String t2Name = tournament.isTeamTournament()
                ? tournament.getTeamShortName(p2Id)
                : (team2.get(0) != null ? team2.get(0).getName() : "?");
        broadcast(tournament, "§7⚔ Match: §e" + t1Name + " §7vs §e" + t2Name);
        Bukkit.getLogger().info("[ManualTournaments] " + tournament.getName()
                + " R" + round + " M" + matchIndex + ": " + t1Name + " vs " + t2Name);

        teleportSpectators(tournament);

        TournamentFight fight = new TournamentFight(
                tournament.getArenaName(),
                tournament.getKitName(),
                winner -> onMatchComplete(tournament, match, winner)
        );
        fight.startMatch(team1, team2);
    }

    private void announceWinner(Tournament tournament) {
        UUID winnerId = tournament.getWinner();
        if (winnerId == null) return;
        String winnerName = Bukkit.getOfflinePlayer(winnerId).getName();
        if (winnerName == null) winnerName = winnerId.toString();
        broadcast(tournament, "§6§l" + winnerName + " §7wins §e" + tournament.getName() + "§7!");
        tmsg(tournament, "tournament-winner-global",
                Collections.singletonMap("player", winnerName));

        TournamentPrize.distribute(tournament);

        Bukkit.getPluginManager().callEvent(new TournamentEndEvent(tournament, winnerId));

        if (TournamentStats.getInstance().isEnabled()) {
            for (UUID uid : tournament.getPlayers()) {
                TournamentStats.getInstance().recordTournamentPlayed(uid);
            }
            TournamentStats.getInstance().recordWin(winnerId);
        }

        clearSpectators(tournament);
    }

    // --- Player tournament lookup ---

    public List<Tournament> getPlayerTournaments(UUID player) {
        List<Tournament> result = new ArrayList<>();
        for (Tournament t : tournaments.values()) {
            if (t.getPlayers().contains(player) && t.getPhase() == Tournament.Phase.REGISTRATION) {
                result.add(t);
            }
        }
        return result;
    }

    // --- Substitute ---

    public boolean substitutePlayer(String name, UUID oldPlayer, UUID newPlayer) {
        Tournament t = tournaments.get(name);
        if (t == null || t.getPhase() != Tournament.Phase.REGISTRATION) return false;
        if (!t.getPlayers().contains(oldPlayer)) return false;
        if (t.getPlayers().contains(newPlayer)) return false;
        boolean removed = t.removePlayer(oldPlayer);
        if (!removed) return false;
        boolean added = t.addPlayer(newPlayer);
        if (!added) {
            t.addPlayer(oldPlayer);
            return false;
        }
        markDirty();
        return true;
    }

    // --- Migration ---

    public boolean migrateToMySQL() {
        if (!TournamentDatabase.getInstance().isAvailable()) {
            return false;
        }
        int count = 0;
        for (Tournament t : tournaments.values()) {
            TournamentDatabase.getInstance().saveTournament(t);
            count++;
        }
        Main.getPlugin().getLogger().info("[Tournament] Migrated " + count + " tournament(s) to MySQL.");
        return true;
    }

    // --- Reload ---

    public void reloadAll() {
        loadAll();
        TournamentScheduler.reload();
        Main.getPlugin().getLogger().info("[Tournament] Reloaded " + tournaments.size() + " tournament(s) and schedules.");
    }

    // --- Spectator management ---

    private final Map<UUID, PendingAction> pendingConfirmations = new HashMap<>();

    private static class PendingAction {
        final String description;
        final Runnable action;
        final long expiresAt;
        PendingAction(String description, Runnable action, long timeoutSeconds) {
            this.description = description;
            this.action = action;
            this.expiresAt = System.currentTimeMillis() + timeoutSeconds * 1000;
        }
        boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }

    public void requireConfirm(Player player, String description, Runnable action) {
        int timeout = Main.getPlugin().getConfig().getInt("tournament-confirm-timeout", 10);
        if (timeout <= 0) {
            action.run();
            return;
        }
        pendingConfirmations.put(player.getUniqueId(), new PendingAction(description, action, timeout));
        player.sendMessage("§6Are you sure? §7" + description);
        player.sendMessage("§7Type §e/tournament confirm §7within " + timeout + "s to confirm.");
    }

    public boolean confirmAction(Player player) {
        PendingAction pending = pendingConfirmations.remove(player.getUniqueId());
        if (pending == null) {
            player.sendMessage("§6Nothing to confirm.");
            return false;
        }
        if (pending.isExpired()) {
            player.sendMessage("§6Confirmation expired.");
            return false;
        }
        pending.action.run();
        return true;
    }

    public void clearConfirmations(Player player) {
        pendingConfirmations.remove(player.getUniqueId());
    }

    public boolean kickPlayer(String name, UUID target) {
        Tournament t = tournaments.get(name);
        if (t == null) return false;
        if (t.getPhase() != Tournament.Phase.REGISTRATION) return false;
        boolean removed = t.removePlayer(target);
        if (removed) markDirty();
        return removed;
    }

    private void teleportSpectators(Tournament tournament) {
        String arenaName = tournament.getArenaName();
        if (arenaName == null || !Main.arenaNames.contains(arenaName)) return;
        String path = "Arenas." + arenaName + ".spectator.";
        if (!Main.getArenaConfig().isSet(path)) return;

        for (UUID uid : getSpectators(tournament.getName())) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) {
                p.teleport(net.flex.ManualTournaments.utils.SharedComponents.location(path, Main.getArenaConfig()));
                p.sendMessage("§7Teleported to the current match in §e" + tournament.getName());
            }
        }
    }

    private void clearSpectators(Tournament tournament) {
        Set<UUID> set = spectators.remove(tournament.getName());
        if (set != null) {
            for (UUID uid : set) {
                Player p = Bukkit.getPlayer(uid);
                if (p != null && p.isOnline()) {
                    p.sendMessage("§7The tournament has ended. Use §e/spec stop §7to exit spectate.");
                }
            }
        }
    }

    public void addSpectator(String tournamentName, UUID player) {
        spectators.computeIfAbsent(tournamentName, k -> new HashSet<>()).add(player);
    }

    public void removeSpectator(String tournamentName, UUID player) {
        Set<UUID> set = spectators.get(tournamentName);
        if (set != null) {
            set.remove(player);
            if (set.isEmpty()) spectators.remove(tournamentName);
        }
    }

    public Set<UUID> getSpectators(String tournamentName) {
        return spectators.getOrDefault(tournamentName, Collections.emptySet());
    }

    public boolean isSpectatingAny(UUID player) {
        return spectators.values().stream().anyMatch(s -> s.contains(player));
    }

    // --- Pause / Resume ---

    public boolean pauseTournament(String name) {
        Tournament t = tournaments.get(name);
        if (t == null || t.getPhase() != Tournament.Phase.IN_PROGRESS) return false;
        t.setPaused(true);
        markDirty();
        tmsg(t, "tournament-paused", null);
        return true;
    }

    public boolean resumeTournament(String name) {
        Tournament t = tournaments.get(name);
        if (t == null || t.getPhase() != Tournament.Phase.IN_PROGRESS) return false;
        t.setPaused(false);
        markDirty();
        scheduleNextMatches(t);
        tmsg(t, "tournament-resumed", null);
        return true;
    }

    // --- Force advance ---

    public boolean forceAdvance(String name, UUID winnerUuid) {
        Tournament t = tournaments.get(name);
        if (t == null || t.getPhase() != Tournament.Phase.IN_PROGRESS) return false;

        TournamentMatch match = t.getNextMatch();
        if (match == null || match.isPlayed()) {
            return false;
        }

        String wName = Bukkit.getOfflinePlayer(winnerUuid).getName();
        Bukkit.broadcastMessage("§6[Tournament] §e" + name + " §7match force-advanced — §e"
                + (wName != null ? wName : winnerUuid.toString().substring(0, 8)) + " §7advances");

        onMatchComplete(t, match, winnerUuid);
        return true;
    }

    // --- Delete / cleanup ---

    public boolean deleteTournament(String name) {
        Tournament t = tournaments.remove(name);
        if (t == null) return false;
        TournamentScoreboard.hideBoard(t);
        clearSpectators(t);
        if (TournamentDatabase.getInstance().isAvailable()) {
            TournamentDatabase.getInstance().deleteTournament(name);
        } else {
            config.set("tournaments." + name, null);
            try {
                config.save(configFile);
            } catch (IOException e) {
                Main.getPlugin().getLogger().log(Level.SEVERE, "Could not save tournaments.yml after delete", e);
            }
        }
        return true;
    }

    public int pruneOldTournaments(int maxAgeDays) {
        long cutoff = System.currentTimeMillis() - (long) maxAgeDays * 24 * 60 * 60 * 1000;
        List<String> toRemove = new ArrayList<>();
        for (Tournament t : tournaments.values()) {
            if (t.getPhase() == Tournament.Phase.FINISHED
                    || t.getPhase() == Tournament.Phase.CANCELLED) {
                if (t.getCreatedTime() < cutoff) {
                    toRemove.add(t.getName());
                }
            }
        }
        for (String name : toRemove) {
            deleteTournament(name);
            Main.getPlugin().getLogger().info("[Tournament] Pruned old tournament: " + name);
        }
        return toRemove.size();
    }

    // --- Broadcast helpers ---

    private void broadcast(Tournament tournament, String message) {
        String prefix = "§6[§e" + tournament.getName() + "§6] §7";
        Bukkit.broadcastMessage(prefix + message);
    }

    private void tmsg(Tournament tournament, String configKey, Map<String, String> placeholders) {
        String template = Main.getPlugin().getConfig().getString(configKey);
        if (template == null || template.isEmpty()) return;
        String msg = ChatColor.translateAlternateColorCodes('&', template);
        msg = msg.replace("{name}", tournament.getName() != null ? tournament.getName() : "");
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                msg = msg.replace("{" + e.getKey() + "}", e.getValue() != null ? e.getValue() : "");
            }
        }
        Bukkit.broadcastMessage(msg);
    }

    private String getName(UUID uuid) {
        if (uuid == null) return "?";
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : uuid.toString().substring(0, 8);
    }
}
