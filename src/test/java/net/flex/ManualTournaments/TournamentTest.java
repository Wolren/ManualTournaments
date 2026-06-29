package net.flex.ManualTournaments;

import net.flex.ManualTournaments.utils.tournament.Tournament;
import net.flex.ManualTournaments.utils.tournament.TournamentMatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Tournament bracket logic.
 * Tournament has no Bukkit dependencies — pure Java tested directly.
 */
class TournamentTest {

    private static UUID uuid(int seed) {
        return new UUID(seed, seed);
    }

    private Tournament makeTournament(int playerCount) {
        Tournament t = new Tournament("test", 64, "arena", "kit");
        for (int i = 0; i < playerCount; i++) {
            t.addPlayer(uuid(i));
        }
        return t;
    }

    private Tournament makeTeamTournament(int playerCount, int teamSize) {
        Tournament t = new Tournament("team-test", playerCount, "arena", "kit");
        t.setTeamSize(teamSize);
        for (int i = 0; i < playerCount; i++) {
            t.addPlayer(uuid(i));
        }
        return t;
    }

    // === Player management ===

    @Test
    void testAddPlayer() {
        Tournament t = makeTournament(8);
        assertEquals(8, t.getPlayerCount());
        assertTrue(t.getPlayers().contains(uuid(0)));
        assertTrue(t.getPlayers().contains(uuid(7)));
    }

    @Test
    void testAddPlayerDuringBracketRejected() {
        Tournament t = makeTournament(4);
        t.generateBracket();
        assertFalse(t.addPlayer(uuid(99)));
    }

    @Test
    void testRemovePlayer() {
        Tournament t = makeTournament(4);
        assertTrue(t.removePlayer(uuid(0)));
        assertEquals(3, t.getPlayerCount());
    }

    @Test
    void testRemovePlayerAfterStartRejected() {
        Tournament t = makeTournament(4);
        t.generateBracket();
        assertFalse(t.removePlayer(uuid(0)));
    }

    @Test
    void testMaxPlayersEnforced() {
        Tournament t = new Tournament("test", 4, "arena", "kit");
        t.addPlayer(uuid(0));
        t.addPlayer(uuid(1));
        t.addPlayer(uuid(2));
        t.addPlayer(uuid(3));
        assertFalse(t.addPlayer(uuid(4)));
    }

    @Test
    void testDuplicatePlayerRejected() {
        Tournament t = makeTournament(4);
        assertFalse(t.addPlayer(uuid(0)));
    }

    @Test
    void testCanStartMin2() {
        Tournament t = makeTournament(2);
        assertTrue(t.canStart());
    }

    @Test
    void testCanStartMin1Rejected() {
        Tournament t = makeTournament(1);
        assertFalse(t.canStart());
    }

    // === Bracket generation — basic ===

    @Test
    void testBracketGenerated() {
        Tournament t = makeTournament(8);
        t.generateBracket();
        assertEquals(Tournament.Phase.IN_PROGRESS, t.getPhase());
        assertEquals(3, t.getTotalRounds()); // 8 -> 3 rounds
        assertEquals(4, t.getBracket().get(0).size()); // 4 matches in round 0
        assertEquals(2, t.getBracket().get(1).size()); // 2 in round 1
        assertEquals(1, t.getBracket().get(2).size()); // 1 in round 2
    }

    @Test
    void testBracket2Players() {
        Tournament t = makeTournament(2);
        t.generateBracket();
        assertEquals(1, t.getTotalRounds());
        assertEquals(1, t.getBracket().get(0).size());
    }

    @Test
    void testBracket64Players() {
        Tournament t = makeTournament(64);
        t.generateBracket();
        assertEquals(6, t.getTotalRounds()); // 2^6 = 64
        assertEquals(32, t.getBracket().get(0).size());
    }

    @Test
    void testBracketNonPowerOf2() {
        Tournament t = makeTournament(5);
        t.generateBracket();
        assertEquals(3, t.getTotalRounds()); // next power of 2 = 8 -> 3 rounds
        // 5 players -> 8 slots with 3 byes; 2 byes produce auto-played matches
        long played = t.getBracket().get(0).stream().filter(TournamentMatch::isPlayed).count();
        assertTrue(played >= 1, "Expected at least 1 auto-played bye match, got " + played);
    }

    @Test
    void testBracketSinglePlayerFinished() {
        Tournament t = makeTournament(1);
        t.generateBracket();
        assertEquals(Tournament.Phase.FINISHED, t.getPhase());
    }

