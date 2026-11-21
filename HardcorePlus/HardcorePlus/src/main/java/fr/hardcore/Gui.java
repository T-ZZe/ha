package fr.hardcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Gui {
    private Gui() {}

    public static final MiniMessage MM = MiniMessage.miniMessage();
    public static final int[] SANCTION_SLOTS = {10, 11, 12, 13};

    public static Component c(String s) {
        return MM.deserialize("<!italic>" + s);
    }

    public static ItemStack item(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(c(name));
        if (lore.length > 0) meta.lore(Arrays.stream(lore).map(Gui::c).toList());
        it.setItemMeta(meta);
        return it;
    }

    private static void fill(Inventory inv) {
        ItemStack f = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, f);
    }

    public static String fmtHours(long h) {
        return (h / 24) + "j " + (h % 24) + "h";
    }

    // ================= MENU PRINCIPAL =================
    public static ItemStack clockItem(long hours) {
        return item(Material.CLOCK, "<gold><bold>Durée du ban à la mort",
                "<yellow>" + fmtHours(hours) + " <gray>(" + hours + " heures)",
                "<gray>Temps réel (IRL)");
    }

    public static void openMain(HardcorePlugin pl, Player p) {
        MenuHolder h = new MenuHolder(MenuHolder.Type.MAIN, null, null, 0);
        Inventory inv = Bukkit.createInventory(h, 27, c("<dark_red>☠ Hardcore"));
        h.inv = inv;
        fill(inv);
        inv.setItem(10, item(Material.RED_CONCRETE, "<red>- 1 jour"));
        inv.setItem(11, item(Material.ORANGE_CONCRETE, "<gold>- 1 heure"));
        inv.setItem(13, clockItem(pl.data().getHours()));
        inv.setItem(15, item(Material.LIME_CONCRETE, "<green>+ 1 heure"));
        inv.setItem(16, item(Material.GREEN_CONCRETE, "<dark_green>+ 1 jour"));
        inv.setItem(20, item(Material.ANVIL, "<red><bold>Sanctions",
                "<gray>Clique puis écris un pseudo", "<gray>dans le chat."));
        inv.setItem(24, item(Material.WITHER_SKELETON_SKULL, "<dark_red><bold>Blacklist",
                "<gray>Joueurs bannis définitivement.",
                "<yellow>" + pl.data().getBlacklist().size() + " <gray>joueur(s)"));
        p.openInventory(inv);
    }

    // ================= MENU SANCTIONS =================
    public static ItemStack sanctionItem(int idx, int lvl) {
        int max = DataManager.MAX[idx];
        String hint1 = "<gray>Clic gauche : <green>+1";
        String hint2 = "<gray>Clic droit : <red>-1";
        return switch (idx) {
            case 0 -> item(Material.GOLDEN_APPLE, "<red><bold>Retirer des PV",
                    "<gray>Cœurs retirés : <yellow>" + lvl + "<gray>/" + max, hint1, hint2);
            case 1 -> item(Material.SLIME_BALL, "<green><bold>Augmenter la taille",
                    "<gray>Taille : <yellow>+" + (lvl * 10) + "%", hint1, hint2);
            case 2 -> item(Material.BLAZE_POWDER, "<gold><bold>Réduire les dégâts",
                    "<gray>Dégâts infligés : <yellow>-" + (lvl * 10) + "%", hint1, hint2);
            default -> item(Material.SPYGLASS, "<aqua><bold>Réduire le reach",
                    "<gray>Portée : <yellow>-" + (lvl * 0.5) + " bloc(s)", hint1, hint2);
        };
    }

    public static void openSanction(HardcorePlugin pl, Player p, UUID target, String name) {
        MenuHolder h = new MenuHolder(MenuHolder.Type.SANCTION, target, name, 0);
        Inventory inv = Bukkit.createInventory(h, 27, c("<dark_red>Sanctions : " + name));
        h.inv = inv;
        fill(inv);
        int[] s = pl.data().getSanctions(target);
        for (int i = 0; i < 4; i++) inv.setItem(SANCTION_SLOTS[i], sanctionItem(i, s[i]));
        inv.setItem(15, item(Material.MILK_BUCKET, "<white><bold>Retirer toutes les sanctions"));
        inv.setItem(21, item(Material.CLOCK, "<gold><bold>Bannir (durée hardcore)",
                "<gray>Ban de <yellow>" + fmtHours(pl.data().getHours())));
        inv.setItem(23, item(Material.BARRIER, "<dark_red><bold>Bannir définitivement",
                "<gray>Ajoute à la blacklist."));
        inv.setItem(18, item(Material.ARROW, "<gray>Retour"));
        p.openInventory(inv);
    }

    // ================= BLACKLIST =================
    public static void openBlacklist(HardcorePlugin pl, Player p, int page) {
        List<Map.Entry<UUID, String>> all = new ArrayList<>(pl.data().getBlacklist().entrySet());
        all.sort(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER));
        int pages = Math.max(1, (all.size() + 44) / 45);
        page = Math.max(0, Math.min(page, pages - 1));

        MenuHolder h = new MenuHolder(MenuHolder.Type.BLACKLIST, null, null, page);
        Inventory inv = Bukkit.createInventory(h, 54, c("<dark_red>Blacklist (" + (page + 1) + "/" + pages + ")"));
        h.inv = inv;

        for (int i = 0; i < 45; i++) {
            int idx = page * 45 + i;
            if (idx >= all.size()) break;
            var e = all.get(idx);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            OfflinePlayer op = Bukkit.getOfflinePlayer(e.getKey());
            sm.setOwningPlayer(op);
            sm.displayName(c("<red>" + e.getValue()));
            sm.lore(List.of(c("<dark_red>Banni définitivement"), c("<gray>Shift + clic : retirer de la blacklist")));
            head.setItemMeta(sm);
            inv.setItem(i, head);
            h.entries.add(e.getKey());
        }
        ItemStack f = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) inv.setItem(i, f);
        if (page > 0) inv.setItem(45, item(Material.ARROW, "<yellow>Page précédente"));
        inv.setItem(49, item(Material.BARRIER, "<red>Retour"));
        if (page < pages - 1) inv.setItem(53, item(Material.ARROW, "<yellow>Page suivante"));
        p.openInventory(inv);
    }
}
