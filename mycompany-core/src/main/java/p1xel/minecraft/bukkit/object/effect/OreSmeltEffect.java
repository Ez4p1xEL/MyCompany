package p1xel.minecraft.bukkit.object.effect;

import org.bukkit.Material;

public class OreSmeltEffect extends Effect {

    private final Material blockMaterial;
    private final String blockMaterialId;
    private final double chance;

    public OreSmeltEffect(String blockMaterialId, double chance) {
        super(EffectType.ORE_SMELT);
        this.chance = chance;
        this.blockMaterialId = blockMaterialId;
        if (blockMaterialId.equalsIgnoreCase("ALL")) {
            this.blockMaterial = null;
        } else {
            this.blockMaterial = Material.matchMaterial(blockMaterialId);
        }
    }

    public OreSmeltEffect(String blockMaterialId, String args) {
        super(EffectType.ORE_SMELT);
        this.chance = Double.parseDouble(args.trim());
        this.blockMaterialId = blockMaterialId;
        if (blockMaterialId.equalsIgnoreCase("ALL")) {
            this.blockMaterial = null;
        } else {
            this.blockMaterial = Material.matchMaterial(blockMaterialId);
        }
    }
    public String getBlockMaterialId() {
        return blockMaterialId;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public double getChance() {
        return chance;
    }

}
