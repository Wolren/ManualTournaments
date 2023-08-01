package net.flex.ManualTournaments.commands.fightCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.FightType;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.flex.ManualTournaments.Main.getCustomConfigFile;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.message;
import static net.flex.ManualTournaments.utils.SharedComponents.player;

public class DefaultFight implements FightType {
    public static Set<UUID> temporary = new HashSet<>();
    public static int fightCount = getPlugin().getConfig().getInt("fight-count");

    public static void countdownBeforeFight() {
        (new BukkitRunnable() {
            int i = getPlugin().getConfig().getInt("countdown-time");

            public void run() {
                if (i == 0) {
                    if (getPlugin().getConfig().getBoolean("fight-good-luck-enabled"))
                        Bukkit.broadcastMessage(message("fight-good-luck"));
                    cancel();
                } else if (cancelled.get()) cancel();
                else
                    Bukkit.broadcastMessage(String.format(message("fight-will-start"), i));
                --i;
            }
        }).runTaskTimer(getPlugin(), 0L, 20L);
    }

    public static void freezeOnStart(Player fighter, UUID fighterId) {
        (new BukkitRunnable() {
            int i = getPlugin().getConfig().getInt("countdown-time");

            public void run() {
                DefaultFight.temporary.add(fighterId);
                player.setWalkSpeed(0.0F);
                if (i == 0) {
                    DefaultFight.temporary.clear();
                    player.setWalkSpeed(0.2F);
                    playSound(fighter);
                    cancel();
                } else {
                    if (cancelled.get()) {
                        DefaultFight.temporary.clear();
                        player.setWalkSpeed(0.2F);
                        cancel();
                    } else {
                        playNote(fighter);
                    }
                }

                --i;
            }
        }).runTaskTimer(getPlugin(), 0L, 20L);
    }

    private static void playSound(Player fighter) {
        if (Main.version >= 18)
            fighter.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        else
            fighter.playNote(player.getEyeLocation(), Instrument.PIANO, Note.sharp(0, Note.Tone.G));
    }

    private static void playNote(Player fighter) {
        if (Main.version >= 18)
            fighter.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        else
            fighter.playNote(player.getEyeLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
    }

    @SneakyThrows
    static void countFights() {
        getPlugin().getConfig().set("fight-count", ++fightCount);
        getPlugin().getConfig().save(getCustomConfigFile());
    }

    @Override
    public void startFight(Player player, List<Player> players) {
    }

    @Override
    public void stopFight() {
    }

    @Override
    public boolean canStartFight(String type) {
        return false;
    }
}

