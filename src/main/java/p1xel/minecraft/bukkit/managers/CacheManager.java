package p1xel.minecraft.bukkit.managers;

import p1xel.minecraft.bukkit.Company;

import java.util.HashMap;
import java.util.UUID;

public class CacheManager {

    public HashMap<UUID, Company> comp =  new HashMap<>();

//    public static HashMap<UUID, Company> getCompanies() {
//        return companies;
//    }

    private CompanyManager companies;
    private UserManager users;
    private ShopManager shops;
    private BuildingManager buildings;

    public CacheManager(CompanyManager companies, UserManager users) {
        this.companies = companies;
        this.users = users;
        this.shops = new ShopManager(companies.getData());
        this.buildings = new BuildingManager(companies.getData());
    }

    public CompanyManager getCompanyManager() {
        return companies;
    }
    
    public UserManager getUserManager() {
        return users;
    }

    public ShopManager getShopManager() { return shops;}

    public BuildingManager getBuildingManager() { return buildings;}

    public void init() {
        this.companies.init();
        this.users.init();
    }



}
