package fr.hardcore;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

public final class SanctionApplier {
    private SanctionApplier() {}

    public static void apply(HardcorePlugin pl, Player p) {
        int[] s = pl.data().getSanctions(p.getUniqueId());
        set(pl, p, Attribute.MAX_HEALTH, "hp", -2.0 * s[0], AttributeModifier.Operation.ADD_NUMBER);
        set(pl, p, Attribute.SCALE, "size", 0.1 * s[1], AttributeModifier.Operation.ADD_SCALAR);
        set(pl, p, Attribute.ATTACK_DAMAGE, "damage", -0.1 * s[2], AttributeModifier.Operation.ADD_SCALAR);
        set(pl, p, Attribute.ENTITY_INTERACTION_RANGE, "reach_entity", -0.5 * s[3], AttributeModifier.Operation.ADD_NUMBER);
        set(pl, p, Attribute.BLOCK_INTERACTION_RANGE, "reach_block", -0.5 * s[3], AttributeModifier.Operation.ADD_NUMBER);

        AttributeInstance max = p.getAttribute(Attribute.MAX_HEALTH);
        if (max != null && p.getHealth() > max.getValue()) p.setHealth(max.getValue());
    }

    private static void set(HardcorePlugin pl, Player p, Attribute attr, String key, double amount,
                            AttributeModifier.Operation op) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        NamespacedKey nk = new NamespacedKey(pl, key);
        inst.removeModifier(nk);
        if (amount != 0) {
            inst.addModifier(new AttributeModifier(nk, amount, op, EquipmentSlotGroup.ANY));
        }
    }
}
