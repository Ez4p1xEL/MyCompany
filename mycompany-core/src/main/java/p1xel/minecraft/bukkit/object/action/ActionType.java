package p1xel.minecraft.bukkit.object.action;

import java.util.HashMap;

public enum ActionType {

    NONE("none"),
    CONSOLE_COMMAND("console_command"),
    DISABLE_EXPERTISE("disable_expertise"),
    ENABLE_EXPERTISE("enable_expertise"),
    SET_PRICE_GROUP("set_price_group"),;

    private final String id;
    private static final HashMap<String, ActionType> NAMEMAP = new HashMap<>();
    ActionType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static ActionType getActionType(String id) {
        return NAMEMAP.get(id.toLowerCase());
    }

    static {
        for (ActionType actionType : ActionType.values()) {
            actionType.NAMEMAP.put(actionType.getId(), actionType);
        }
    }

}
