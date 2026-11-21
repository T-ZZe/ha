package fr.hardcore;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

public class PlayerListener implements Listener {
    private final HardcorePlugin pl;

    public PlayerListener(HardcorePlugin pl) {
        this.pl = pl;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        UUID id = e.getUniqueId();
        DataManager d = pl.data();
        if (d.isBlacklisted(id)) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    Gui.MM.deserialize("<dark_red><bold>Tu es banni définitivement de ce serveur."));
            return;
        }
        Long until = d.getTempBanUntil(id);
        if (until != null) {
            if (until > System.currentTimeMillis()) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        HardcorePlugin.banMessage("Tu es mort en mode hardcore.", until));
            } else {
                d.removeTempBan(id);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (p.hasPermission("hardcore.bypass")) return;
        long until = System.currentTimeMillis() + pl.data().getHours() * 3_600_000L;
        pl.data().tempBan(p.getUniqueId(), p.getName(), until);
        Bukkit.broadcast(Gui.MM.deserialize("<dark_red>☠ <red>" + p.getName()
                + " est mort et est banni pour <yellow>" + Gui.fmtHours(pl.data().getHours()) + "</yellow> !"));
        Bukkit.getScheduler().runTaskLater(pl, () -> {
            if (p.isOnline()) p.kick(HardcorePlugin.banMessage("Tu es mort en mode hardcore.", until));
        }, 2L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        SanctionApplier.apply(pl, e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTask(pl, () -> SanctionApplier.apply(pl, p));
    }

    /** Saisie du pseudo dans le chat apres clic sur l'onglet Sanctions */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if (!pl.awaiting().remove(p.getUniqueId())) return;
        e.setCancelled(true);
        String msg = PlainTextComponentSerializer.plainText().serialize(e.message()).trim();
        Bukkit.getScheduler().runTask(pl, () -> {
            if (msg.equalsIgnoreCase("annuler")) {
                p.sendMessage(Gui.MM.deserialize("<gray>Annulé."));
                return;
            }
            Player online = Bukkit.getPlayerExact(msg);
            OfflinePlayer op = online != null ? online : Bukkit.getOfflinePlayerIfCached(msg);
            if (op == null || (!op.isOnline() && !op.hasPlayedBefore())) {
                p.sendMessage(Gui.MM.deserialize("<red>Joueur introuvable : " + msg.replace("<", "")));
                return;
            }
            String name = op.getName() != null ? op.getName() : msg;
            Gui.openSanction(pl, p, op.getUniqueId(), name);
        });
    }
}
