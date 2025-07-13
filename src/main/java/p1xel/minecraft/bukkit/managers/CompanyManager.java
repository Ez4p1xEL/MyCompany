package p1xel.minecraft.bukkit.managers;

import p1xel.minecraft.bukkit.utils.storage.CompanyData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CompanyManager {

    private CompanyData data;
    private HashMap<Integer, UUID> cids = new HashMap<>();

    public CompanyManager(CompanyData data) {
        this.data = data;
    }

    public CompanyData getData() {
        return this.data;
    }

    public HashMap<Integer, UUID> getCIds() {
        return cids;
    }

    @Nullable
    public UUID getUUID(int cid) {
        return cids.get(cid);
    }

    public void init() {
        cids.clear();
        this.data.init();
    }

    public List<String> getCompaniesName() {
        return this.data.getCompaniesName();
    }

    public void createCompany(String companyName, UUID playerUniqueId) {
        this.data.createCompany(companyName, playerUniqueId);
    }

    public String getName(UUID uniqueId) {
        return this.data.getName(uniqueId);
    }

    // position here cannot be employer
    public List<UUID> getEmployeeList(UUID uniqueId, String position) {
        return this.data.getEmployeeList(uniqueId, position);
    }

    public void dismissEmployee(UUID companyUniqueId, UUID employeeUniqueId) {
        this.data.dismissEmployee(companyUniqueId, employeeUniqueId);
    }

    public void employPlayer(UUID companyUniqueId, UUID playerUniqueId, String position) {
        this.data.employPlayer(companyUniqueId, playerUniqueId, position);
    }

    public void disbandCompany(UUID uniqueId) {
        this.data.disbandCompany(uniqueId);
    }

    public UUID getEmployer(UUID uniqueId) {
        return this.data.getEmployer(uniqueId);
    }

    public String getFoundDate(UUID uniqueId) {
        return this.data.getFoundDate(uniqueId);
    }

    public int getMemberAmount(UUID uniqueId) {
        return this.data.getMemberAmount(uniqueId);
    }

    public int getId(UUID uniqueId) {
        return this.data.getId(uniqueId);
    }

    public UUID getFounder(UUID uniqueId) {
        return this.data.getFounder(uniqueId);
    }

    public String getFounderName(UUID uniqueId) {
        return this.data.getFounderName(uniqueId);
    }

    public UUID getUUIDFromId(int cid) {
        return this.data.getUUIDFromId(cid);
    }

    // This does not include employer
    public List<String> getPositions(UUID uniqueId) {
        return this.data.getPositions(uniqueId);
    }

    public double getCash(UUID uniqueId) {
        return this.data.getCash(uniqueId);
    }

    public List<UUID> getAllCompanies() {
        return this.data.getAllCompanies();
    }

    public void setCash(UUID uniqueId, double amount) {
        this.data.setCash(uniqueId, amount);
    }

    public double getDailyIncome(UUID uniqueId) {
        return this.data.getDailyIncome(uniqueId);
    }

    public double getTotalIncome(UUID uniqueId) {
        return this.data.getTotalIncome(uniqueId);
    }

    public void giveMoney(UUID uniqueId, double amount) {
        this.data.giveMoney(uniqueId, amount);
    }

    public void takeMoney(UUID uniqueId, double amount) {
        this.data.takeMoney(uniqueId, amount);
    }

}
