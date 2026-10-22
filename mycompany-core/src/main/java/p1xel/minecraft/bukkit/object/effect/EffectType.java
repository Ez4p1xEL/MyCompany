package p1xel.minecraft.bukkit.object.effect;

import java.util.HashMap;

public enum EffectType {

    MOB_DROPS_ADD("mob_drops_add"),
    MOB_DAMAGE_MULTIPLY("mob_damage_multiply"),;

    private final String id;
    private static HashMap<String, EffectType> NAMEMAP = new HashMap<>();
    EffectType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static EffectType getById(String id) {
        return NAMEMAP.get(id);
    }

    static {
        for (EffectType effectType : EffectType.values()) {
            NAMEMAP.put(effectType.getId(), effectType);
        }
    }
}