    // === Bracket progression ===

    @Test
    void testAdvanceWinnerThroughFullBracket() {
        Tournament t = makeTournament(8);
        t.generateBracket();

        // Simulate all matches in round 0
        for (TournamentMatch match : t.getBracket().get(0)) {
            if (match.isBye()) continue;
            assertFalse(match.isPlayed());
            UUID winner = match.getPlayer1(); // p1 always wins
            List<int[]> next = t.advanceWinner(0, match.getMatchIndex(), winner);
            assertTrue(match.isPlayed());
            assertEquals(winner, match.getWinner());
            // After 4 matches in round 0, round 1 should start getting populated
            if (match.getMatchIndex() % 2 == 0) {
                // First match of a pair places winner in next round's player1 slot
                TournamentMatch nextMatch = t.getMatch(1, match.getMatchIndex() / 2);
                assertNotNull(nextMatch);
            }
        }

        // All round 0 matches should be played
        assertTrue(t.allMatchesInRoundFinished(0));

        // Round 1 should have 2 matches (4 winners)
        assertEquals(2, t.getBracket().get(1).size());
        assertTrue(t.getBracket().get(1).stream().allMatch(m -> m.getPlayer1() != null));

        // Advance round 1
        for (TournamentMatch match : t.getBracket().get(1)) {
            UUID winner = match.getPlayer1();
            t.advanceWinner(1, match.getMatchIndex(), winner);
        }

        // Final round should have 1 match
        assertEquals(1, t.getBracket().get(2).size());
        TournamentMatch finals = t.getBracket().get(2).get(0);
        assertNotNull(finals.getPlayer1());
        assertNotNull(finals.getPlayer2());

        // Advance finals
        t.advanceWinner(2, 0, finals.getPlayer1());

        // Tournament should be finished
        assertEquals(Tournament.Phase.FINISHED, t.getPhase());
        assertNotNull(t.getWinner());
    }

    @Test
    void testAdvanceWinnerReturnsNextMatches() {
        Tournament t = makeTournament(4);
        t.generateBracket();

        // Advance first match of round 0 — should not start next round yet
        TournamentMatch m0 = t.getBracket().get(0).get(0);
        List<int[]> next = t.advanceWinner(0, 0, m0.getPlayer1());
        assertTrue(next.isEmpty()); // round not complete

        // Advance second match — round complete, should trigger next
        TournamentMatch m1 = t.getBracket().get(0).get(1);
        next = t.advanceWinner(0, 1, m1.getPlayer1());
        assertFalse(next.isEmpty());
        assertEquals(1, next.get(0)[0]); // round 1
        assertEquals(0, next.get(0)[1]); // match 0
    }

    // === Bye handling ===

    @Test
    void testByesAutoAdvance() {
        Tournament t = makeTournament(3);
        t.generateBracket();
        // 3 players -> 4 slots -> 1 bye
        long byeCount = t.getBracket().get(0).stream().filter(TournamentMatch::isBye).count();
        assertEquals(1, byeCount);
        // Bye matches should auto-play on generation
        long played = t.getBracket().get(0).stream().filter(TournamentMatch::isPlayed).count();
        assertEquals(1, played);
    }

    @Test
    void testByeWinnerNotNull() {
        Tournament t = makeTournament(3);
        t.generateBracket();
        for (TournamentMatch match : t.getBracket().get(0)) {
            if (match.isBye()) {
                assertTrue(match.isPlayed());
                assertNotNull(match.getWinner());
            }
        }
    }

    // === Team tournaments ===

    @Test
    void testTeamTournamentBracket() {
        Tournament t = makeTeamTournament(12, 3); // 12 players, teams of 3 -> 4 teams
        t.generateBracket();
        // 4 teams -> 2 rounds
        assertEquals(2, t.getTotalRounds());
        assertEquals(2, t.getBracket().get(0).size()); // 2 first-round matches
        assertEquals(4, t.getTeamCount());
    }

    @Test
    void testTeamMembersPopulated() {
        Tournament t = makeTeamTournament(8, 2); // 8 players, teams of 2 -> 4 teams
        t.generateBracket();
        // Each team should have 2 members
        for (List<TournamentMatch> round : t.getBracket()) {
            for (TournamentMatch match : round) {
                if (match.getPlayer1() != null) {
                    assertEquals(2, t.getTeamMembers(match.getPlayer1()).size());
                }
                if (match.getPlayer2() != null) {
                    assertEquals(2, t.getTeamMembers(match.getPlayer2()).size());
                }
            }
        }
    }

