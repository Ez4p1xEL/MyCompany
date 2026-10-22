package p1xel.minecraft.bukkit.object.focus;

import p1xel.minecraft.bukkit.object.ManagerSupport;
import p1xel.minecraft.bukkit.object.focus.tree.Expertise;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseInstance;

public interface FocusInterface {

    String getId();

    ExpertiseInstance getExpertiseInstance(String id);

    ExpertiseInstance getDisabledExpertiseInstance(String id);

    void disableExpertiseInstance(String id);

    void enableExpertiseInstance(String id);

    boolean getExpertiseUnlocked(String id);

}
