package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.factories.FightFactory;
import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.commands.fightCommands.NullFight;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class Fight implements CommandExecutor, TabCompleter {
    private final Collection<Player> distinctFighters = new java.util.ArrayList<>(Collections.emptyList());
    private final List<Player> fighters = new ArrayList<>();

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (optional(sender) == null) return false;
        else player = optional(sender);
        getKitsConfig().load(getPlugin().KitsConfigfile);
        getCustomConfig().load(getPlugin().customConfigFile);
        getArenaConfig().load(getPlugin().ArenaConfigFile);
        distinctFighters.clear();
        fighters.clear();
        for (int i = 1; i < args.length; i++) {
            Player fighter = Bukkit.getPlayer(args[i]);
            if (fighter != null) {
                distinctFighters.add(fighter);
                fighters.add(fighter);
            }
        }
        if (args.length == 1 && args[0].equals("stop")) {
            FightFactory.fight.stopFight();
            FightFactory.fight = new NullFight();
        } else if (args.length > 2 && (FightFactory.fightTypesMap.containsKey(args[0].toUpperCase()) || args[0].equalsIgnoreCase("stop")) && distinctFighters.stream().distinct().count() == args.length - 1) {
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
}