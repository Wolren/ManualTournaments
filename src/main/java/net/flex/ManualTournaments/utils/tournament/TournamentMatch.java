package net.flex.ManualTournaments.utils.tournament;

import java.util.UUID;

public class TournamentMatch {
    private UUID player1;
    private UUID player2;
    private UUID winner;
    private boolean played;
    private final int round;
    private final int matchIndex;

    public TournamentMatch(UUID player1, UUID player2, int round, int matchIndex) {
        this.player1 = player1;
        this.player2 = player2;
        this.round = round;
        this.matchIndex = matchIndex;
        this.played = false;
    }

    public UUID getPlayer1() { return player1; }
    public UUID getPlayer2() { return player2; }
    public UUID getWinner() { return winner; }
    public boolean isPlayed() { return played; }
    public int getRound() { return round; }
    public int getMatchIndex() { return matchIndex; }

    public void setPlayer1(UUID player1) { this.player1 = player1; }
    public void setPlayer2(UUID player2) { this.player2 = player2; }

    public void setWinner(UUID winner) {
        this.winner = winner;
        this.played = true;
    }

    public boolean containsPlayer(UUID uuid) {
        return (player1 != null && player1.equals(uuid))
                || (player2 != null && player2.equals(uuid));
    }

    /**
     * Whether this match is a bye (auto-advance because one player was absent).
     */
    public boolean isBye() {
        return player1 == null || player2 == null;
    }
}
