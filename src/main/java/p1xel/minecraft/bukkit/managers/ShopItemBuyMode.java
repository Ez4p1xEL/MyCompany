package p1xel.minecraft.bukkit.managers;

import org.bukkit.scheduler.BukkitTask;
import p1xel.minecraft.bukkit.utils.storage.Shop;

import java.util.UUID;

public class ShopItemBuyMode {

    private final UUID playerUniqueId;
    private final Shop shop; // target player
    private final long timestamp; // 发送时间
    private BukkitTask expirationTask;

    public ShopItemBuyMode(UUID playerUniqueId, Shop shop, long timestamp, BukkitTask expirationTask) {
        this.playerUniqueId = playerUniqueId;
        this.shop = shop;
        this.timestamp = timestamp;
        this.expirationTask = expirationTask;
    }

    public BukkitTask getExpirationTask() {
        return this.expirationTask;
    }

    public Shop getShop() {
        return this.shop;
    }

    public UUID getPlayerUniqueId() {
        return this.playerUniqueId;
    }

    public long getTimeStamp() {
        return this.timestamp;
    }

    public void setExpirationTask(BukkitTask expirationTask) {
        this.expirationTask = expirationTask;
    }

}
