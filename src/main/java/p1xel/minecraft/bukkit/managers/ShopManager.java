package p1xel.minecraft.bukkit.managers;

import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.utils.storage.CompanyData;
import p1xel.minecraft.bukkit.utils.storage.Shop;

import javax.annotation.Nullable;
import javax.xml.stream.Location;
import java.util.UUID;


public class ShopManager {

    private CompanyData data;
    public ShopManager(CompanyData data) {
        this.data = data;
    }

    @Nullable
    public Shop getShop(UUID companyUniqueId, UUID shopUniqueId) {
        return new Shop(companyUniqueId, shopUniqueId);
    }

    public UUID createShop(UUID uniqueId, org.bukkit.Location location, double price, String creatorName) {return this.data.createShop(uniqueId, location, price, creatorName);}

    @Nullable
    public ItemStack getItem(UUID companyUniqueId, UUID shopUniqueId) {
        return (ItemStack) this.data.get(companyUniqueId, "shop", shopUniqueId + ".item");
    }

    public double getPrice(UUID companyUniqueId, UUID shopUniqueId) {
        return (double) this.data.get(companyUniqueId, "shop", shopUniqueId + ".price");
    }



}
