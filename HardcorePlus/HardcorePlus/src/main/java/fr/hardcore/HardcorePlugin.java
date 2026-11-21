package fr.hardcore;

import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class HardcorePlugin extends JavaPlugin {
    private DataManager data;
    private final Set<UUID> awaiting = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        data = new DataManager(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        UnbanCommand unban = new UnbanCommand(this);
        getCommand("unban").setExecutor(unban);
        getCommand("unban").setTabCompleter(unban);
        getCommand("hardcore").setExecutor(new HardcoreCommand(this));
        getLogger().info("HardcorePlus activé.");
    }

    @Override
    public void onDisable() {
        if (data != null) data.save();
    }

    public DataManager data() {
        return data;
    }

    public Set<UUID> awaiting() {
        return awaiting;
    }

    public static Component banMessage(String reason, long untilMs) {
        long left = Math.max(0, untilMs - System.currentTimeMillis());
        long s = left / 1000;
        long d = s / 86400; s %= 86400;
        long h = s / 3600; s %= 3600;
        long m = s / 60;
        return Gui.MM.deserialize("<dark_red><bold>☠ HARDCORE ☠</bold><newline><newline><red>" + reason
                + "<newline><gray>Temps restant : <yellow>" + d + "j " + h + "h " + m + "m");
    }
}
