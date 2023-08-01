package net.flex.ManualTournaments.commands.fightCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.commands.Fight;
import net.flex.ManualTournaments.commands.kitCommands.GiveKit;
import net.flex.ManualTournaments.interfaces.FightType;
import net.flex.ManualTournaments.listeners.TeamFightListener;
import net.flex.ManualTournaments.utils.SqlMethods;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class TeamFight implements FightType {
    private static final Set<Player> distinctFighters = new HashSet<>();
    public static Set<UUID> team1 = new HashSet<>(), team2 = new HashSet<>();
    public static Team team1Board = Fight.board.registerNewTeam(message("team1")), team2Board = Fight.board.registerNewTeam(message("team2"));
    public static final Set<String> team1String = new HashSet<>(), team2String = new HashSet<>();
    public static File FightsConfigFile;
    public static FileConfiguration FightsConfig;
    public static int duration;

    @SneakyThrows
    public void startFight(Player player, List<Player> fighters) {
        clearBeforeFight();
        setBoard();
        distinctFighters.addAll(fighters);
        if (distinctFighters.size() == fighters.size()) {
            for (int i = 0; i < fighters.size(); i++) {
                Player fighter = fighters.get(i);
                if (fighter == null) send(player, "fighter-error");
                else {
                    UUID fighterId = fighter.getUniqueId();
                    fighter.setGameMode(GameMode.SURVIVAL);
                    if (Main.version <= 13) collidableReflection(fighter, false);
                    getPlugin().getConfig().load(getCustomConfigFile());
                    if (i < (fighters.size() / 2)) {
                        team1Board.addEntry(fighter.getDisplayName());
                        team1.add(fighterId);
                        fighter.teleport(location("Arenas." + getPlugin().getConfig().getString("current-arena") + ".pos1.", getArenaConfig()));
                    } else if (i >= (fighters.size() / 2)) {
                        team2Board.addEntry(fighter.getDisplayName());
                        team2.add(fighterId);
                        fighter.teleport(location("Arenas." + getPlugin().getConfig().getString("current-arena") + ".pos2.", getArenaConfig()));
                    }
                    GiveKit.setKit(fighter, getPlugin().getConfig().getString("current-kit"));
                    if (getPlugin().getConfig().getBoolean("freeze-on-start")) DefaultFight.freezeOnStart(fighter, fighterId);
                }
            }
            if (getPlugin().getConfig().getBoolean("count-fights")) DefaultFight.countFights();
            if (getPlugin().getConfig().getBoolean("create-fights-folder")) setFightsFolder();
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
            if (team1.contains(online.getUniqueId()) || team2.contains(online.getUniqueId())) {
                if (Main.version <= 13) collidableReflection(player, true);
                if (getPlugin().getConfig().getBoolean("kill-on-fight-end")) online.setHealth(0);
                else if (!getPlugin().getConfig().getBoolean("kill-on-fight-end")) {
                    String path = "fight-end-spawn.";
                    clear(online);
                    online.teleport(location(path, getPlugin().getConfig()));
                }
            }
        }
        team1.clear();
        team2.clear();
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
                    if (TeamFight.team1.isEmpty() && TeamFight.team2.isEmpty()) {
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
        cancelled.set(false);
        distinctFighters.clear();
    }

    private void setBoard() {
        team1Board.setPrefix(message("team1-prefix"));
        team2Board.setPrefix(message("team2-prefix"));
        if (Main.version >= 14) {
            team1Board.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team2Board.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        if (!getPlugin().getConfig().getBoolean("friendly-fire")) {
            team1Board.setAllowFriendlyFire(false);
            team2Board.setAllowFriendlyFire(false);
        } else if (getPlugin().getConfig().getBoolean("friendly-fire")) {
            team1Board.setAllowFriendlyFire(true);
            team2Board.setAllowFriendlyFire(true);
        }
        for (Player online : Bukkit.getOnlinePlayers()) online.setScoreboard(Fight.board);
    }

    @SneakyThrows
    private void setFightsFolder() {
        duration = 0;
        TeamFightListener.stopper = true;
        new BukkitRunnable() {
            public void run() {
                if (!TeamFightListener.stopper || cancelled.get()) cancel();
                else duration++;
            }
        }.runTaskTimer(getPlugin(), 0L, 20L);
        createFightsFolder();
        FightsConfig.set("team1", teamList(team1, team1String));
        FightsConfig.set("team2", teamList(team2, team2String));
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
