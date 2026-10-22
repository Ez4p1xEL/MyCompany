package p1xel.minecraft.bukkit.object.condition;

import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.ManagerSupport;
import p1xel.minecraft.bukkit.object.User;

public class Condition extends ManagerSupport {

    private boolean consumed;
    protected String id; // 应在内部初始化

    public Condition(boolean consumed) {
        this.consumed = consumed;
    }

    public boolean isConditionMet(User user, Company company) {
        // 默认条件总是满足
        return true;
    }

    public void consume(User user, Company company) {}

    public boolean checkConditionAndConsume(User user, Company company) {
        if (isConditionMet(user, company)) {
            if (consumed) {
                consume(user, company);
            }
            return true;
        }
        return false;
    }

    public String replaceString(String text, User user, Company company) {
        // 默认不进行任何替换
        return text;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public String getId() {
        return id;
    }

}
