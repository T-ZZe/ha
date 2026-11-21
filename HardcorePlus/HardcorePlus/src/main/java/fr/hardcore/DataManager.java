package fr.hardcore;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
    /** Index des sanctions : 0 = PV, 1 = taille, 2 = degats, 3 = reach */
    public static final int[] MAX = {18, 10, 9, 5};

    private final HardcorePlugin plugin;
    private final File file;
    private final Map<UUID, Long> tempBans = new ConcurrentHashMap<>();
    private final Map<UUID, String> tempNames = new ConcurrentHashMap<>();
    private final Map<UUID, String> blacklist = new ConcurrentHashMap<>();
    private final Map<UUID, int[]> sanctions = new ConcurrentHashMap<>();

    public DataManager(HardcorePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    private void load() {
        YamlConfiguration c = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection s = c.getConfigurationSection("tempbans");
        if (s != null) for (String k : s.getKeys(false)) {
            UUID id = UUID.fromString(k);
            tempBans.put(id, s.getLong(k + ".until"));
            tempNames.put(id, s.getString(k + ".name", "?"));
        }
        s = c.getConfigurationSection("blacklist");
        if (s != null) for (String k : s.getKeys(false))
            blacklist.put(UUID.fromString(k), s.getString(k, "?"));
        s = c.getConfigurationSection("sanctions");
        if (s != null) for (String k : s.getKeys(false)) {
            List<Integer> l = s.getIntegerList(k);
            int[] a = new int[4];
            for (int i = 0; i < 4 && i < l.size(); i++) a[i] = l.get(i);
            sanctions.put(UUID.fromString(k), a);
        }
    }

    public synchronized void save() {
        YamlConfiguration c = new YamlConfiguration();
        tempBans.forEach((id, until) -> {
            c.set("tempbans." + id + ".until", until);
            c.set("tempbans." + id + ".name", tempNames.getOrDefault(id, "?"));
        });
        blacklist.forEach((id, n) -> c.set("blacklist." + id, n));
        sanctions.forEach((id, a) -> {
            if (a[0] + a[1] + a[2] + a[3] > 0)
                c.set("sanctions." + id, List.of(a[0], a[1], a[2], a[3]));
        });
        try {
            plugin.getDataFolder().mkdirs();
            c.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder data.yml : " + e.getMessage());
        }
    }

    // ---- Duree ----
    public long getHours() {
        return Math.max(1, plugin.getConfig().getLong("ban-hours", 120));
    }

    public void setHours(long h) {
        plugin.getConfig().set("ban-hours", Math.max(1, h));
        plugin.saveConfig();
    }

    // ---- Bans temporaires ----
    public void tempBan(UUID id, String name, long untilMs) {
        tempBans.put(id, untilMs);
        tempNames.put(id, name);
        save();
    }

    public Long getTempBanUntil(UUID id) {
        return tempBans.get(id);
    }

    public boolean removeTempBan(UUID id) {
        boolean r = tempBans.remove(id) != null;
        tempNames.remove(id);
        if (r) save();
        return r;
    }

    public int clearTempBans() {
        int n = tempBans.size();
        tempBans.clear();
        tempNames.clear();
        save();
        return n;
    }

    // ---- Blacklist ----
    public void blacklistAdd(UUID id, String name) {
        blacklist.put(id, name);
        save();
    }

    public boolean blacklistRemove(UUID id) {
        boolean r = blacklist.remove(id) != null;
        if (r) save();
        return r;
    }

    public boolean isBlacklisted(UUID id) {
        return blacklist.containsKey(id);
    }

    public Map<UUID, String> getBlacklist() {
        return blacklist;
    }

    // ---- Recherche par pseudo ----
    public UUID findByName(String name) {
        for (var e : tempNames.entrySet()) if (e.getValue().equalsIgnoreCase(name)) return e.getKey();
        for (var e : blacklist.entrySet()) if (e.getValue().equalsIgnoreCase(name)) return e.getKey();
        return null;
    }

    public List<String> bannedNames() {
        List<String> l = new ArrayList<>(tempNames.values());
        l.addAll(blacklist.values());
        return l;
    }

    // ---- Sanctions ----
    public int[] getSanctions(UUID id) {
        return sanctions.computeIfAbsent(id, k -> new int[4]);
    }

    public void setSanction(UUID id, int idx, int level) {
        getSanctions(id)[idx] = Math.max(0, Math.min(MAX[idx], level));
        save();
    }

    public void resetSanctions(UUID id) {
        sanctions.remove(id);
        save();
    }
}
