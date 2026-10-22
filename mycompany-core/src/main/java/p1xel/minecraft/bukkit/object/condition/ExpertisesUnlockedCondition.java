package p1xel.minecraft.bukkit.object.condition;

import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.object.focus.CompanyFocus;
import p1xel.minecraft.bukkit.object.focus.Focus;
import p1xel.minecraft.bukkit.util.focus.FocusUtil;
import p1xel.minecraft.bukkit.util.storage.Locale;

import java.util.List;

public class ExpertisesUnlockedCondition extends Condition {

    private final List<String> argsList;
    public ExpertisesUnlockedCondition(List<String> argsList) {
        super(false);
        this.argsList = argsList;
        this.id = "expertises";
    }

    @Override
    public boolean isConditionMet(User user, Company company) {
        if (argsList == null || argsList.isEmpty()) {
            return false; // 如果argsList为空，则条件不满足
        }

        CompanyFocus companyFocus = focusManager.getCompanyFocus(company.getUUID());
        if (companyFocus == null) {
            return false; // 如果公司没有Focus，则条件不满足。但理论上条件会满足，此举是为了防止空指针异常
        }

        Focus focus = companyFocus.getFocus();

        boolean allUnlocked = true;
        for (String args : argsList) {

            // 代表有 orCondition
            if (args.contains(",")) {
                String[] split = args.split(",");
                boolean anyUnlocked = false;
                for (String expertiseId : split) {
                    if (focus.getExpertiseUnlocked(expertiseId)) {
                        anyUnlocked = true;
                        break;
                    }
                }

                if (!anyUnlocked) {
                    allUnlocked = false;
                    break;
                }

                continue;

            }

            if (!focus.getExpertiseUnlocked(args)) {
                allUnlocked = false;
                break;
            }

        }
        return allUnlocked;
    }

    @Override
    public void consume(User user, Company company) {
        // Consume Nothing, as unlocking an expertise is not a consumable action. This method is intentionally left empty.
    }

    @Override
    public String replaceString(String text, User user, Company company) {
        StringBuilder updatedText = new StringBuilder();
        boolean removeFirst = true;
        for (String args : argsList) {
            String cText = text;
            if (args.contains(",")) {
                String[] split = args.split(",");
                String expertiseId1 = split[0];
                String expertiseId2 = split[1];
                cText = cText.replace("%i1%", FocusUtil.EXPERTISE_LABELS.getOrDefault(expertiseId1, expertiseId1)).replace("%i2%", Locale.yaml.getString("or", " OR")).replace("%i3%", expertiseId2);

            } else {
                cText = cText.replace("%i1%", FocusUtil.EXPERTISE_LABELS.getOrDefault(args, args)).replace("%i2%", "").replace("%i3%", "");
            }

            if (removeFirst) {
                updatedText.append(cText);
                removeFirst = false;
            } else {
                updatedText.append("\n").append(cText);
            }
        }
        return updatedText.toString();
    }



}
