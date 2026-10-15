package p1xel.minecraft.bukkit.object.focus.tree.config;

public class ExpertiseConfig {

    private final String expertiseId;
    private final ExpertiseCondition condition;

    public ExpertiseConfig(String expertiseId, ExpertiseCondition condition) {
        this.expertiseId = expertiseId;
        this.condition = condition;
    }

    public String getExpertiseId() {
        return expertiseId;
    }

    public ExpertiseCondition getCondition() {
        return condition;
    }

}
