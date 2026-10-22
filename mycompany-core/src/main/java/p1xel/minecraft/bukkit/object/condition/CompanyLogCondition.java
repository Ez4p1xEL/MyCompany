package p1xel.minecraft.bukkit.object.condition;

import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.CompanyLog;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.Translator;
import p1xel.minecraft.bukkit.util.storage.Locale;

import java.util.List;

public class CompanyLogCondition extends Condition {

    private final List<String> argsList;
    public CompanyLogCondition(List<String> argsList) {
        super(false);
        this.argsList = argsList;
        this.id = "company_log";
    }

    @Override
    public boolean isConditionMet(User user, Company company) {
        if (argsList == null || argsList.isEmpty()) {
            return false; // 如果argsList为空，则条件不满足
        }

        CompanyLog companyLog = company.getCompanyLog();

        boolean allMet = true;
        for (String args : argsList) {

            String[] split = args.split(",");
            if (split.length != 3) {
                allMet = false; // 如果参数数量不为3，则条件不满足
                Logger.warn("Invalid number of arguments for company_log condition: " + args);
                break;
            }

            String logId = split[0];
            String type = split[1];
            double value;
            try {
                value = Double.parseDouble(split[2]);
            } catch (NumberFormatException e) {
                allMet = false;
                Logger.warn("Invalid value for company_log condition: " + split[2]);
                break;
            }

            if (companyLog.getLog(logId+"_"+type) < value) {
                allMet = false;
            }

        }
        return allMet;
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
            String[] split = args.split(",");
            String logId = split[0];
            String type = split[1];
            String value = split[2];
            String translated = Translator.translateLog(logId, type);
            cText = cText.replace("%i1%", Locale.yaml.getString("log." + logId, "").replace("%type%", translated)).replace("%i3%", value);
            cText = cText.replace("%i2%", company.getCompanyLog().getLog(logId + "_" + type) + "");
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
