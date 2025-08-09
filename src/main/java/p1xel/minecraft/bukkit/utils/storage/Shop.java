package p1xel.minecraft.bukkit.utils.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.ShopManager;
import p1xel.minecraft.bukkit.utils.Logger;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

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

    public void setItem(ItemStack item) {
        shopManager.setItem(companyUniqueId, shopUniqueId, item);
    }

    public Location getLocation() {
        return shopManager.getLocation(companyUniqueId, shopUniqueId);
    }

    public Block getChestBlock() {
        return shopManager.getChestBlock(companyUniqueId, shopUniqueId);
    }

//    public int getStockSync() {
//        int amount = 0;
//        Block block = getChestBlock();
//        Chest chest = (Chest) block.getState();
//        Inventory inventory = chest.getBlockInventory();
//        ItemStack item = getItem();
//        for (ItemStack i : inventory.getContents()) {
//            if (i != null && i.isSimilar(item)) {
//                amount += i.getAmount();
//            }
//        }
//        Logger.debug(Level.INFO, "Amount: "+ amount);
//        return amount;
//
//    }

    public String getStatus(int amount) {
        //int amount = getStock();
        if (amount <= 0) {
            return "out-of-stock";
        }
        return "selling";
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
        return shopManager.getCreator(companyUniqueId, shopUniqueId);
    }


}