    @Test
    void testTeamTournamentIncompleteTeams() {
        // 5 players with teamSize 3 -> only 1 full team, need 2 teams to play
        Tournament t = makeTeamTournament(5, 3);
        assertFalse(t.canStart(), "Need at least 6 players for 2 full teams of 3");
    }

    @Test
    void testTeamTournamentAdvance() {
        Tournament t = makeTeamTournament(8, 2); // 4 teams of 2
        t.generateBracket();

        // Advance first match
        TournamentMatch m0 = t.getBracket().get(0).get(0);
        t.advanceWinner(0, 0, m0.getPlayer1());
        t.advanceWinner(0, 1, m0.getPlayer2()); // actually get the other match

        // Actually let me do it properly
        for (TournamentMatch match : t.getBracket().get(0)) {
            if (!match.isPlayed()) {
                t.advanceWinner(0, match.getMatchIndex(), match.getPlayer1());
            }
        }

        // Finals should have both participants
        assertTrue(t.allMatchesInRoundFinished(0));
        TournamentMatch finals = t.getBracket().get(1).get(0);
        assertNotNull(finals.getPlayer1());
        assertNotNull(finals.getPlayer2());
    }

    // === Serialization ===

    @Test
    void testSerializeDeserialize() {
        Tournament t = makeTournament(4);
        t.setTeamSize(2);
        t.addPrizeCommand("give {player} diamond 1");
        t.setMatchTimeout(300);
        t.setScheduledStartTime(1000L);

        java.util.Map<String, Object> data = t.serialize();
        Tournament restored = Tournament.deserialize(data);

        assertEquals(t.getName(), restored.getName());
        assertEquals(t.getMaxPlayers(), restored.getMaxPlayers());
        assertEquals(t.getTeamSize(), restored.getTeamSize());
        assertEquals(t.getArenaName(), restored.getArenaName());
        assertEquals(t.getKitName(), restored.getKitName());
        assertEquals(t.getPlayerCount(), restored.getPlayerCount());
        assertEquals(1, restored.getPrizeCommands().size());
        assertEquals("give {player} diamond 1", restored.getPrizeCommands().get(0));
        assertEquals(300, restored.getMatchTimeout());
    }

    @Test
    void testSerializePhase() {
        Tournament t = makeTournament(4);
        t.generateBracket();
        java.util.Map<String, Object> data = t.serialize();
        Tournament restored = Tournament.deserialize(data);
        // Phase should be preserved from the generated state
        assertEquals(Tournament.Phase.IN_PROGRESS, restored.getPhase());
    }

    // === Edge cases ===

    @Test
    void testAdvanceWinnerInvalidRound() {
        Tournament t = makeTournament(4);
        t.generateBracket();
        List<int[]> next = t.advanceWinner(99, 0, uuid(0));
        assertTrue(next.isEmpty());
    }

    @Test
    void testAdvanceWinnerInvalidMatch() {
        Tournament t = makeTournament(4);
        t.generateBracket();
        List<int[]> next = t.advanceWinner(0, 99, uuid(0));
        assertTrue(next.isEmpty());
    }

    @Test
    void testNextPowerOf2() {
        assertEquals(1, Tournament.nextPowerOf2(0));
        assertEquals(1, Tournament.nextPowerOf2(1));
        assertEquals(2, Tournament.nextPowerOf2(2));
        assertEquals(4, Tournament.nextPowerOf2(3));
        assertEquals(8, Tournament.nextPowerOf2(5));
        assertEquals(64, Tournament.nextPowerOf2(33));
    }

    @Test
    void testCancel() {
        Tournament t = makeTournament(4);
        t.cancel();
        assertEquals(Tournament.Phase.CANCELLED, t.getPhase());
    }

    @Test
    void testGetNextMatch() {
        Tournament t = makeTournament(4);
        t.generateBracket();
        TournamentMatch m = t.getNextMatch();
        assertNotNull(m);
        assertEquals(0, m.getRound());
    }

    @Test
    void testAllMatchesInRoundFinished() {
        Tournament t = makeTournament(4);
        t.generateBracket();
        assertFalse(t.allMatchesInRoundFinished(0));
    }
}
