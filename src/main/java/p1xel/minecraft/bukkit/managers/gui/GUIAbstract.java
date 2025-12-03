package p1xel.minecraft.bukkit.managers.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.*;

public abstract class GUIAbstract {

    CacheManager cache = MyCompany.getCacheManager();
    protected final UserManager userManager = cache.getUserManager();
    protected final CompanyManager companyManager = cache.getCompanyManager();
    protected final ShopManager shopManager = cache.getShopManager();
    protected final BuildingManager buildingManager = cache.getBuildingManager();
    protected final AreaManager areaManager = cache.getAreaManager();
    protected NamespacedKey menu_id_key = new NamespacedKey("mycompany", "menu_id");
    public abstract void init();
    public abstract Inventory getInventory();
    public abstract boolean check(String name);

}
