package net.flex.ManualTournaments.commands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.FightCommand.FightHandler;
import net.flex.ManualTournaments.commands.FightCommand.FightType;
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
import static net.flex.ManualTournaments.utils.SharedComponents.optional;
import static net.flex.ManualTournaments.utils.SharedComponents.player;

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
        FightType currentFight = new FightHandler().createFight(args[0]);
        if (args.length == 1 && args[0].equals("stop")) {
            return currentFight.stopFight();
        } else if (args.length > 2 && distinctFighters.stream().distinct().count() == args.length - 1) {
            return currentFight.startFight(fighters);
        } else return false;
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