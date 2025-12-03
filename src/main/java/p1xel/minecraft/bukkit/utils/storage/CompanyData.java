package p1xel.minecraft.bukkit.utils.storage;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.Company;
import p1xel.minecraft.bukkit.managers.areas.CompanyArea;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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

    public abstract UUID createShop(UUID uniqueId, Location location, double price, String creatorName);

    @Nullable
    public abstract List<Shop> getShops(UUID uniqueId);

    @Nullable
    public abstract List<UUID> getShopsUUID(UUID uniqueId);

    @Nullable
    public abstract ItemStack getItem(UUID companyUniqueId, UUID shopUniqueId);

    @Nullable
    public abstract Location getLocation(UUID uniqueId);

    public abstract void setLocation(UUID uniqueId, Location location);

    //public abstract Location getLocation(UUID uniqueId);

    //public abstract UUID getCompanyUUID(UUID uniqueId);

    //public abstract UUID getCreator(UUID uniqueId);

    public abstract int getLocationPos(UUID uniqueId, String area, String type);

    public abstract void createArea(UUID uniqueId, CompanyArea companyArea, OfflinePlayer creator, Location firstBlock, Location secondBlock);

    public abstract Set<String> getAreas(UUID uniqueId);

    public abstract void setAccessibleCompanies(UUID uniqueId, String area, List<UUID> companyList);

    public abstract List<UUID> getAccessibleCompanies(UUID uniqueId, String area);

    public abstract void deleteArea(UUID uniqueId, CompanyArea companyArea);

    public abstract void setAreaLocation(UUID uniqueId, String area, Location location);

    public abstract Location getAreaLocation(UUID uniqueId, String area);

    public abstract HashMap<UUID, List<String>> getAreasRented(UUID uniqueId);
}
