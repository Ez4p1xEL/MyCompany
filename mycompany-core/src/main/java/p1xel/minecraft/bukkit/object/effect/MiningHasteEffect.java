package p1xel.minecraft.bukkit.object.effect;

import org.bukkit.Material;

public class MiningHasteEffect extends Effect {

    private final Material blockMaterial;
    private final String blockMaterialId;
    private final int amplifier;
    private final int duration;
    private final double chance;

    public MiningHasteEffect(String blockMaterialId, int amplifier, int duration, double chance) {
        super(EffectType.MINING_HASTE);
        this.amplifier = amplifier;
        this.duration = duration;
        this.chance = chance;
        this.blockMaterialId = blockMaterialId;
        if (blockMaterialId.equalsIgnoreCase("ALL")) {
            this.blockMaterial = null;
        } else {
            this.blockMaterial = Material.matchMaterial(blockMaterialId);
        }
    }

    public MiningHasteEffect(String blockMaterialId, String args) {
        super(EffectType.MINING_HASTE);
        String[] split = args.split(",");
        this.amplifier = Integer.parseInt(split[0].trim());
        this.duration = Integer.parseInt(split[1].trim());
        this.chance = Double.parseDouble(split[2].trim());
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

    public int getAmplifier() {
        return amplifier;
    }

    public int getDuration() {
        return duration;
    }

}
