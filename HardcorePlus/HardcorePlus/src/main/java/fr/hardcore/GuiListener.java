package fr.hardcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.UUID;

public class GuiListener implements Listener {
    private final HardcorePlugin pl;

    public GuiListener(HardcorePlugin pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof MenuHolder) e.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof MenuHolder h)) return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!p.hasPermission("hardcore.admin")) {
            p.closeInventory();
            return;
        }
        int slot = e.getRawSlot();
        switch (h.type) {
            case MAIN -> handleMain(p, h, slot);
            case SANCTION -> handleSanction(p, h, slot, e);
            case BLACKLIST -> handleBlacklist(p, h, slot, e);
        }
    }

    // ---------- MAIN ----------
    private void handleMain(Player p, MenuHolder h, int slot) {
        long hours = pl.data().getHours();
        long delta = switch (slot) {
            case 10 -> -24;
            case 11 -> -1;
            case 15 -> 1;
            case 16 -> 24;
            default -> 0;
        };
        if (delta != 0) {
            pl.data().setHours(hours + delta);
            h.inv.setItem(13, Gui.clockItem(pl.data().getHours()));
        } else if (slot == 20) {
            p.closeInventory();
            pl.awaiting().add(p.getUniqueId());
            p.sendMessage(Gui.MM.deserialize("<gold>Écris le <yellow>pseudo</yellow> du joueur dans le chat <gray>(ou « annuler »)."));
        } else if (slot == 24) {
            Gui.openBlacklist(pl, p, 0);
        }
    }

    // ---------- SANCTIONS ----------
    private void handleSanction(Player p, MenuHolder h, int slot, InventoryClickEvent e) {
        UUID t = h.target;
        for (int i = 0; i < 4; i++) {
            if (slot == Gui.SANCTION_SLOTS[i]) {
                int cur = pl.data().getSanctions(t)[i];
                if (e.isLeftClick()) cur++;
                else if (e.isRightClick()) cur--;
                pl.data().setSanction(t, i, cur);
                h.inv.setItem(slot, Gui.sanctionItem(i, pl.data().getSanctions(t)[i]));
                Player tp = Bukkit.getPlayer(t);
                if (tp != null) SanctionApplier.apply(pl, tp);
                return;
            }
        }
        switch (slot) {
            case 15 -> {
                pl.data().resetSanctions(t);
                Player tp = Bukkit.getPlayer(t);
                if (tp != null) SanctionApplier.apply(pl, tp);
                Gui.openSanction(pl, p, t, h.targetName);
            }
            case 18 -> Gui.openMain(pl, p);
            case 21 -> {
                long until = System.currentTimeMillis() + pl.data().getHours() * 3_600_000L;
                pl.data().tempBan(t, h.targetName, until);
                Player tp = Bukkit.getPlayer(t);
                if (tp != null) tp.kick(HardcorePlugin.banMessage("Tu as été sanctionné par un admin.", until));
                p.closeInventory();
                p.sendMessage(Gui.MM.deserialize("<green>" + h.targetName + " est banni pour " + Gui.fmtHours(pl.data().getHours()) + "."));
            }
            case 23 -> {
                pl.data().blacklistAdd(t, h.targetName);
                Player tp = Bukkit.getPlayer(t);
                if (tp != null) tp.kick(Gui.MM.deserialize("<dark_red><bold>Tu es banni définitivement de ce serveur."));
                p.closeInventory();
                p.sendMessage(Gui.MM.deserialize("<dark_red>" + h.targetName + " a été ajouté à la blacklist."));
            }
            default -> {}
        }
    }

    // ---------- BLACKLIST ----------
    private void handleBlacklist(Player p, MenuHolder h, int slot, InventoryClickEvent e) {
        if (slot == 45) Gui.openBlacklist(pl, p, h.page - 1);
        else if (slot == 53) Gui.openBlacklist(pl, p, h.page + 1);
        else if (slot == 49) Gui.openMain(pl, p);
        else if (slot >= 0 && slot < 45 && slot < h.entries.size() && e.isShiftClick()) {
            UUID id = h.entries.get(slot);
            String name = pl.data().getBlacklist().get(id);
            pl.data().blacklistRemove(id);
            p.sendMessage(Gui.MM.deserialize("<green>" + name + " a été retiré de la blacklist."));
            Gui.openBlacklist(pl, p, h.page);
        }
    }
}
