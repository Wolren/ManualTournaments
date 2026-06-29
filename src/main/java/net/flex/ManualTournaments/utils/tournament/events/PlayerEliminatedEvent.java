package net.flex.ManualTournaments.utils.tournament.events;

import net.flex.ManualTournaments.utils.tournament.Tournament;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a player is eliminated from a tournament (loses their match).
 */
public class PlayerEliminatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Tournament tournament;
    private final UUID player;
    private final UUID eliminatedBy;

    public PlayerEliminatedEvent(Tournament tournament, UUID player, UUID eliminatedBy) {
        this.tournament = tournament;
        this.player = player;
        this.eliminatedBy = eliminatedBy;
    }

    public Tournament getTournament() { return tournament; }
    public UUID getPlayer() { return player; }
    public UUID getEliminatedBy() { return eliminatedBy; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
