package p1xel.minecraft.bukkit.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.utils.Logger;
import p1xel.minecraft.bukkit.utils.storage.CompanyData;
import p1xel.minecraft.bukkit.utils.storage.Shop;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;


public class ShopManager {

    private CompanyData data;
    private final NamespacedKey shopKey = new NamespacedKey("mycompany", "is_company_shop");
    private final NamespacedKey companyKey = new NamespacedKey("mycompany", "company");
    private final NamespacedKey shopUUIDKey = new NamespacedKey("mycompany", "shop_uuid");
    public ShopManager(CompanyData data) {
        this.data = data;
    }

    @Nullable
    public Shop getShop(UUID companyUniqueId, UUID shopUniqueId) {
        return new Shop(companyUniqueId, shopUniqueId);
    }

    @Nullable
    public Shop getShop(Location location) {
        Block chestBlock = location.getBlock();
        if (!(chestBlock.getState() instanceof Chest)) {
            return null;
        }
        Chest chest = (Chest) chestBlock.getState();
        PersistentDataContainer container = chest.getPersistentDataContainer();
        if (!container.has(shopKey, PersistentDataType.BOOLEAN)) {
            return null;
        }

        UUID companyUniqueId = UUID.fromString(container.get(companyKey, PersistentDataType.STRING));
        UUID shopUniqueId = UUID.fromString(container.get(shopUUIDKey, PersistentDataType.STRING));
        return new Shop(companyUniqueId, shopUniqueId);
    }

    public UUID createShop(UUID uniqueId, org.bukkit.Location location, double price, String creatorName) {return this.data.createShop(uniqueId, location, price, creatorName);}

    @Nullable
    public ItemStack getItem(UUID companyUniqueId, UUID shopUniqueId) {
        //return (ItemStack) this.data.get(companyUniqueId, "shop", shopUniqueId + ".item");
        return this.data.getItem(companyUniqueId, shopUniqueId);
    }

    public double getPrice(UUID companyUniqueId, UUID shopUniqueId) {
        return (double) this.data.get(companyUniqueId, "shop", shopUniqueId + ".price");
    }

    public void setItem(UUID companyUniqueId, UUID shopUniqueId, ItemStack item) {
        this.data.set(companyUniqueId, "shop", shopUniqueId + ".item", item.clone());
        Logger.debug(Level.INFO, ((ItemStack) this.data.get(companyUniqueId, "shop", shopUniqueId + ".item")).toString());
    }

    public Location getLocation(UUID companyUniqueId, UUID shopUniqueId) {
        return getChestBlock(companyUniqueId, shopUniqueId).getLocation();
    }

    public Block getChestBlock(UUID companyUniqueId, UUID shopUniqueId) {
        String world = (String) this.data.get(companyUniqueId, "shop", shopUniqueId + ".location.world");
        int x = (int) this.data.get(companyUniqueId, "shop", shopUniqueId + ".location.x");
        int y = (int) this.data.get(companyUniqueId, "shop", shopUniqueId + ".location.y");
        int z = (int) this.data.get(companyUniqueId, "shop", shopUniqueId + ".location.z");
        return Bukkit.getWorld(world).getBlockAt(x,y,z);
    }

    public void deleteShop(UUID companyUniqueId, UUID shopUniqueId) {
        this.data.set(companyUniqueId, "shop", shopUniqueId.toString(), null);
    }

    public void deleteShop(Shop shop) {
        this.data.set(shop.getCompanyUUID(), "shop", shop.getShopUUID().toString(), null);
    }

    public List<Shop> getShops(UUID companyUniqueId) {
        return this.data.getShops(companyUniqueId);
    }

    public List<UUID> getShopsUUID(UUID companyUniqueId) {
        return this.data.getShopsUUID(companyUniqueId);
    }



}
