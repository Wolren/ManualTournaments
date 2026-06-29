package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.fightCommands.DefaultFight;
import net.flex.ManualTournaments.factories.FightFactory;
import net.flex.ManualTournaments.interfaces.FightType;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class Fight implements CommandExecutor, TabCompleter {
    private final List<Player> fighters = new ArrayList<>();

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        Player player = optional(sender);
        if (player == null) return true;
        getKitConfig().load(getKitConfigFile());
        getArenaConfig().load(getArenaConfigFile());
        getPlugin().reloadConfig();
        config = getPlugin().getConfig();
        fighters.clear();
        IntStream.range(1, args.length).mapToObj(i -> Bukkit.getPlayer(args[i])).filter(Objects::nonNull).forEach(fighters::add);
        Map<Team, Set<UUID>> localTeams = new HashMap<>();
        Scoreboard localBoard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        if (args.length == 1) {
            if (args[0].equals("stop")) {
                FightFactory.stopAllFights();
            } else if (args[0].equals("queue")) {
                FightType currentFight = new FightFactory().createFight(args[0], localTeams, player);
                if (currentFight != null) currentFight.startFight(player, Collections.emptyList(), null, localTeams, localBoard);
            }
        }
        else if (args.length > 2 && args[0].equalsIgnoreCase("team") || args[0].equalsIgnoreCase("ffa")) {
            FightType currentFight = new FightFactory().createFight(args[0], localTeams, player);
            if (currentFight != null) currentFight.startFight(player, fighters, null, localTeams, localBoard);
        } else send(player, "fight-usage");
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("stop");
            list.add("team");
            list.add("ffa");
            list.add("queue");
        } else list = Bukkit.getOnlinePlayers().stream().map(Player::getDisplayName).collect(Collectors.toList());
        return list;
    }
}
