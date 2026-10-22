package p1xel.minecraft.bukkit.object.focus.tree.config;

public class ExpertiseConfig {

    private final String expertiseId;
    private final ExpertiseCondition condition;
    private final ExpertiseEffect effect;
    private final ExpertiseAction action;

    public ExpertiseConfig(String expertiseId, ExpertiseCondition condition, ExpertiseEffect effect, ExpertiseAction action) {
        this.expertiseId = expertiseId;
        this.condition = condition;
        this.effect = effect;
        this.action = action;
    }

    public String getExpertiseId() {
        return expertiseId;
    }

    public ExpertiseCondition getCondition() {
        return condition;
    }

    public ExpertiseEffect getEffect() {
        return effect;
    }

    public ExpertiseAction getAction() {
        return action;
    }

}
