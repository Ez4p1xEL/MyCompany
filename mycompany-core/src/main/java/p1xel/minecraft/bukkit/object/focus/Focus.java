package p1xel.minecraft.bukkit.object.focus;

import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseInstance;

import java.util.HashMap;
import java.util.List;

public class Focus implements FocusInterface {

    public final String id;
    public final HashMap<String, ExpertiseInstance> expertiseMap = new HashMap<>();
    public final HashMap<String, ExpertiseInstance> disabledExpertiseMap = new HashMap<>();

    public Focus(String id, List<ExpertiseInstance> expertiseList) {
        this.id = id;
        for (ExpertiseInstance expertise : expertiseList) {
            if (expertise.getEnabled()) {
                expertiseMap.put(expertise.getId(), expertise);
            } else {
                disabledExpertiseMap.put(expertise.getId(), expertise);
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ExpertiseInstance getExpertiseInstance(String id) {
        return expertiseMap.getOrDefault(id, null);
    }

    @Override
    public ExpertiseInstance getDisabledExpertiseInstance(String id) {
        return disabledExpertiseMap.getOrDefault(id, null);
    }

    @Override
    public void disableExpertiseInstance(String id) {
        ExpertiseInstance expertise = expertiseMap.remove(id);
        if (expertise != null) {
            disabledExpertiseMap.put(id, expertise);
        }
    }

    @Override
    public void enableExpertiseInstance(String id) {
        ExpertiseInstance expertise = disabledExpertiseMap.remove(id);
        if (expertise != null) {
            expertiseMap.put(id, expertise);
        }
    }

    @Override
    public boolean getExpertiseUnlocked(String id) {
        ExpertiseInstance expertise;
        if (expertiseMap.containsKey(id)) {
            expertise = expertiseMap.get(id);
        } else if (disabledExpertiseMap.containsKey(id)) {
            expertise = disabledExpertiseMap.get(id);
        } else {
            return false;
        }
        return expertise.getUnlocked();
    }

}
