package p1xel.minecraft.bukkit.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.util.ItemSerializer;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.storage.AbstractCompanyData;
import p1xel.minecraft.bukkit.object.Shop;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;


public class ShopManager {

    private final AbstractCompanyData data;
    private final NamespacedKey shopKey = new NamespacedKey("mycompany", "is_company_shop");
    private final NamespacedKey companyKey = new NamespacedKey("mycompany", "company");
    private final NamespacedKey shopUUIDKey = new NamespacedKey("mycompany", "shop_uuid");
    public ShopManager(AbstractCompanyData data) {
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

    public UUID createShop(UUID uniqueId, Location location, double price, String creatorName) {
        UUID shopUniqueId = this.data.createShop(uniqueId, location, price, creatorName);
        Company company = MyCompany.getCacheManager().getCompany(uniqueId);
        company.addShop(shopUniqueId, getShop(uniqueId, shopUniqueId));
        return shopUniqueId;
    }

    @Nullable
    public ItemStack getItem(UUID companyUniqueId, UUID shopUniqueId) {
        //return (ItemStack) this.data.get(companyUniqueId, "shop", shopUniqueId + ".item");
        return this.data.getItem(companyUniqueId, shopUniqueId);
    }

    public double getPrice(UUID companyUniqueId, UUID shopUniqueId) {
        return (double) this.data.get(companyUniqueId, "shop", shopUniqueId + ".price");
    }

    public void setItem(UUID companyUniqueId, UUID shopUniqueId, ItemStack item) {
        this.data.set(companyUniqueId, "shop", shopUniqueId + ".item", ItemSerializer.toBase64(item.clone()));
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
        Company company = MyCompany.getCacheManager().getCompany(companyUniqueId);
        company.removeShop(shopUniqueId);
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

    public String getCreator(UUID companyUniqueId, UUID shopUniqueId) {
        return (String) this.data.get(companyUniqueId, "shop", shopUniqueId + ".creator");
    }

    // In Game
    public boolean isShop(Chest chest) {
        PersistentDataContainer container = chest.getPersistentDataContainer();
        return container.has(shopKey, PersistentDataType.BOOLEAN);
    }

    @Nullable
    public UUID getShopUUID(Chest chest) {
        PersistentDataContainer container = chest.getPersistentDataContainer();
        if (!container.has(shopUUIDKey, PersistentDataType.STRING)) {
            return null;
        }

        String string = container.get(shopUUIDKey, PersistentDataType.STRING);
        if (string == null) { return null; }

        return UUID.fromString(string);
    }

    @Nullable
    public UUID getCompanyUUID(Chest chest) {
        PersistentDataContainer container = chest.getPersistentDataContainer();
        if (!container.has(companyKey, PersistentDataType.STRING)) {
            return null;
        }

        String string = container.get(companyKey, PersistentDataType.STRING);
        if (string == null) { return null; }

        return UUID.fromString(string);
    }

    @Nullable
    public Shop getShop(Chest chest) {
        UUID companyUniqueId = getCompanyUUID(chest);
        UUID shopUniqueId = getShopUUID(chest);
        if (companyUniqueId == null || shopUniqueId == null) {
            return null;
        }

        Company company = MyCompany.getCacheManager().getCompany(companyUniqueId);
        return company.getShop(shopUniqueId, true);

    }

    public NamespacedKey getShopKey() { return this.shopKey; }
    public NamespacedKey getCompanyKey() { return this.companyKey; }
    public NamespacedKey getShopUUIDKey() { return this.shopUUIDKey; }

}
