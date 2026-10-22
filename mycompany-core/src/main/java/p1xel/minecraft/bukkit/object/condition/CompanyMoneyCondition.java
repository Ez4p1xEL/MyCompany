package p1xel.minecraft.bukkit.object.condition;

import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.util.storage.Locale;

public class CompanyMoneyCondition extends Condition {

    private final double money;
    public CompanyMoneyCondition(double money, boolean consumed) {
        super(consumed);
        this.money = money;
        this.id = "company_money";
    }

    @Override
    public boolean isConditionMet(User user, Company company) {
        if (money == 0) {
            return true; // 如果money为0，则条件总是满足
        }
        return company.getCash() >= money;
    }

    @Override
    public void consume(User user, Company company) {
        companyManager.takeMoney(company.getUUID(), money);
    }

    @Override
    public String replaceString(String text, User user, Company company) {
        text = text.replace("%i1%", String.valueOf(money)).replace("%i2%", Locale.getYesOrNo(isConsumed()));
        return text;
    }



}
