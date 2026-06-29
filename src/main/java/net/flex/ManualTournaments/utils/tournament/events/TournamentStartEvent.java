package net.flex.ManualTournaments.utils.tournament.events;

import net.flex.ManualTournaments.utils.tournament.Tournament;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a tournament starts (bracket generation complete, first matches about to begin).
 */
public class TournamentStartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Tournament tournament;

    public TournamentStartEvent(Tournament tournament) {
        this.tournament = tournament;
    }

    public Tournament getTournament() {
        return tournament;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
