package p1xel.minecraft.bukkit.object.condition;

import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.object.focus.CompanyFocus;
import p1xel.minecraft.bukkit.util.focus.FocusUtil;

public class ExpertiseUnlockedCondition extends Condition {

    private final String expertiseId;
    public ExpertiseUnlockedCondition(String expertiseId) {
        super(false);
        this.expertiseId = expertiseId;
        this.id = "expertise";
    }

    @Override
    public boolean isConditionMet(User user, Company company) {
        if (expertiseId == null || expertiseId.isEmpty()) {
            return false; // 如果expertiseId为空，则条件不满足
        }

        CompanyFocus companyFocus = focusManager.getCompanyFocus(company.getUUID());
        if (companyFocus == null) {
            return false; // 如果公司没有Focus，则条件不满足。但理论上条件会满足，此举是为了防止空指针异常
        }

        return companyFocus.getFocus().getExpertiseUnlocked(expertiseId);
    }

    @Override
    public void consume(User user, Company company) {
        // Consume Nothing, as unlocking an expertise is not a consumable action. This method is intentionally left empty.
    }

    @Override
    public String replaceString(String text, User user, Company company) {
        text = text.replace("%i1%", FocusUtil.EXPERTISE_LABELS.getOrDefault(expertiseId, expertiseId)).replace("%i2%", expertiseId);
        return text;
    }



}
