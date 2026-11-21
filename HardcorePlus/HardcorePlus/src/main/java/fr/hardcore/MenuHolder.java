package fr.hardcore;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MenuHolder implements InventoryHolder {
    public enum Type { MAIN, SANCTION, BLACKLIST }

    public final Type type;
    public final UUID target;
    public final String targetName;
    public int page;
    public final List<UUID> entries = new ArrayList<>();
    public Inventory inv;

    public MenuHolder(Type type, UUID target, String targetName, int page) {
        this.type = type;
        this.target = target;
        this.targetName = targetName;
        this.page = page;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
