package p1xel.minecraft.bukkit.object.effect;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import p1xel.minecraft.bukkit.object.math.NumberType;

public class MobDropEffect extends Effect {

    private final EntityType entityType;
    private final Material material;
    private final String entityId;
    private final String itemId;
    private final int amount;
    private NumberType numberType;
    private final double chance;

    public MobDropEffect(String entityId, String itemId, int amount, NumberType numberType, double chance) {
        super(EffectType.MOB_DROPS_ADD);
        this.entityId = entityId;
        this.itemId = itemId;
        this.numberType = numberType;
        this.amount = amount;
        this.chance = chance;
        if (entityId.equalsIgnoreCase("ALL")) {
            this.entityType = null;
        } else {
            this.entityType = EntityType.valueOf(entityId.toUpperCase());
        }
        if (itemId.equalsIgnoreCase("ALL")) {
            this.material = null;
        } else {
            this.material = Material.getMaterial(itemId.toUpperCase());
        }
    }

    public MobDropEffect(String entityId, String args) {
        super(EffectType.MOB_DROPS_ADD);
        String[] parts = args.split(",");
        String itemId = parts[0].trim();
        String amountStr = parts[1].trim();
        this.numberType = NumberType.ADD; // default to ADD if no symbol is found
        for (NumberType type : NumberType.values()) {
            if (amountStr.startsWith(type.getSymbol())) {
                this.numberType = type;
                amountStr = amountStr.substring(type.getSymbol().length());
                break;
            }
        }
        int amount = Integer.parseInt(amountStr);
        double chance = Double.parseDouble(parts[2].trim());
        this.entityId = entityId;
        this.itemId = itemId;
        this.amount = amount;
        this.chance = chance;
        if (entityId.equalsIgnoreCase("ALL")) {
            this.entityType = null;
        } else {
            this.entityType = EntityType.valueOf(entityId.toUpperCase());
        }
        if (itemId.equalsIgnoreCase("ALL")) {
            this.material = null;
        } else {
            this.material = Material.getMaterial(itemId.toUpperCase());
        }
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Material getMaterial() {
        return material;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    public NumberType getNumberType() {
        return numberType;
    }

    public double getChance() {
        return chance;
    }

}
