package p1xel.minecraft.bukkit.object.action;

import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.object.focus.Focus;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseInstance;
import p1xel.minecraft.bukkit.util.Logger;

import java.util.List;

public class ExpertiseEnableAction extends Action {

    private final List<String> expertiseIds;

    public ExpertiseEnableAction(List<String> expertiseIds) {
        super(ActionType.ENABLE_EXPERTISE);
        this.expertiseIds = expertiseIds;
    }

    public List<String> getExpertiseIds() {
        return expertiseIds;
    }

    @Override
    public boolean execute(Company company, User user) {
        Focus focus = focusManager.getCompanyFocus(company.getUUID()).getFocus();
        for (String expertiseId : expertiseIds) {
            ExpertiseInstance expertiseInstance = focus.getDisabledExpertiseInstance(expertiseId);
            if (expertiseInstance == null) {
                Logger.warn(actionType.getId() + " action failed: Expertise with ID '" + expertiseId + "' not found for company " + company.getUUID());
                return false;
            }

            expertiseInstance.setEnabled(true);
            focusManager.setExpertise(company.getUUID(), expertiseInstance.getString());
            focus.enableExpertiseInstance(expertiseInstance.getId());
        }
        return true;
    }

}