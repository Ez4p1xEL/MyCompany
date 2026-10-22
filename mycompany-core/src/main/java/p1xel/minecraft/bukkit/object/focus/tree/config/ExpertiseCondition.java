package p1xel.minecraft.bukkit.object.focus.tree.config;

import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.object.condition.Condition;

import java.util.Set;

public class ExpertiseCondition {

    private final Condition[] conditions;

    public ExpertiseCondition(Condition... conditions) {
        this.conditions = conditions;
    }

    public ExpertiseCondition(Set<Condition> conditions) {
        this.conditions = conditions.toArray(new Condition[0]);
    }

    public Condition[] getConditions() {
        return conditions;
    }

    public boolean checkConditionAndConsume(User user, Company company) {
        boolean isMet = true;

        // 如果有设置money条件，则检查公司余额是否满足条件

        for (Condition condition : conditions) {
            if (!condition.isConditionMet(user, company)) {
                isMet = false;
                return isMet;
            }
        }

        // If isMet = true
        for (Condition condition : conditions) {
            if (condition.isConsumed()) {
                condition.consume(user, company);
            }
        }

        return isMet;
    }

}
