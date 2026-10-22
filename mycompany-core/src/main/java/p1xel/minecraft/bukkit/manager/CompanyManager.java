package p1xel.minecraft.bukkit.manager;

import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.object.CompanyLog;
import p1xel.minecraft.bukkit.object.price.PriceGroup;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.storage.AbstractCompanyData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class CompanyManager {

    private final AbstractCompanyData data;
    private final HashMap<Integer, UUID> cids = new HashMap<>();
    private CacheManager cacheManager;

    public CompanyManager(AbstractCompanyData data) {
        this.data = data;
    }

    public AbstractCompanyData getData() {
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
        cacheManager = MyCompany.getCacheManager();
        this.data.init();
    }

    public List<String> getCompaniesName() {
        return this.data.getCompaniesName();
    }

    public UUID createCompany(String companyName, UUID playerUniqueId) {
        UUID uuid = this.data.createCompany(companyName, playerUniqueId);
        cacheManager.createCompanyCache(uuid);
        return uuid;
    }

    public String getName(UUID uniqueId) {
        return this.data.getName(uniqueId);
    }

    // position here cannot be employer
    public List<UUID> getEmployeeList(UUID uniqueId, String position) {
        return this.data.getEmployeeList(uniqueId, position);
    }

    public Company getCompany(UUID uniqueId) {
        return MyCompany.getCacheManager().getCompany(uniqueId);
    }

    public void dismissEmployee(UUID companyUniqueId, UUID employeeUniqueId) {
        this.data.dismissEmployee(companyUniqueId, employeeUniqueId);
        getCompany(companyUniqueId).removeEmployee(employeeUniqueId);
    }

    public void employPlayer(UUID companyUniqueId, UUID playerUniqueId, String position) {
        this.data.employPlayer(companyUniqueId, playerUniqueId, position);
        getCompany(companyUniqueId).addEmployee(playerUniqueId, position);
        MyCompany.getCacheManager().getUserManager().randomizeDailyOrder(playerUniqueId);
    }

    public void disbandCompany(UUID uniqueId) {
        MyCompany.getCacheManager().getAreaManager().clearCompanyCache(uniqueId);
        this.data.disbandCompany(uniqueId);
        cacheManager.removeCompanyCache(uniqueId);
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

    // This includes employer, employee and all custom positions.
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
        MyCompany.getCacheManager().getCompany(uniqueId).setCash(amount);
    }

    public double getDailyIncome(UUID uniqueId) {
        return this.data.getDailyIncome(uniqueId);
    }

    public double getTotalIncome(UUID uniqueId) {
        return this.data.getTotalIncome(uniqueId);
    }

    public void giveMoney(UUID uniqueId, double amount) {
        this.data.giveMoney(uniqueId, amount);
        getCompany(uniqueId).setCash(getCompany(uniqueId).getCash() + amount);
    }

    public void takeMoney(UUID uniqueId, double amount) {
        this.data.takeMoney(uniqueId, amount);
        getCompany(uniqueId).setCash(getCompany(uniqueId).getCash() - amount);
    }

    public double getSalary(UUID uniqueId, String position) {
        return this.data.getSalary(uniqueId, position);
    }

    public void setSalary(UUID uniqueId, String position, double amount) {
        this.data.setSalary(uniqueId, position, amount);
        getCompany(uniqueId).setPositionSalary(position, amount);
    }

    public void resetDailyIncome(UUID uniqueId) {
        this.data.resetDailyIncome(uniqueId);
        getCompany(uniqueId).setDailyIncome(0);
    }

    public String getEmployerLabel(UUID uniqueId) {
        return (String) this.data.get(uniqueId, "settings", "position.default.employer.label");
    }

    public String getEmployeeLabel(UUID uniqueId) {
        return (String) this.data.get(uniqueId, "settings", "position.default.employee.label");
    }

    // Do not enter employer or employee
    public String getPositionLabel(UUID uniqueId, String position) {
        if (position.equalsIgnoreCase("employer")) {
            return getEmployerLabel(uniqueId);
        }
        if (position.equalsIgnoreCase("employee")) {
            return getEmployeeLabel(uniqueId);
        }
        return (String) this.data.get(uniqueId, "settings", "position.custom." + position + ".label");
    }

    public void setEmployerLabel(UUID uniqueId, String label) {
        this.data.set(uniqueId, "settings", "position.default.employer.label", label);
        this.data.save(uniqueId, "settings");
        getCompany(uniqueId).setPositionLabel("employer", label);
    }

    public void setEmployeeLabel(UUID uniqueId, String label) {
        this.data.set(uniqueId, "settings", "position.default.employee.label", label);
        this.data.save(uniqueId, "settings");
        getCompany(uniqueId).setPositionLabel("employee", label);
    }

    public void setPositionLabel(UUID uniqueId, String position, String label) {
        this.data.set(uniqueId, "settings", "position.custom." + position + ".label", label);
        this.data.save(uniqueId, "settings");
        getCompany(uniqueId).setPositionLabel(position, label);
    }

    public void addPosition(UUID uniqueId, String position) {
        List<String> permissionList = Config.getStringList("company-settings.employee-default-permission");
        this.data.set(uniqueId, "info", "members." + position, Collections.emptyList());
        this.data.set(uniqueId, "settings", "salary." + position, 500.0);
        this.data.set(uniqueId, "settings", "position.custom." + position + ".label", position);
        this.data.set(uniqueId, "settings","position.custom." + position + ".permission", permissionList);
        this.data.save(uniqueId, "info");
        this.data.save(uniqueId, "settings");
        Company company = getCompany(uniqueId);
        company.addPosition(position, position, 500.0);
        for (String permissionString : permissionList) {
            Permission permission = Permission.matchPermission(permissionString);
            if (permission != null) {
                company.getPositionPermission(position).add(permission);
            }
        }
    }

    // Member of the position will be moved to employee's list
    public void removePosition(UUID uniqueId, String position) {
        this.data.set(uniqueId, "settings", "position.custom." + position, null);
        this.data.set(uniqueId, "settings", "salary." + position, null);
        this.data.save(uniqueId, "settings");
        List<UUID> origin = getEmployeeList(uniqueId, "employee");
        List<UUID> add = getEmployeeList(uniqueId, position);
        origin.addAll(add);
        List<String> stringList = origin.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        this.data.set(uniqueId, "info", "members.employee", stringList);
        this.data.set(uniqueId, "info", "members." + position, null);
        this.data.save(uniqueId, "info");
        Company company = getCompany(uniqueId);
        company.removePosition(position);
    }

    public List<Permission> getPositionPermission(UUID uniqueId, String position) {
        List<String> stringList;
        switch (position) {
            case "employer":
            case "employee":
                List<String> list1 = (List<String>) this.data.get(uniqueId, "settings", "position.default." + position + ".permission");
                stringList = list1;
                break;
            default:
                List<String> list2 = (List<String>) this.data.get(uniqueId, "settings", "position.custom." + position + ".permission");
                stringList = list2;
                break;
        }
        List<Permission> list = new ArrayList<>();
        for (String perm : stringList) {
            list.add(Permission.matchPermission(perm));
        }
        return list;
    }

    public void addPositionPermission(UUID uniqueId, String position, Permission permission) {
        List<String> list = new ArrayList<>();
        switch (position) {
            case "employer":
            case "employee":
                List<String> list1 = (List<String>) this.data.get(uniqueId, "settings", "position.default." + position + ".permission");
                list = list1;
                list.add(permission.getName());
                this.data.set(uniqueId, "settings", "position.default." + position + ".permission", list);
                break;
            default:
                List<String> list2 = (List<String>) this.data.get(uniqueId, "settings", "position.custom." + position + ".permission");
                list = list2;
                list.add(permission.getName());
                this.data.set(uniqueId, "settings", "position.custom." + position + ".permission", list);
                break;
        }
        this.data.save(uniqueId, "settings");
        getCompany(uniqueId).getPositionPermission(position).add(permission);
    }

    public void removePositionPermission(UUID uniqueId, String position, Permission permission) {
        List<String> list = new ArrayList<>();
        switch (position) {
            case "employer":
            case "employee":
                List<String> list1 = (List<String>) this.data.get(uniqueId, "settings", "position.default." + position + ".permission");
                list = list1;
                list.remove(permission.getName());
                this.data.set(uniqueId, "settings", "position.default." + position + ".permission", list);
                break;
            default:
                List<String> list2 = (List<String>) this.data.get(uniqueId, "settings", "position.custom." + position + ".permission");
                list = list2;
                list.remove(permission.getName());
                this.data.set(uniqueId, "settings", "position.custom." + position + ".permission", list);
                break;
        }
        this.data.save(uniqueId, "settings");
        getCompany(uniqueId).getPositionPermission(position).remove(permission);
    }

    public void setEmployeePosition(UUID companyUniqueId, UUID employeeUniqueId, String position) {

        UserManager userManager = MyCompany.getCacheManager().getUserManager();
        String current = userManager.getPosition(employeeUniqueId);
        List<UUID> origin = getEmployeeList(companyUniqueId, current);
        origin.remove(employeeUniqueId);
        List<String> origin_string = origin.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        this.data.set(companyUniqueId, "info", "members." + current, origin_string);
        List<UUID> latest = getEmployeeList(companyUniqueId, position);
        latest.add(employeeUniqueId);
        List<String> latest_string = latest.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        this.data.set(companyUniqueId, "info", "members." + position, latest_string);
        userManager.setPosition(employeeUniqueId, position);

        this.data.save(companyUniqueId, "info");
        Company company = getCompany(companyUniqueId);
        company.setEmployeeList(current, origin);
        company.setEmployeeList(position, latest);

    }

    public String getPriceGroup(UUID uniqueId) {
        return (String) this.data.get(uniqueId, "info", "price-group");
    }

    public void setPriceGroup(UUID uniqueId, String priceGroupId) {
        this.data.set(uniqueId, "info", "price-group", priceGroupId);
        this.data.save(uniqueId, "info");
        getCompany(uniqueId).setPriceGroupId(priceGroupId);
    }

    public CompanyLog createCompanyLog(UUID uniqueId) {
        return this.data.createCompanyLog(uniqueId);
    }

    public double getLog(UUID uniqueId, String logType) {
        return getCompany(uniqueId).getCompanyLog().getLog(logType);
    }

    public void addLogValue(UUID uniqueId, String logType, double value) {
        this.data.addLogValue(uniqueId, logType, value);
        getCompany(uniqueId).getCompanyLog().addLogValue(logType, value);
    }

}
