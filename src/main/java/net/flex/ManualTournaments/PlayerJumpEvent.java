package net.flex.ManualTournaments;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlayerJumpEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;

    public PlayerJumpEvent(Player player) {
        super(player);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public static class CallJumpEvent implements Listener {
        public static Map<Player, Boolean> jumping = new HashMap<>();
        public static double jump_vel_border = 0.4;

        @SuppressWarnings("deprecation")
        @EventHandler
        public void onJump(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            double vy = player.getVelocity().getY();
            Material mat = player.getLocation().getBlock().getType();
            boolean isClimbing = mat == Material.LADDER || mat == Material.VINE;
            if (vy > jump_vel_border && !isClimbing && !jumping.get(player)) {
                PlayerJumpEvent jumpEvent = new PlayerJumpEvent(player);
                Bukkit.getServer().getPluginManager().callEvent(jumpEvent);
                if (jumpEvent.isCancelled())
                    player.setVelocity(new Vector(player.getVelocity().getX(), 0, player.getVelocity().getZ()));
                jumping.replace(player, true);
            } else if (player.isOnGround() && jumping.get(player)) jumping.replace(player, false);
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            add(player);
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            remove(player);
        }

        @EventHandler
        public void onEnable(PluginEnableEvent event) {
            for (Player player : Bukkit.getOnlinePlayers()) add(player);
        }

        @EventHandler
        public void onDisable(PluginDisableEvent event) {
            jumping.clear();
        }

        private void remove(Player player) {
            jumping.remove(player);
        }

        private void add(Player player) {
            if (!jumping.containsKey(player)) jumping.put(player, false);
        }
    }
}
