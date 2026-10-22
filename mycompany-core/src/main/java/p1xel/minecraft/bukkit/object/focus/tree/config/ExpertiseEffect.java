package p1xel.minecraft.bukkit.object.focus.tree.config;

import p1xel.minecraft.bukkit.object.effect.Effect;
import p1xel.minecraft.bukkit.object.effect.EffectType;

import java.util.HashMap;
import java.util.List;

public class ExpertiseEffect {

    private HashMap<EffectType, List<Effect>> effects = new HashMap<>();

    public ExpertiseEffect(HashMap<EffectType, List<Effect>> effects) {
        this.effects = effects;
    }

    public HashMap<EffectType, List<Effect>> getEffects() {
        return effects;
    }


}
