package p1xel.minecraft.bukkit.object.action;

import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.ManagerSupport;
import p1xel.minecraft.bukkit.object.User;

public class Action extends ManagerSupport {

    protected final ActionType actionType;

    public Action(ActionType actionType) {
        this.actionType = actionType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public boolean execute(Company company, User user) {
        // Implement the action execution logic here
        return true;
    }

}
