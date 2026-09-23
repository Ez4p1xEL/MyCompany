package p1xel.minecraft.bukkit.object;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.ShopManager;

import javax.annotation.Nullable;
import java.util.UUID;

public class Shop{

    private final UUID companyUniqueId;
    private final UUID shopUniqueId;
    private @Nullable ItemStack itemStack;
    private final double price;
    private final Location location;
    private final Block block;
    private final String creator;
    private long lastAccess = 0;
    private final ShopManager shopManager = MyCompany.getCacheManager().getShopManager();

    public Shop(UUID companyUniqueId, UUID shopUniqueId) {
        this.companyUniqueId = companyUniqueId;
        this.shopUniqueId = shopUniqueId;
        try {
            itemStack = shopManager.getItem(companyUniqueId, shopUniqueId);
        } catch (NullPointerException exception) {
            itemStack = null;
        }
        price = shopManager.getPrice(companyUniqueId, shopUniqueId);
        location = shopManager.getLocation(companyUniqueId, shopUniqueId);
        block = shopManager.getChestBlock(companyUniqueId, shopUniqueId);
        creator = shopManager.getCreator(companyUniqueId, shopUniqueId);
    }

    public UUID getCompanyUUID() {
        return this.companyUniqueId;
    }

    public UUID getShopUUID() {
        return this.shopUniqueId;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    public double getPrice() {
        return price;
    }

    public void setItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }
        this.itemStack = item;
        shopManager.setItem(companyUniqueId, shopUniqueId, item);
    }

    public Location getLocation() {
        return this.location;
    }

    public Block getChestBlock() {
        return this.block;
    }

    public String getStatus(int amount) {
        //int amount = getStock();
        if (amount <= 0) {
            return "out-of-stock";
        }
        return "selling";
    }

    public long getLastAccess() {
        return lastAccess;
    }

//    public void takeStock(int amount) {
//        Bukkit.getScheduler().runTask(MyCompany.getInstance(), ()-> {
//            Block block = getChestBlock();
//            Chest chest = (Chest) block.getState();
//            Inventory inventory = chest.getBlockInventory();
//            ItemStack item = getItem();
//            int remaining = amount;
//            int count = 0;
//            for (ItemStack i : inventory.getContents()) {
//                if (remaining <= 0) {
//                    break;
//                }
//                if (i != null && i.isSimilar(item)) {
//                    int has = i.getAmount();
//                    if (has <= remaining) {
//                        remaining -= has;
//                        i.setAmount(0);
//                        inventory.setItem(count, i);
//                    }
//
//                    if (remaining < has) {
//                        remaining = 0;
//                        i.setAmount(has - remaining);
//                        inventory.setItem(count, i);
//                    }
//                }
//                count++;
//            }
//            chest.update();
//        });
//    }

    public String getCreator() {
        return this.creator;
    }

    public void updateLastAccess() {
        this.lastAccess = System.currentTimeMillis();
    }


}
