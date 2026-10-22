package p1xel.minecraft.bukkit.object.effect;

import org.bukkit.entity.EntityType;

public class MobDamageEffect extends Effect {

    private final EntityType entityType;
    private final String entityId;
    private final double amount;
    private final double chance;

    public MobDamageEffect(String entityId, double amount, double chance) {
        super(EffectType.MOB_DAMAGE_MULTIPLY);
        this.entityId = entityId;
        this.amount = amount;
        this.chance = chance;
        if (entityId.equalsIgnoreCase("ALL")) {
            this.entityType = null;
        } else {
            this.entityType = EntityType.valueOf(entityId.toUpperCase());
        }
    }

    public MobDamageEffect(String entityId, String args) {
        super(EffectType.MOB_DAMAGE_MULTIPLY);
        String[] parts = args.split(",");
        String amountStr = parts[0].trim();
        double amount = Double.parseDouble(amountStr);
        String chanceStr = parts[1].trim();
        double chance = Double.parseDouble(chanceStr);
        this.entityId = entityId;
        this.amount = amount;
        this.chance = chance;
        if (entityId.equalsIgnoreCase("ALL")) {
            this.entityType = null;
        } else {
            this.entityType = EntityType.valueOf(entityId.toUpperCase());
        }
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public double getAmount() {
        return amount;
    }

    public double getChance() {
        return chance;
    }

}
