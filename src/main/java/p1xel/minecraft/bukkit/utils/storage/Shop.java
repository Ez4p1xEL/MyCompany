package p1xel.minecraft.bukkit.utils.storage;

import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.ShopManager;

import java.util.UUID;

public class Shop{

    private final UUID companyUniqueId;
    private final UUID shopUniqueId;
    private final ShopManager shopManager = MyCompany.getCacheManager().getShopManager();

    public Shop(UUID companyUniqueId, UUID shopUniqueId) {
        this.companyUniqueId = companyUniqueId;
        this.shopUniqueId = shopUniqueId;
    }

    public UUID getCompanyUUID() {
        return this.companyUniqueId;
    }

    public UUID getShopUUID() {
        return this.shopUniqueId;
    }

    public ItemStack getItem() {
        return shopManager.getItem(companyUniqueId, shopUniqueId);
    }

    public double getPrice() {
        return shopManager.getPrice(companyUniqueId, shopUniqueId);
    }


}
