package p1xel.minecraft.bukkit.object.effect;

import org.bukkit.Material;

public class ItemDurabilityConsumptionReductionEffect extends Effect {

    private final Material itemMaterial;
    private final String itemMaterialId;
    private final double percentage;
    private final double chance;
    private boolean applyForAllItems = false;

    public ItemDurabilityConsumptionReductionEffect(String itemMaterialId, double percentage, double chance) {
        super(EffectType.ITEM_DURABILITY_SAVE);
        this.percentage = percentage;
        this.chance = chance;
        this.itemMaterialId = itemMaterialId.toLowerCase();
        if (itemMaterialId.equalsIgnoreCase("ALL")) {
            this.itemMaterial = null;
            this.applyForAllItems = true;
        } else {
            this.itemMaterial = Material.matchMaterial(itemMaterialId);
            // 如果这个 itemMaterial 是 null的话
            // 则检查是否为 item-type
            // 然后到config.yml查找
        }
    }

    public ItemDurabilityConsumptionReductionEffect(String itemMaterialId, String args) {
        super(EffectType.ITEM_DURABILITY_SAVE);
        String[] split = args.split(",");
        this.percentage = Double.parseDouble(split[0].trim());
        this.chance = Double.parseDouble(split[1].trim());
        this.itemMaterialId = itemMaterialId.toLowerCase();
        if (itemMaterialId.equalsIgnoreCase("ALL")) {
            this.applyForAllItems = true;
            this.itemMaterial = null;
        } else {
            this.itemMaterial = Material.matchMaterial(itemMaterialId);
        }
    }
    public String getItemMaterialId() {
        return itemMaterialId;
    }

    public Material getItemMaterial() {
        return itemMaterial;
    }

    public double getChance() {
        return chance;
    }

    public double getPercentage() {
        return percentage;
    }

    public boolean isApplyForAllItems() {
        return applyForAllItems;
    }

}
