package p1xel.minecraft.bukkit.object;

import org.jetbrains.annotations.Nullable;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.CompanyManager;
import p1xel.minecraft.bukkit.manager.ShopManager;
import p1xel.minecraft.bukkit.util.permission.Permission;

import java.util.*;

public class Company {

    private final UUID companyUniqueId;
    private final CompanyManager companyManager;
    private final ShopManager shopManager;

    private String name;
    private final int cid;
    private UUID employerUniqueId;
    private final HashMap<String, List<UUID>> employeeList = new HashMap<>();
    private List<String> positions;
    private final HashMap<UUID, Shop> shops = new HashMap<>();
    private final HashMap<String, HashSet<Permission>> permissions = new HashMap<>();
    private final HashMap<String, String> positionLabels = new HashMap<>();
    private final HashMap<String, Double> positionSalaries = new HashMap<>();
    private double cash; // TODO: companyManager修改现金时应该更新这个值
    private double totalIncome; // TODO: 同上
    private double dailyIncome; // TODO: 同上

    public Company(UUID companyUniqueId) {
        this.companyUniqueId = companyUniqueId;
        this.companyManager = MyCompany.getCacheManager().getCompanyManager();
        this.shopManager = MyCompany.getCacheManager().getShopManager();

        this.name = companyManager.getName(companyUniqueId);
        this.cid = companyManager.getId(companyUniqueId);
        this.employerUniqueId = companyManager.getEmployer(companyUniqueId);
        this.positions = companyManager.getPositions(companyUniqueId);
        for (String position : positions) {
            // Initialize employee list for each position
            this.employeeList.put(position, companyManager.getEmployeeList(companyUniqueId, position));

            // Initialize permissions for each position
            HashSet<Permission> set = new HashSet<>(companyManager.getPositionPermission(companyUniqueId, position));
            this.permissions.put(position, set);

            // Initialize position labels for each position
            String label = companyManager.getPositionLabel(companyUniqueId, position);
            this.positionLabels.put(position, label);

            // Initialize position salaries for each position
            double salary = companyManager.getSalary(companyUniqueId, position);
            this.positionSalaries.put(position, salary);

        }

        this.cash = companyManager.getCash(companyUniqueId);
        this.totalIncome = companyManager.getTotalIncome(companyUniqueId);
        this.dailyIncome = companyManager.getDailyIncome(companyUniqueId);

    }

    public UUID getUUID() {
        return companyUniqueId;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return cid;
    }

    public UUID getEmployer() {
        return employerUniqueId;
    }

    public List<UUID> getEmployeeList(String position) {
        return employeeList.get(position);
    }

    public List<String> getPositions() {
        return positions;
    }

    @Nullable
    public Shop getShop(UUID shopUniqueId) {
        return shops.get(shopUniqueId);
    }

    public HashSet<Permission> getPositionPermission(String position) { return permissions.get(position); }

    public String getPositionLabel(String position) { return positionLabels.get(position); }

    public double getPositionSalary(String position) { return positionSalaries.get(position); }

    public double getCash() { return cash;   }

    public double getTotalIncome() { return totalIncome; }

    public double getDailyIncome() { return dailyIncome; }

    /*
    如果該商店存在, 但未被緩存, 則嘗試加載該商店擁有的所有箱子商店並存至HashMap
     */
    @Nullable
    public Shop getShop(UUID shopUniqueId, boolean loadIfNotExist) {
        Shop shop = shops.get(shopUniqueId);
        if (shop == null && loadIfNotExist) {
            saveShops();
            shop = shops.get(shopUniqueId);
        }

        if (shop != null && !loadIfNotExist) {
            shop.updateLastAccess(); // 會順便更新商店的最後訪問時間
        }
        return shop;
    }

    public int getPersonnelAmount() {
        int amount = 1;
        for (String position : positions) {
            if (position.equalsIgnoreCase("employer")) {
                continue;
            }

            List<UUID> employeeList = getEmployeeList(position);
            if (employeeList != null && !employeeList.isEmpty()) {
                amount = amount + employeeList.size();
            }
        }
        return amount;
    }

    public void saveShops() {
        for (Shop shop : shopManager.getShops(companyUniqueId)) {
            shops.putIfAbsent(shop.getShopUUID(), shop);
        }
    }

    public void addShop(UUID shopUniqueId, Shop shop) {
        shops.put(shopUniqueId, shop);
    }

    public void removeShop(UUID shopUniqueId) {
        shops.remove(shopUniqueId);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmployer(UUID employerUniqueId) {
        this.employerUniqueId = employerUniqueId;
    }

    public void setEmployeeList(String position, List<UUID> employees) {
        this.employeeList.put(position, employees);
    }

    public void setPositions(List<String> positions) {
        this.positions = positions;
    }

    public void setPositionLabel(String position, String label) { this.positionLabels.put(position, label); }

    public void setCash(double cash) { this.cash = cash; }

    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

    public void setDailyIncome(double dailyIncome) { this.dailyIncome = dailyIncome; }

    public void addEmployee(UUID employeeUniqueId, String position) { employeeList.get(position).add(employeeUniqueId); }

    public void removeEmployee(UUID employeeUniqueId) {
        for (List<UUID> employees : employeeList.values()) {
            employees.remove(employeeUniqueId);
        }
    }

    public void setPositionSalary(String position, double salary) { this.positionSalaries.put(position, salary); }

    public void addPosition(String position, String label, double salary) {
        if (!positions.contains(position)) {
            positions.add(position);
            employeeList.put(position, new ArrayList<>());
            permissions.put(position, new HashSet<>());
            positionLabels.put(position, label);
            positionSalaries.put(position, salary);
        }
    }

    public void removePosition(String position) {
        List<UUID> list = employeeList.get(position);
        employeeList.get("employee").addAll(list);
        employeeList.remove(position);
        positions.remove(position);
        permissions.remove(position);
        positionLabels.remove(position);
        positionSalaries.remove(position);
    }

}
