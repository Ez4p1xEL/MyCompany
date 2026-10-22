package p1xel.minecraft.bukkit.object.action;

import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.User;

public class PriceGroupSetAction extends Action {

    private final String priceGroupId;

    public PriceGroupSetAction(String priceGroupId) {
        super(ActionType.SET_PRICE_GROUP);
        this.priceGroupId = priceGroupId;
    }

    public String getPriceGroupId() {
        return priceGroupId;
    }

    @Override
    public boolean execute(Company company, User user) {
        companyManager.setPriceGroup(company.getUUID(), priceGroupId);

        return true;
    }

}