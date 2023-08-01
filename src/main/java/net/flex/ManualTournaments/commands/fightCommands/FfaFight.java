package net.flex.ManualTournaments.commands.fightCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.Fight;
import net.flex.ManualTournaments.commands.kitCommands.GiveKit;
import net.flex.ManualTournaments.interfaces.FightType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class FfaFight implements FightType {
    private static final Set<Player> distinctFighters = new HashSet<>();
    public static Map<Team, List<UUID>> teams = new HashMap<>();
    public static boolean cancelled = false;

    @SneakyThrows
    @Override
    public void startFight(Player player, List<Player> fighters) {
        clearBeforeFight();
        distinctFighters.addAll(fighters);
        if (distinctFighters.size() == fighters.size()) {
            for (Player fighter : fighters) {
                Team team = Fight.board.registerNewTeam("Team " + fighter.getName());
                setBoard(team, fighter);
                teams.put(team, Collections.singletonList(fighter.getUniqueId()));
                fighter.setGameMode(GameMode.SURVIVAL);
                if (Main.version <= 13) collidableReflection(fighter, false);
                getPlugin().getConfig().load(getCustomConfigFile());
                fighter.teleport(location("Arenas." + getPlugin().getConfig().getString("current-arena") + ".pos1.", getArenaConfig()));
                GiveKit.setKit(fighter, getPlugin().getConfig().getString("current-kit"));
                if (getPlugin().getConfig().getBoolean("freeze-on-start"))
                    DefaultFight.freezeOnStart(fighter, fighter.getUniqueId());
            }
            if (getPlugin().getConfig().getBoolean("count-fights")) DefaultFight.countFights();
            if (getPlugin().getConfig().getBoolean("freeze-on-start")) DefaultFight.countdownBeforeFight();
            else if (getPlugin().getConfig().getBoolean("fight-good-luck-enabled"))
                Bukkit.broadcastMessage(message("fight-good-luck"));
        }
    }

    private void setBoard(Team team, Player fighter) {
        team.addEntry(fighter.getDisplayName());
        if (Main.version >= 14) team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        Bukkit.getOnlinePlayers().forEach(online -> online.setScoreboard(Fight.board));
    }

    @Override
    public void stopFight() {
    }

    @Override
    public boolean canStartFight(String type) {
        if (Main.kitNames.contains(getPlugin().getConfig().getString("current-kit"))) {
            if (Main.arenaNames.contains(getPlugin().getConfig().getString("current-arena"))) {
                if (type.equalsIgnoreCase("ffa")) {
                    String path = "Arenas." + getPlugin().getConfig().getString("current-arena") + ".";
                    boolean pos1 = getArenaConfig().isSet(path + "pos1");
                    boolean pos2 = getArenaConfig().isSet(path + "pos2");
                    boolean spectator = getArenaConfig().isSet(path + "spectator");
                    if (pos1 && pos2 && spectator) return true;
                    else {
                        if (!pos1) send(player, "arena-lacks-pos1");
                        if (!pos2) send(player, "arena-lacks-pos2");
                        if (!spectator) send(player, "arena-lacks-spectator");
                    }
                }
            } else send(player, "current-arena-not-set");
        } else send(player, "current-kit-not-set");
        return false;
    }

    private static void clearBeforeFight() {
        Fight.board.getTeams().forEach(Team::unregister);
        teams.clear();
        cancelled = false;
        distinctFighters.clear();
    }
}
