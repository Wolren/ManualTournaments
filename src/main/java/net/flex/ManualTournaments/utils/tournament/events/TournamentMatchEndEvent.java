package net.flex.ManualTournaments.utils.tournament.events;

import net.flex.ManualTournaments.utils.tournament.Tournament;
import net.flex.ManualTournaments.utils.tournament.TournamentMatch;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a bracket match finishes.
 * Contains the match, winner, and loser UUIDs.
 */
public class TournamentMatchEndEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Tournament tournament;
    private final TournamentMatch match;
    private final UUID winner;
    private final UUID loser;

    public TournamentMatchEndEvent(Tournament tournament, TournamentMatch match, UUID winner, UUID loser) {
        this.tournament = tournament;
        this.match = match;
        this.winner = winner;
        this.loser = loser;
    }

    public Tournament getTournament() { return tournament; }
    public TournamentMatch getMatch() { return match; }
    public UUID getWinner() { return winner; }
    public UUID getLoser() { return loser; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
