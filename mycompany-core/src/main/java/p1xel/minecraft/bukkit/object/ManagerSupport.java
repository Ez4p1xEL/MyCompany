package p1xel.minecraft.bukkit.object;

import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.*;
import p1xel.minecraft.bukkit.object.focus.FocusManager;
import p1xel.minecraft.bukkit.object.price.InternalStore;

public class ManagerSupport {

    public final CacheManager cacheManager = MyCompany.getCacheManager();
    public CompanyManager companyManager = cacheManager.getCompanyManager();
    public UserManager userManager = cacheManager.getUserManager();
    public ShopManager shopManager = cacheManager.getShopManager();
    public AreaManager areaManager = cacheManager.getAreaManager();
    public BuildingManager buildingManager = cacheManager.getBuildingManager();
    public InternalStore internalStore = cacheManager.getInternalStore();
    public FocusManager focusManager = cacheManager.getFocusManager();

}
