package net.flex.ManualTournaments.utils.tournament;

import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.utils.gui.GUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class TournamentGUI {

    private static final int[] BRACKET_SLOTS = {
            // Round 0 (leftmost)
            12, 14,
            // Round 1
            21, 23,
            // Round 2 (final)
            31
    };
    // Better layout: 6 rows, bracket flows left to right
    // Row 0: title/info
    // Rounds 1-3: matches
    // Row 5: controls

    private TournamentGUI() {}

    /**
     * Open bracket inventory for a tournament.
     */
    public static void openBracket(Player player, Tournament tournament) {
        int size = Math.min(54, Math.max(27, (tournament.getTotalRounds() + 1) * 9));
        Inventory inv = Bukkit.createInventory(null, size,
                "§8Bracket: §e" + tournament.getName());

        // Fill with glass
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < size; i++) {
            inv.setItem(i, filler);
        }

        // Draw bracket matches
        List<List<TournamentMatch>> bracket = tournament.getBracket();
        if (bracket != null && !bracket.isEmpty()) {
            drawBracket(inv, tournament, bracket);
        }

        // Status bar at bottom
        int statusRow = size - 9;
        ItemStack status = makeItem(Material.PAPER,
                "§6§l" + tournament.getName(),
                Arrays.asList(
                        "§7Phase: §e" + tournament.getPhase().name(),
                        "§7Players: §e" + tournament.getPlayerCount() + "/" + tournament.getMaxPlayers(),
                        "§7Round: §e" + (tournament.getActiveRound() + 1) + "/" + tournament.getTotalRounds(),
                        tournament.getPhase() == Tournament.Phase.FINISHED && tournament.getWinner() != null
                                ? "§7Winner: §6§l" + getName(tournament.getWinner())
                                : ""
                )
        );
        inv.setItem(statusRow, status);

        player.openInventory(inv);
    }

    private static void drawBracket(Inventory inv, Tournament tournament,
                                     List<List<TournamentMatch>> bracket) {
        int numRounds = bracket.size();

        for (int r = 0; r < numRounds; r++) {
            List<TournamentMatch> round = bracket.get(r);
            int roundOffset = r * 9 + 12;

            // Dynamic spacing based on round depth
            int totalSlots = tournament.getMaxPlayers();
            int matchesInRound = totalSlots / (int) Math.pow(2, r + 1);
            int spacing = Math.max(1, (5 - matchesInRound));

            for (int m = 0; m < round.size() && m < 4; m++) {
                TournamentMatch match = round.get(m);
                int slot = roundOffset + m * (2 + spacing);

                if (slot >= inv.getSize() - 9) break;

                inv.setItem(slot - 9, makeMatchItem(match, r, m, tournament));
                if (match != null && match.getPlayer1() != null && match.getPlayer2() != null) {
                    // VS indicator
                    inv.setItem(slot, makeVSBanner(match));
                }
            }
        }

        // Draw connector lines between rounds (conceptual — glass panes)
        // Simple arrows showing progression
        for (int r = 0; r < numRounds - 1; r++) {
            for (int m = 0; m < bracket.get(r).size(); m++) {
                int fromSlot = r * 9 + 12 + m * 2;
                int toSlot = (r + 1) * 9 + 12 + m / 2;
                for (int s = fromSlot + 1; s < toSlot; s++) {
                    if (s < inv.getSize() - 9 && inv.getItem(s) != null
                            && inv.getItem(s).getType() == Material.BLACK_STAINED_GLASS_PANE) {
                        inv.setItem(s, makeConnector());
                    }
                }
            }
        }
    }

    private static ItemStack makeMatchItem(TournamentMatch match, int round, int index, Tournament tournament) {
        List<String> lore = new ArrayList<>();
        lore.add("§7Round " + (round + 1) + " — Match " + (index + 1));

        if (match == null || (match.getPlayer1() == null && match.getPlayer2() == null)) {
            return makeItem(Material.BARRIER,
                    "§7TBD vs TBD", lore);
        }

        String p1Name = tournament != null
                ? tournament.getParticipantDisplayName(match.getPlayer1())
                : getName(match.getPlayer1());
        String p2Name = tournament != null
                ? tournament.getParticipantDisplayName(match.getPlayer2())
                : getName(match.getPlayer2());

        if (match.isBye() && match.isPlayed()) {
            UUID winner = match.getWinner();
            String wName = tournament != null
                    ? tournament.getParticipantDisplayName(winner)
                    : getName(winner);
            lore.add("§e" + wName + " §7(auto-advance)");
            return makeItem(Material.ARROW,
                    "§7Bye — §e" + wName + " §7advances", lore);
        }

        if (match.isPlayed()) {
            UUID winner = match.getWinner();
            String wName = tournament != null
                    ? tournament.getParticipantDisplayName(winner)
                    : getName(winner);
            String loser = winner != null && winner.equals(match.getPlayer1())
                    ? (tournament != null ? tournament.getParticipantDisplayName(match.getPlayer2()) : getName(match.getPlayer2()))
                    : (tournament != null ? tournament.getParticipantDisplayName(match.getPlayer1()) : getName(match.getPlayer1()));
            lore.add("§aWinner: §l" + (wName != null ? wName : "?"));
            lore.add("§7Loser: " + (loser != null ? loser : "?"));
            return makeItem(Material.GOLDEN_SWORD,
                    "§a" + (wName != null ? wName : "?") + " §7vs " + (loser != null ? loser : "?"), lore);
        }

        // Unplayed match
            lore.add("§e" + (p1Name != null ? p1Name : "§7TBD") + " §7vs §e" + (p2Name != null ? p2Name : "§7TBD"));
            lore.add("§7⏳ Waiting");
            return makeItem(Material.IRON_SWORD,
                "§f" + (p1Name != null ? p1Name : "TBD") + " vs " + (p2Name != null ? p2Name : "TBD"), lore);
    }

    private static ItemStack makeVSBanner(TournamentMatch match) {
        if (match.isPlayed()) {
            return makeItem(Material.LIME_STAINED_GLASS_PANE, "§6✓", Collections.singletonList("§7Match complete"));
        }
        return makeItem(Material.RED_STAINED_GLASS_PANE, "§cVS", Collections.singletonList("§7Pending"));
    }

    private static ItemStack makeConnector() {
        ItemStack is = new ItemStack(Material.ARROW);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§8›");
            is.setItemMeta(meta);
        }
        return is;
    }

    private static ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream()
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            }
            is.setItemMeta(meta);
        }
        return is;
    }

    private static String getName(UUID uuid) {
        if (uuid == null) return "?";
        String name = org.bukkit.Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : uuid.toString().substring(0, 8);
    }
}

// Note: TournamentGUI now references the tournament object for team-aware display names.
// The makeMatchItem and other methods use the tournament's getParticipantDisplayName and
// getTeamShortName methods when displaying bracket participants.
