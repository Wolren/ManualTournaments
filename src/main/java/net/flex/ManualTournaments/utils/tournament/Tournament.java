package net.flex.ManualTournaments.utils.tournament;

import java.util.*;
import java.util.stream.Collectors;

public class Tournament {
    public enum Phase { REGISTRATION, IN_PROGRESS, FINISHED, CANCELLED }

    private final String name;
    private Phase phase;
    private final List<UUID> players;
    private final List<List<TournamentMatch>> bracket;
    private int currentRound;
    private UUID winner;
    private int maxPlayers;
    private int teamSize; // 1 = solo, 2+ = team tournament
    private String arenaName;
    private String kitName;
    private boolean startedByForce;
    private long createdTime;
    private long scheduledStartTime;
    private final List<String> prizeCommands;
    private boolean paused;
    private int matchTimeout;
    // Maps team captain UUID -> all member UUIDs (for teamSize > 1)
    private final Map<UUID, List<UUID>> teamMembers;

    public Tournament(String name, int maxPlayers, String arenaName, String kitName) {
        this.name = name;
        this.maxPlayers = validateMaxPlayers(maxPlayers);
        this.arenaName = arenaName;
        this.kitName = kitName;
        this.phase = Phase.REGISTRATION;
        this.players = new ArrayList<>();
        this.bracket = new ArrayList<>();
        this.currentRound = 0;
        this.startedByForce = false;
        this.createdTime = System.currentTimeMillis();
        this.scheduledStartTime = 0;
        this.prizeCommands = new ArrayList<>();
        this.paused = false;
        this.matchTimeout = 0;
        this.teamSize = 1;
        this.teamMembers = new HashMap<>();
    }

    private static int validateMaxPlayers(int n) {
        int[] allowed = {2, 4, 8, 16, 32, 64};
        for (int a : allowed) {
            if (n == a) return n;
        }
        return 16;
    }

