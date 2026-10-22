package p1xel.minecraft.bukkit.object.focus.tree.config;

import p1xel.minecraft.bukkit.object.action.Action;
import p1xel.minecraft.bukkit.object.action.ActionType;
import p1xel.minecraft.bukkit.object.effect.Effect;
import p1xel.minecraft.bukkit.object.effect.EffectType;

import java.util.HashMap;
import java.util.List;

public class ExpertiseAction {

    private HashMap<ActionType, Action> actions = new HashMap<>();

    public ExpertiseAction(HashMap<ActionType, Action> actions) {
        this.actions = actions;
    }

    public HashMap<ActionType, Action> getActions() {
        return actions;
    }

    public Action getAction(ActionType actionType) {
        return actions.get(actionType);
    }


}
