package p1xel.minecraft.bukkit.object.focus;

import p1xel.minecraft.bukkit.object.focus.tree.Expertise;

import java.util.HashMap;
import java.util.Set;

public class Mob implements Focus {

    private final String id;
    private final HashMap<String, Expertise> expertiseMap = new HashMap<>();

    public Mob(String id, Set<Expertise> expertiseSet) {
        this.id = id;
        for (Expertise expertise : expertiseSet) {
            expertiseMap.put(expertise.getId(), expertise);
        }
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