    // --- Getters ---
    public String getName() { return name; }
    public Phase getPhase() { return phase; }
    public List<UUID> getPlayers() { return Collections.unmodifiableList(players); }
    public List<List<TournamentMatch>> getBracket() { return bracket; }
    public int getCurrentRound() { return currentRound; }
    public UUID getWinner() { return winner; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getTeamSize() { return teamSize; }
    public boolean isTeamTournament() { return teamSize > 1; }
    public String getArenaName() { return arenaName; }
    public String getKitName() { return kitName; }
    public boolean isStartedByForce() { return startedByForce; }
    public long getCreatedTime() { return createdTime; }
    public int getPlayerCount() { return players.size(); }
    public long getScheduledStartTime() { return scheduledStartTime; }
    public List<String> getPrizeCommands() { return prizeCommands; }
    public boolean isPaused() { return paused; }

    void setPhase(Phase phase) { this.phase = phase; }

    public void setArenaName(String arenaName) { this.arenaName = arenaName; }
    public void setKitName(String kitName) { this.kitName = kitName; }
    public void setStartedByForce(boolean v) { startedByForce = v; }
    public void setScheduledStartTime(long time) { this.scheduledStartTime = time; }
    public void setPaused(boolean paused) { this.paused = paused; }
    public void addPrizeCommand(String command) { this.prizeCommands.add(command); }
    public void removePrizeCommand(int index) { if (index >= 0 && index < prizeCommands.size()) prizeCommands.remove(index); }
    public void clearPrizeCommands() { this.prizeCommands.clear(); }
    public int getMatchTimeout() { return matchTimeout; }
    public void setMatchTimeout(int seconds) { this.matchTimeout = seconds; }
    public void setTeamSize(int teamSize) { this.teamSize = Math.max(1, teamSize); }

    /**
     * Get all members of a team given its captain UUID.
     * For solo tournaments, returns just the captain.
     */
    public List<UUID> getTeamMembers(UUID captain) {
        if (captain == null) return Collections.emptyList();
        List<UUID> members = teamMembers.get(captain);
        if (members != null) return Collections.unmodifiableList(members);
        return Collections.singletonList(captain);
    }

    /**
     * Get all team captain UUIDs (bracket participants).
     */
    public Set<UUID> getTeamCaptains() {
        return teamMembers.keySet();
    }

    /**
     * Get the number of teams in the bracket.
     */
    public int getTeamCount() {
        return isTeamTournament() ? teamMembers.size() : players.size();
    }

    // --- Player management ---
    public boolean addPlayer(UUID uuid) {
        if (phase != Phase.REGISTRATION) return false;
        if (players.contains(uuid)) return false;
        if (players.size() >= maxPlayers) return false;
        return players.add(uuid);
    }

    public boolean removePlayer(UUID uuid) {
        if (phase != Phase.REGISTRATION) return false;
        return players.remove(uuid);
    }

    public boolean canStart() {
        if (phase != Phase.REGISTRATION) return false;
        if (players.size() < 2) return false;
        // For team tournaments, need at least 2 full teams
        if (isTeamTournament() && players.size() < teamSize * 2) return false;
        return true;
    }

    // --- Bracket generation ---
    public static int nextPowerOf2(int n) {
        if (n <= 0) return 1;
        int p = 1;
        while (p < n) p <<= 1;
        return p;
    }

    public void generateBracket() {
        bracket.clear();
        teamMembers.clear();
        currentRound = 0;

        if (players.size() < 2) {
            phase = Phase.FINISHED;
            return;
        }

        List<UUID> seed = new ArrayList<>(players);
        Collections.shuffle(seed);

        List<UUID> bracketParticipants;

        if (isTeamTournament()) {
            // Group players into teams of teamSize
            int numFullTeams = seed.size() / teamSize;
            List<UUID> captains = new ArrayList<>();
            for (int i = 0; i < numFullTeams; i++) {
                List<UUID> team = new ArrayList<>();
                for (int j = 0; j < teamSize; j++) {
                    team.add(seed.get(i * teamSize + j));
                }
                UUID captain = team.get(0);
                teamMembers.put(captain, team);
                captains.add(captain);
            }
            bracketParticipants = captains;
        } else {
            bracketParticipants = seed;
        }

        int totalSlots = nextPowerOf2(bracketParticipants.size());
        int numRounds = (int) (Math.log(totalSlots) / Math.log(2));
        int byes = totalSlots - bracketParticipants.size();

        List<UUID> roundPlayers = new ArrayList<>(bracketParticipants);
        for (int i = 0; i < byes; i++) {
            roundPlayers.add(null);
        }

        // Round 0
        List<TournamentMatch> firstRound = new ArrayList<>();
        for (int m = 0; m < roundPlayers.size() / 2; m++) {
            UUID p1 = roundPlayers.get(m * 2);
            UUID p2 = roundPlayers.get(m * 2 + 1);
            TournamentMatch match = new TournamentMatch(p1, p2, 0, m);
            if (p1 == null || p2 == null) {
                UUID winner = p1 != null ? p1 : p2;
                match.setWinner(winner);
            }
            firstRound.add(match);
        }
        bracket.add(firstRound);

        // Subsequent rounds
        for (int r = 1; r < numRounds; r++) {
            int matchCount = totalSlots / (int) Math.pow(2, r + 1);
            List<TournamentMatch> round = new ArrayList<>();
            for (int m = 0; m < matchCount; m++) {
                round.add(new TournamentMatch(null, null, r, m));
            }
            bracket.add(round);
        }

        phase = Phase.IN_PROGRESS;
    }

    /**
     * Advance a winner to the next round.
     * Returns list of (round, matchIndex) for matches that should start immediately.
     */
    public List<int[]> advanceWinner(int matchRound, int matchIndex, UUID winnerUuid) {
        List<int[]> matchesToStart = new ArrayList<>();
        if (matchRound >= bracket.size()) return matchesToStart;
        List<TournamentMatch> round = bracket.get(matchRound);
        if (matchIndex >= round.size()) return matchesToStart;

        TournamentMatch match = round.get(matchIndex);
        match.setWinner(winnerUuid);

        boolean isLastRound = (matchRound == bracket.size() - 1);
        if (isLastRound) {
            this.winner = winnerUuid;
            this.phase = Phase.FINISHED;
            return matchesToStart;
        }

        List<TournamentMatch> nextRound = bracket.get(matchRound + 1);
        int nextMatchIndex = matchIndex / 2;
        TournamentMatch nextMatch = nextRound.get(nextMatchIndex);

        if (nextMatch.getPlayer1() == null) {
            nextMatch.setPlayer1(winnerUuid);
        } else {
            nextMatch.setPlayer2(winnerUuid);
        }

        currentRound = matchRound + 1;

        if (nextMatch.getPlayer1() != null && nextMatch.getPlayer2() != null) {
            matchesToStart.add(new int[]{matchRound + 1, nextMatchIndex});
        }
        return matchesToStart;
    }

    public boolean allMatchesInRoundFinished(int round) {
        if (round >= bracket.size()) return false;
        return bracket.get(round).stream().allMatch(TournamentMatch::isPlayed);
    }

    public int getTotalRounds() { return bracket.size(); }

    public TournamentMatch getNextUnplayedMatch(int round) {
        if (round >= bracket.size()) return null;
        return bracket.get(round).stream().filter(m -> !m.isPlayed()).findFirst().orElse(null);
    }

    public TournamentMatch getMatch(int round, int index) {
        if (round >= bracket.size() || index >= bracket.get(round).size()) return null;
        return bracket.get(round).get(index);
    }

    public TournamentMatch getNextMatch() {
        for (int r = 0; r < bracket.size(); r++) {
            TournamentMatch match = getNextUnplayedMatch(r);
            if (match != null) return match;
        }
        return null;
    }

    public int getActiveRound() {
        for (int r = 0; r < bracket.size(); r++) {
            if (!allMatchesInRoundFinished(r)) return r;
        }
        return bracket.size() - 1;
    }

    public void cancel() {
        phase = Phase.CANCELLED;
    }

    // --- Winner info ---

    /**
     * Get display name(s) for a bracket participant (player or team).
     */
    public String getParticipantDisplayName(UUID uuid) {
        if (uuid == null) return "TBD";
        if (isTeamTournament()) {
            List<UUID> members = teamMembers.get(uuid);
            if (members != null) {
                return members.stream()
                        .map(uid -> {
                            org.bukkit.Bukkit.getOfflinePlayer(uid);
                            String n = org.bukkit.Bukkit.getOfflinePlayer(uid).getName();
                            return n != null ? n : uid.toString().substring(0, 8);
                        })
                        .collect(Collectors.joining(", "));
            }
        }
        String n = org.bukkit.Bukkit.getOfflinePlayer(uuid).getName();
        return n != null ? n : uuid.toString().substring(0, 8);
    }

    /**
     * Get a short display name for a team (first player name + " team").
     */
    public String getTeamShortName(UUID captain) {
        if (!isTeamTournament() || captain == null) {
            return getParticipantDisplayName(captain);
        }
        String name = org.bukkit.Bukkit.getOfflinePlayer(captain).getName();
        return (name != null ? name : "?") + "'s team";
    }

    // --- Serialization ---

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("phase", phase.name());
        map.put("maxPlayers", maxPlayers);
        map.put("teamSize", teamSize);
        map.put("arenaName", arenaName != null ? arenaName : "");
        map.put("kitName", kitName != null ? kitName : "");
        map.put("createdTime", createdTime);
        map.put("scheduledStartTime", scheduledStartTime);
        map.put("prizeCommands", prizeCommands);
        map.put("matchTimeout", matchTimeout);
        List<String> uuids = new ArrayList<>();
        for (UUID u : players) uuids.add(u.toString());
        map.put("players", uuids);
        // Serialize team members
        if (isTeamTournament() && !teamMembers.isEmpty()) {
            Map<String, List<String>> serializedTeams = new HashMap<>();
            for (Map.Entry<UUID, List<UUID>> entry : teamMembers.entrySet()) {
                List<String> memberStrs = new ArrayList<>();
                for (UUID m : entry.getValue()) memberStrs.add(m.toString());
                serializedTeams.put(entry.getKey().toString(), memberStrs);
            }
            map.put("teamMembers", serializedTeams);
        }
        if (winner != null) map.put("winner", winner.toString());
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Tournament deserialize(Map<String, Object> map) {
        Tournament t = new Tournament(
                (String) map.get("name"),
                (int) map.get("maxPlayers"),
                (String) map.get("arenaName"),
                (String) map.get("kitName")
        );
        if (map.containsKey("teamSize")) {
            t.teamSize = (int) map.get("teamSize");
        }
        if (map.containsKey("phase")) {
            try {
                t.phase = Phase.valueOf((String) map.get("phase"));
            } catch (IllegalArgumentException ignored) {}
        }
        if (map.containsKey("createdTime")) {
            t.createdTime = (long) map.get("createdTime");
        }
        if (map.containsKey("scheduledStartTime")) {
            t.scheduledStartTime = (long) map.get("scheduledStartTime");
        }
        if (map.containsKey("prizeCommands")) {
            Object raw = map.get("prizeCommands");
            if (raw instanceof List) {
                for (Object cmd : (List<Object>) raw) {
                    if (cmd instanceof String) t.prizeCommands.add((String) cmd);
                }
            }
        }
        if (map.containsKey("matchTimeout")) {
            t.matchTimeout = (int) map.get("matchTimeout");
        }
        if (map.containsKey("players")) {
            for (String s : (List<String>) map.get("players")) {
                try {
                    t.players.add(UUID.fromString(s));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        // Deserialize team members
        if (map.containsKey("teamMembers")) {
            Object raw = map.get("teamMembers");
            if (raw instanceof Map) {
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) raw).entrySet()) {
                    try {
                        UUID captain = UUID.fromString(entry.getKey());
                        if (entry.getValue() instanceof List) {
                            List<UUID> members = new ArrayList<>();
                            for (Object s : (List<Object>) entry.getValue()) {
                                if (s instanceof String) {
                                    try {
                                        members.add(UUID.fromString((String) s));
                                    } catch (IllegalArgumentException ignored) {}
                                }
                            }
                            if (!members.isEmpty()) t.teamMembers.put(captain, members);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
        if (map.containsKey("winner")) {
            try {
                t.winner = UUID.fromString((String) map.get("winner"));
            } catch (IllegalArgumentException ignored) {}
        }
        return t;
    }
}
