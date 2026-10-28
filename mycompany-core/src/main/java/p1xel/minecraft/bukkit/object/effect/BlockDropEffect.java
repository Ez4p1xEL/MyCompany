package p1xel.minecraft.bukkit.object.effect;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import p1xel.minecraft.bukkit.object.math.NumberType;

public class BlockDropEffect extends Effect {

    private final Material blockMaterial;
    private final String blockMaterialId;
    private final Material material;
    private final String itemId;
    private final int amount;
    private NumberType numberType;
    private final double chance;

    public BlockDropEffect(String blockMaterialId, String itemId, int amount, NumberType numberType, double chance) {
        super(EffectType.BLOCK_DROPS_ADD);
        this.itemId = itemId;
        this.numberType = numberType;
        this.amount = amount;
        this.chance = chance;
        this.blockMaterialId = blockMaterialId;
        if (blockMaterialId.equalsIgnoreCase("ALL")) {
            this.blockMaterial = null;
        } else {
            this.blockMaterial = Material.matchMaterial(blockMaterialId);
        }
        if (itemId.equalsIgnoreCase("ALL")) {
            this.material = null;
        } else {
            this.material = Material.getMaterial(itemId.toUpperCase());
        }
    }

    public BlockDropEffect(String blockMaterialId, String args) {
        super(EffectType.BLOCK_DROPS_ADD);
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
        this.blockMaterialId = blockMaterialId;
        this.itemId = itemId;
        this.amount = amount;
        this.chance = chance;
        if (blockMaterialId.equalsIgnoreCase("ALL")) {
            this.blockMaterial = null;
        } else {
            this.blockMaterial = Material.matchMaterial(blockMaterialId);
        }
        if (itemId.equalsIgnoreCase("ALL")) {
            this.material = null;
        } else {
            this.material = Material.getMaterial(itemId.toUpperCase());
        }
    }
    public String getBlockMaterialId() {
        return blockMaterialId;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public Material getMaterial() {
        return material;
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
