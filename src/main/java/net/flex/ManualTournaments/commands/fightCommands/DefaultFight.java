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
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class DefaultFight implements FightType {
    public static Set<UUID> temporary = new HashSet<>();

    static void countdownBeforeFight() {
        new BukkitRunnable() {
            int countdownTime = config.getInt("countdown-time");

            public void run() {
                if (countdownTime == 0) {
                    if (config.getBoolean("fight-good-luck-enabled")) {
                        Bukkit.broadcastMessage(message("fight-good-luck"));
                    }
                    cancel();
                } else if (cancelled.get()) {
                    cancel();
                } else {
                    Bukkit.broadcastMessage(String.format(message("fight-will-start"), countdownTime));
                }

                countdownTime--;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
    }

    @SneakyThrows
    static void countFights() {
        int fightCount = config.getInt("fight-count");
        config.set("fight-count", ++fightCount);
        config.save(getCustomConfigFile());
    }

    static void freezeOnStart(Player fighter, UUID fighterId) {
        new BukkitRunnable() {
            int countdownTime = config.getInt("countdown-time");

            public void run() {
                DefaultFight.temporary.add(fighterId);
                player.setWalkSpeed(0.0F);
                if (countdownTime == 0) {
                    DefaultFight.temporary.clear();
                    player.setWalkSpeed(0.2F);
                    playSound(fighter);
                    cancel();
                } else if (cancelled.get()) {
                    DefaultFight.temporary.clear();
                    player.setWalkSpeed(0.2F);
                    cancel();
                } else playNote(fighter);

                countdownTime--;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
    }

    static void playSound(Player fighter) {
        if (Main.version >= 18) {
            fighter.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        } else {
            fighter.playNote(player.getEyeLocation(), Instrument.PIANO, Note.sharp(0, Note.Tone.G));
        }
    }

    static void playNote(Player fighter) {
        if (Main.version >= 18) {
            fighter.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        } else {
            fighter.playNote(player.getEyeLocation(), Instrument.PIANO, Note.flat(1, Note.Tone.B));
        }
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

