package net.flex.ManualTournaments.commands.fightCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.Fight;
import net.flex.ManualTournaments.commands.kitCommands.GiveKit;
import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.listeners.TeamFightListener;
import net.flex.ManualTournaments.utils.SqlMethods;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.commands.Fight.teams;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class TeamFight implements FightType {
    public static final Set<String> team1String = new HashSet<>(), team2String = new HashSet<>();
    public static Team team1 = Fight.board.registerNewTeam("1");
    public static Team team2 = Fight.board.registerNewTeam("2");
    public static File FightsConfigFile;
    public static FileConfiguration FightsConfig;
    public static int duration;

    @SneakyThrows
    public void startFight(Player player, List<Player> fighters) {
        if (fighters.stream().anyMatch(Objects::isNull)) {
            send(player, "fighter-error");
            return;
        }
        Set<Player> distinctFighters = new HashSet<>(fighters);
        if (distinctFighters.size() == fighters.size()) {
            clearBeforeFight();
            setBoard(team1);
            setBoard(team2);
            getPlugin().getConfig().load(getCustomConfigFile());
            fighters.forEach(fighter -> {
                fighter.setGameMode(GameMode.SURVIVAL);
                if (Main.version <= 13) collidableReflection(fighter, false);
                Team team = fighters.indexOf(fighter) < (fighters.size() / 2) ? team1 : team2;
                team.addEntry(fighter.getName());
                teams.computeIfAbsent(team, k -> new HashSet<>()).add(fighter.getUniqueId());
                fighter.teleport(location("Arenas." + getPlugin().getConfig().getString("current-arena") + ".pos" + (team == team1 ? "1" : "2") + ".", getArenaConfig()));
                GiveKit.setKit(fighter, getPlugin().getConfig().getString("current-kit"));
                if (getPlugin().getConfig().getBoolean("freeze-on-start"))
                    DefaultFight.freezeOnStart(fighter, fighter.getUniqueId());
            });
            if (getPlugin().getConfig().getBoolean("count-fights")) DefaultFight.countFights();
            if (getPlugin().getConfig().getBoolean("create-fights-folder")) setFightsFolder(team1, team2);
            if (getPlugin().getConfig().getBoolean("mysql-enabled")) SqlMethods.sqlFights();
            if (getPlugin().getConfig().getBoolean("freeze-on-start")) DefaultFight.countdownBeforeFight();
            else if (getPlugin().getConfig().getBoolean("fight-good-luck-enabled"))
                Bukkit.broadcastMessage(message("fight-good-luck"));
        }
    }

    @SneakyThrows
    public void stopFight() {
        player.setWalkSpeed(0.2F);
        removeEntries();
        cancelled.set(true);
        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
            if (Fight.playerIsInTeam(online.getUniqueId())) {
                if (Main.version <= 13) collidableReflection(player, true);
                if (getPlugin().getConfig().getBoolean("kill-on-fight-end")) online.setHealth(0);
                else if (!getPlugin().getConfig().getBoolean("kill-on-fight-end")) {
                    String path = "fight-end-spawn.";
                    clear(online);
                    online.teleport(location(path, getPlugin().getConfig()));
                }
            }
        }
        teams.clear();
        team1String.clear();
        team2String.clear();
        if (!FightsConfig.isSet("cancelled")) {
            FightsConfig.set("cancelled", true);
            FightsConfig.save(FightsConfigFile);
        }
    }

    @Override
    public boolean canStartFight(String type) {
        if (Main.kitNames.contains(getPlugin().getConfig().getString("current-kit"))) {
            if (Main.arenaNames.contains(getPlugin().getConfig().getString("current-arena"))) {
                if (type.equalsIgnoreCase("team")) {
                    if (teams.values().stream().allMatch(Set::isEmpty)) {
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
                    } else send(player, "fight-concurrent");
                }
            } else send(player, "current-arena-not-set");
        } else send(player, "current-kit-not-set");
        return false;
    }

    private static void clearBeforeFight() {
        team1String.clear();
        team2String.clear();
        teams.clear();
        cancelled.set(false);
    }

    private void setBoard(Team team) {
        if (team.getName().equals("1")) team.setPrefix(message("team1-prefix"));
        else if (team.getName().equals("2")) team.setPrefix(message("team2-prefix"));
        if (Main.version >= 14) team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        if (!getPlugin().getConfig().getBoolean("friendly-fire")) team.setAllowFriendlyFire(false);
        else if (getPlugin().getConfig().getBoolean("friendly-fire")) team.setAllowFriendlyFire(true);
        for (Player online : Bukkit.getOnlinePlayers()) online.setScoreboard(Fight.board);
    }

    @SneakyThrows
    private void setFightsFolder(Team team1, Team team2) {
        duration = 0;
        TeamFightListener.stopper = true;
        new BukkitRunnable() {
            public void run() {
                if (!TeamFightListener.stopper || cancelled.get()) cancel();
                else duration++;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
        createFightsFolder();
        FightsConfig.set("team1", teamList());
        FightsConfig.set("team2", teamList());
        FightsConfig.set("damageTeam1", 0);
        FightsConfig.set("damageTeam2", 0);
        FightsConfig.set("regeneratedTeam1", 0);
        FightsConfig.set("regeneratedTeam2", 0);
        FightsConfig.set("arena", getPlugin().getConfig().getString("current-arena"));
        FightsConfig.set("kit", getPlugin().getConfig().getString("current-kit"));
        FightsConfig.set("duration", 0);
        FightsConfig.set("winners", "");
        FightsConfig.save(FightsConfigFile);
    }


    private static void createFightsFolder() {
        File fightsConfigFolder = new File(getPlugin().getDataFolder(), "fights");
        if (!fightsConfigFolder.exists()) {
            boolean create = fightsConfigFolder.mkdir();
            if (!create) getPlugin().getLogger().log(Level.SEVERE, "Failed to create fights directory");
        }
        File[] filesInFightsFolder = fightsConfigFolder.listFiles();
        int i = (filesInFightsFolder != null ? filesInFightsFolder.length : 0) + 1;
        FightsConfigFile = new File(getPlugin().getDataFolder(), "fights/fight" + i + ".yml");
        FightsConfig = new YamlConfiguration();
        YamlConfiguration.loadConfiguration(FightsConfigFile);
    }
}
