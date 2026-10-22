package p1xel.minecraft.bukkit.util;

import org.jetbrains.annotations.NotNull;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.object.condition.*;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseCondition;
import p1xel.minecraft.bukkit.util.storage.Locale;

import java.util.ArrayList;
import java.util.List;

public class ConditionUtil {

    public static List<String> getConditionMessages(List<Condition> conditions, User user, Company company) {
        List<String> messages = new ArrayList<>();
        for (Condition condition : conditions) {
            String id = condition.getId();
            String message = Locale.yaml.getString("condition." + id, "No Description");
            message = condition.replaceString(message, user, company);
            messages.add(message);
        }
        return messages;
    }

    public static Condition getCondition(String conditionId, @NotNull Object value) {

        switch (conditionId) {
            case "company_money" -> {
                double money = Double.parseDouble(value.toString());
                return new CompanyMoneyCondition(money, true);
            }
            case "expertise" -> {
                String expertise_id_unlocked = value.toString();
                return new ExpertiseUnlockedCondition(expertise_id_unlocked);
            }
            case "expertises" -> {
                List<String> expertiseIds = (List<String>) value;
                return new ExpertisesUnlockedCondition(expertiseIds);
            }

            case "company_log" -> {
                List<String> logList = (List<String>) value;
                return new CompanyLogCondition(logList);
            }
        }

        return null;


    }

}
