package p1xel.minecraft.bukkit.manager;

import org.jetbrains.annotations.NotNull;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.price.InternalStore;
import p1xel.minecraft.bukkit.object.price.PriceGroup;

import java.util.HashMap;
import java.util.UUID;

public class CacheManager {

    public HashMap<UUID, Company> comp =  new HashMap<>();

//    public static HashMap<UUID, Company> getCompanies() {
//        return companies;
//    }

    private final CompanyManager companies;
    private final UserManager users;
    private final ShopManager shops;
    private final BuildingManager buildings;
    private final AreaManager areas;
    private final InternalStore internalStore;
    private final HashMap<UUID, Company> companiesCache = new HashMap<>();

    public CacheManager(CompanyManager companies, UserManager users) {
        this.companies = companies;
        this.users = users;
        this.shops = new ShopManager(companies.getData());
        this.buildings = new BuildingManager(companies.getData());
        this.areas = new AreaManager(companies.getData());
        this.internalStore = new InternalStore();
    }

    public CompanyManager getCompanyManager() {
        return companies;
    }
    
    public UserManager getUserManager() {
        return users;
    }

    public ShopManager getShopManager() { return shops;}

    public BuildingManager getBuildingManager() { return buildings;}

    public AreaManager getAreaManager() { return areas; }

    public InternalStore getInternalStore() { return internalStore; }

    public void init() {
        this.companies.init();
        this.users.init();
        this.areas.init();

        /* init companies
        初始化公司
         */
        for (UUID uuid : companies.getAllCompanies()) {
            Company company = new Company(uuid);
            company.setEmployer(companies.getEmployer(uuid));
            company.setPositions(companies.getPositions(uuid));
            for (String position : companies.getPositions(uuid)) {
                company.setEmployeeList(position, companies.getEmployeeList(uuid, position));
            }

            if (companies.getPriceGroup(uuid) == null) {
                companies.setPriceGroup(uuid, PriceGroup.NORMAL);
            }
            companiesCache.put(uuid, company);
        }
    }

    @NotNull
    public Company getCompany(@NotNull UUID uuid) {
        return companiesCache.get(uuid);
    }

    public boolean isCompanyCached(@NotNull UUID uuid) {
        return companiesCache.containsKey(uuid);
    }

    public Company createCompanyCache(@NotNull UUID uuid) {
        Company company = new Company(uuid);
        companiesCache.put(uuid, company);
        return company;
    }

    public void removeCompanyCache(@NotNull UUID uuid) {
        companiesCache.remove(uuid);
    }



}
