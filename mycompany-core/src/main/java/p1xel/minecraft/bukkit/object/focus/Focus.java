package p1xel.minecraft.bukkit.object.focus;

import p1xel.minecraft.bukkit.object.focus.tree.Expertise;

import java.util.HashMap;
import java.util.Set;

public class Focus implements FocusInterface {

    public final String id;
    public final HashMap<String, Expertise> expertiseMap = new HashMap<>();

    public Focus(String id, Set<Expertise> expertiseSet) {
        this.id = id;
        for (Expertise expertise : expertiseSet) {
            expertiseMap.put(expertise.getId(), expertise);
        }
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public Expertise getExpertise(String id) {
        return null;
    }
}
