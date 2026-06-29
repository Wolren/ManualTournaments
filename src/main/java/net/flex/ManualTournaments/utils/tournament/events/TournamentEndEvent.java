package net.flex.ManualTournaments.utils.tournament.events;

import net.flex.ManualTournaments.utils.tournament.Tournament;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a tournament finishes and a winner is determined.
 */
public class TournamentEndEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Tournament tournament;
    private final UUID winner;

    public TournamentEndEvent(Tournament tournament, UUID winner) {
        this.tournament = tournament;
        this.winner = winner;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public UUID getWinner() {
        return winner;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
