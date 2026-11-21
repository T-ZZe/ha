package fr.hardcore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnbanCommand implements CommandExecutor, TabCompleter {
    private final HardcorePlugin pl;

    public UnbanCommand(HardcorePlugin pl) {
        this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        if (!s.hasPermission("hardcore.admin")) {
            s.sendMessage(Gui.MM.deserialize("<red>Permission refusée."));
            return true;
        }
        if (a.length != 1) {
            s.sendMessage(Gui.MM.deserialize("<red>Usage : /unban <pseudo|all>"));
            return true;
        }
        if (a[0].equalsIgnoreCase("all")) {
            int n = pl.data().clearTempBans();
            s.sendMessage(Gui.MM.deserialize("<green>" + n + " ban(s) temporaire(s) levé(s). <gray>(la blacklist n'est pas touchée)"));
            return true;
        }
        UUID id = pl.data().findByName(a[0]);
        if (id == null) {
            s.sendMessage(Gui.MM.deserialize("<red>Ce joueur n'est pas banni."));
            return true;
        }
        boolean t = pl.data().removeTempBan(id);
        boolean b = pl.data().blacklistRemove(id);
        s.sendMessage(Gui.MM.deserialize("<green>" + a[0] + " a été débanni"
                + (b ? " <gray>(retiré de la blacklist)" : "") + "."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1 && s.hasPermission("hardcore.admin")) {
            out.add("all");
            out.addAll(pl.data().bannedNames());
            out.removeIf(n -> !n.toLowerCase().startsWith(a[0].toLowerCase()));
        }
        return out;
    }
}
