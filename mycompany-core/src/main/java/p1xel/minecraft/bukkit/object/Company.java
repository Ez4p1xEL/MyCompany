package p1xel.minecraft.bukkit.object;

import org.jetbrains.annotations.Nullable;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.CompanyManager;
import p1xel.minecraft.bukkit.manager.ShopManager;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Company {

    private final UUID companyUniqueId;
    private final CompanyManager companyManager;
    private final ShopManager shopManager;

    private String name;
    private UUID employerUniqueId;
    private final HashMap<String, List<UUID>> employeeList = new HashMap<>();
    private List<String> positions;
    private final HashMap<UUID, Shop> shops = new HashMap<>();

    public Company(UUID companyUniqueId) {
        this.companyUniqueId = companyUniqueId;
        this.companyManager = MyCompany.getCacheManager().getCompanyManager();
        this.shopManager = MyCompany.getCacheManager().getShopManager();

        this.name = companyManager.getName(companyUniqueId);
        this.employerUniqueId = companyManager.getEmployer(companyUniqueId);
        this.positions = companyManager.getPositions(companyUniqueId);
        for (String position : positions) {
            this.employeeList.put(position, companyManager.getEmployeeList(companyUniqueId, position));
        }

    }

    public UUID getUUID() {
        return companyUniqueId;
    }

    public String getName() {
        return name;
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

}
