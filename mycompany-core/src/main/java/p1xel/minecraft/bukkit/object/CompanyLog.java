package p1xel.minecraft.bukkit.object;

import java.util.HashMap;
import java.util.UUID;

public class CompanyLog {

    private final UUID companyUniqueId;
    private final HashMap<String, Double> logMap = new HashMap<>();

    public CompanyLog(UUID companyUniqueId) {
        this.companyUniqueId = companyUniqueId;
    }

    public void addLogValue(String logId, double value) {
        if (logMap.get(logId) == null) {
            logMap.put(logId, value);
        } else {
            logMap.put(logId, getLog(logId) + value);
        }
    }

    public double getLog(String logId) {
        if (logMap.get(logId) == null) {
            return 0.0;
        }
        return logMap.get(logId);
    }

}
