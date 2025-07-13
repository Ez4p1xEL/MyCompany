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

    public CacheManager(CompanyManager companies, UserManager users) {
        this.companies = companies;
        this.users = users;
    }

    public CompanyManager getCompanyManager() {
        return companies;
    }
    
    public UserManager getUserManager() {
        return users;
    }

    public void init() {
        this.companies.init();
        this.users.init();
    }



}
