package p1xel.minecraft.bukkit.utils.storage;

import p1xel.minecraft.bukkit.Company;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public abstract class CompanyData {

    // Initialize the database
    public abstract void init();

    public abstract Company getCompany(UUID uniqueId);

    public abstract List<UUID> getCompaniesUUID();

    public abstract List<String> getCompaniesName();

    public abstract void set(UUID uniqueId, String type, String key, Object value);

    public abstract void createCompany(String companyName, UUID playerUniqueId);

    public abstract Object get(UUID uniqueId, String type, String path);

    public abstract String getName(UUID uniqueId);

    public abstract int getId(UUID uniqueId);

    public abstract UUID getFounder(UUID uniqueId);

    public abstract String getFounderName(UUID uniqueId);

    public abstract UUID getEmployer(UUID uniqueId);

    // position here cannot be employer
    @Nullable
    public abstract List<UUID> getEmployeeList(UUID uniqueId, String position);

    public abstract void dismissEmployee(UUID companyUniqueId, UUID employeeUniqueId);

    public abstract void employPlayer(UUID companyUniqueId, UUID playerUniqueId, String position);

    public abstract void disbandCompany(UUID uniqueId);

    public abstract String getFoundDate(UUID uniqueId);

    public abstract int getMemberAmount(UUID uniqueId);

    @Nullable
    public abstract UUID getUUIDFromId(int cid);

    public abstract List<String> getPositions(UUID uniqueId);

    public abstract List<UUID> getAllCompanies();

    public abstract double getCash(UUID uniqueId);

    public abstract void setCash(UUID uniqueId, double amount);

    public abstract double getDailyIncome(UUID uniqueId);

    public abstract double getTotalIncome(UUID uniqueId);

    public abstract void giveMoney(UUID uniqueId, double amount);

    public abstract void takeMoney(UUID uniqueId, double amount);

    // This does not include the sponsorship in config.yml.
    public abstract double getSalary(UUID uniqueId, String position);

    public abstract void setSalary(UUID uniqueId, String position, double amount);

    public abstract void resetDailyIncome(UUID uniqueId);
}
