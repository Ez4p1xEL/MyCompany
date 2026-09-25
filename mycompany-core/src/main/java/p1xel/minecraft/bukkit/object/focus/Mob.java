package p1xel.minecraft.bukkit.object.focus;

import p1xel.minecraft.bukkit.object.focus.tree.Expertise;

import java.util.HashMap;
import java.util.Set;

public class Mob extends Focus {

    public Mob(String id, Set<Expertise> expertiseSet) {
        super(id,expertiseSet);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Expertise getExpertise(String id) {
        return expertiseMap.getOrDefault(id, Expertise.empty);
    }
}
