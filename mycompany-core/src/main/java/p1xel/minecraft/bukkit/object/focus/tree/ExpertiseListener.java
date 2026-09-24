package p1xel.minecraft.bukkit.object.focus.tree;

import org.bukkit.event.Listener;

public class ExpertiseListener implements Listener {

    private final String name;
    private final String[] expertiseIds;

    public ExpertiseListener(String name, String... expertiseIds) {
        this.name = name;
        this.expertiseIds = expertiseIds;
    }

    public String getName() {
        return name;
    }

    public String[] getExpertiseIds() {
        return expertiseIds;
    }


}
