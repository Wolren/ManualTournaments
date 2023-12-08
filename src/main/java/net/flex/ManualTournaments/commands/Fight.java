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
    public static Map<Team, Set<UUID>> teams = new HashMap<>();
    private final List<Player> fighters = new ArrayList<>();
    public static Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
        if (optional(sender) == null && !(sender instanceof ConsoleCommandSender)) return false;
        else player = optional(sender);
        getKitConfig().load(getKitConfigFile());
        getArenaConfig().load(getArenaConfigFile());
        getCustomConfig().load(getCustomConfigFile());
        fighters.clear();
        IntStream.range(1, args.length).mapToObj(i -> Bukkit.getPlayer(args[i])).filter(Objects::nonNull).forEach(fighters::add);
        if (args.length == 1) {
            if (args[0].equals("stop")) {
                FightFactory.fight.stopFight();
                FightFactory.fight = new DefaultFight();
            } else if (args[0].equals("queue")) {
                FightType currentFight = new FightFactory().createFight(args[0]);
                currentFight.startFight(player, Collections.emptyList());
            }
        }
        else if (args.length > 2 && (FightFactory.fightTypesMap.containsKey(args[0].toUpperCase()) || args[0].equalsIgnoreCase("stop"))) {
            FightType currentFight = new FightFactory().createFight(args[0]);
            currentFight.startFight(player, fighters);
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