package p1xel.minecraft.bukkit.manager.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.*;
import p1xel.minecraft.bukkit.object.price.InternalStore;

public abstract class GUIAbstract {

    protected final CacheManager cache = MyCompany.getCacheManager();
    protected final UserManager userManager = cache.getUserManager();
    protected final CompanyManager companyManager = cache.getCompanyManager();
    protected final ShopManager shopManager = cache.getShopManager();
    protected final BuildingManager buildingManager = cache.getBuildingManager();
    protected final AreaManager areaManager = cache.getAreaManager();
    protected final InternalStore internalStore = cache.getInternalStore();
    protected final NamespacedKey menu_id_key = new NamespacedKey("mycompany", "menu_id");
    protected final NamespacedKey menu_custom_id_key = new NamespacedKey("mycompany", "menu_custom_id_key");
    public abstract void init();
    public abstract Inventory getInventory();
    public abstract boolean check(String name);
    public abstract boolean check(String name, ClickType clickType);

}
