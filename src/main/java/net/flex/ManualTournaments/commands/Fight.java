package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.fightCommands.DefaultFight;
import net.flex.ManualTournaments.factories.FightFactory;
import net.flex.ManualTournaments.interfaces.FightType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class Fight implements CommandExecutor, TabCompleter {
    public static Map<Team, Set<UUID>> teams = new HashMap<>();
    private final List<Player> fighters = new ArrayList<>();
    public static Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        Main.getKitConfig().load(getKitConfigFile());
        Main.getArenaConfig().load(getArenaConfigFile());
        Main.getCustomConfig().load(getCustomConfigFile());
        fighters.clear();
        IntStream.range(1, args.length).mapToObj(i -> Bukkit.getPlayer(args[i])).filter(Objects::nonNull).forEach(fighters::add);
        if (args.length == 1 && args[0].equals("stop")) {
            FightFactory.fight.stopFight();
            FightFactory.fight = new DefaultFight();
        } else if (args.length > 2 && (FightFactory.fightTypesMap.containsKey(args[0].toUpperCase()) || args[0].equalsIgnoreCase("stop"))) {
            FightType currentFight = new FightFactory().createFight(args[0]);
            currentFight.startFight(player, fighters);
        } else send(player, "fight-usage");
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("stop");
            list.add("team");
        } else for (Player online : Bukkit.getOnlinePlayers()) list.add(online.getDisplayName());
        return list;
    }

    public static boolean playerIsInTeam(UUID player) {
        return teams.values().stream().anyMatch(list -> list.contains(player));
    }

    public static List<UUID> getTeamPlayers(String teamName) {
        List<UUID> UUIDs = new ArrayList<>();
        for (Map.Entry<Team, Set<UUID>> entry : teams.entrySet()) {
            Team team = entry.getKey();
            if (team.getName().equals(teamName)) {
                UUIDs.addAll(entry.getValue());
            }
        }
        return UUIDs;
    }
}