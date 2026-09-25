package p1xel.minecraft.bukkit.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.object.EmployeeOrder;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.storage.EmployeeOrders;
import p1xel.minecraft.bukkit.util.storage.AbstractUserData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserManager {

    private final AbstractUserData data;
    private final HashMap<UUID, User> userCache = new HashMap<>();
    private static final long EXPIRE_TIME_SECONDS = 10L * 60L; // 10 分鐘

    public UserManager(AbstractUserData data) {
        this.data = data;
        cleanCache();
    }

    public void init() {
        this.data.init();
    }

    public AbstractUserData getData() {
        return data;
    }

    public User getUser(UUID uniqueId) {
        // 從緩存中找 User
        if (this.userCache.containsKey(uniqueId)) {
            return this.userCache.get(uniqueId);
        }

        // 如果緩存裏沒有User，則從資料庫中創建一個新的User對象
        User user = this.data.buildUser(uniqueId);

        addUserToCache(uniqueId, user);
        return user;
    }

    public User getUser(UUID uniqueId, boolean includeOnlineData) {
        User user = getUser(uniqueId);
        if (includeOnlineData) {
            for (Map.Entry<String, EmployeeOrder> entry : EmployeeOrders.getPlayerOrders(uniqueId).entrySet()) {
                user.createOrder(entry.getKey(), entry.getValue());
            }
        }
        return user;
    }

    public void addUserToCache(UUID uniqueId, User user) {
        this.userCache.put(uniqueId, user);
    }

    public void removeUserFromCache(UUID uniqueId) {
        this.userCache.remove(uniqueId);
    }

    private void cleanCache() {
        new BukkitRunnable() {
            @Override
            public void run() {

                // 安全遍歷 Map
                userCache.entrySet().removeIf(entry -> {
                    UUID uuid = entry.getKey();

                    // 條件 1：玩家當前「在線」，絕對不清除
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        return false;
                    }

                    // 離線玩家，如果超時未被存取，保存並從 Map 移除

                    return true;
                });
            }
        }.runTaskTimerAsynchronously(MyCompany.getInstance(), EXPIRE_TIME_SECONDS * 20L, EXPIRE_TIME_SECONDS * 20L);
    }

    public void createUser(UUID uniqueId) {
        this.data.createUser(uniqueId);
    }

    public void getName(UUID uniqueId) {
        this.data.getName(uniqueId);
    }

    public boolean isUserExist(UUID uniqueId) {
        return this.data.isUserExist(uniqueId);
    }

    public void setCompany(UUID playerUniqueId, UUID companyUniqueId) {
        this.data.setCompany(playerUniqueId,companyUniqueId);
    }

    @Nullable
    public UUID getCompanyUUID(UUID uniqueId) {
        return this.data.getCompanyUUID(uniqueId);
    }

    public void setPosition(UUID uniqueId, String position) {
        this.data.setPosition(uniqueId,position);
    }

    public String getPosition(UUID uniqueId) {
        return this.data.getPosition(uniqueId);
    }

    public boolean hasPermission(UUID uniqueId, Permission permission) {
        if (permission ==null) {
            return false;
        }
        List<Permission> list = MyCompany.getCacheManager().getCompanyManager().getPositionPermission(getCompanyUUID(uniqueId), getPosition(uniqueId));
        return list.contains(Permission.ALL) || list.contains(permission);
    }

    public List<String> getOrdersInProgress(UUID uniqueId) {
        return this.data.getOrdersInProgress(uniqueId);
    }

    public void createOrderForPlayer(UUID uniqueId, String order) {
        this.data.createOrderForPlayer(uniqueId, order);
    }

    public void updateOrderValue(UUID uniqueId, String order, String quest, int value) {
        this.data.updateOrderValue(uniqueId, order, quest, value);
    }

    public void removeOrder(UUID uniqueId, String order) {
        this.data.removeOrder(uniqueId, order);
    }

    public void removeAllOrders(UUID uniqueId) {
        this.data.removeAllOrders(uniqueId);
    }

    public void randomizeDailyOrder(UUID uniqueId) {
        this.data.randomizeDailyOrder(uniqueId);
        EmployeeOrders.saveCacheToLocal();
    }

    public List<String> getDailyOrders(UUID uniqueId) {
        return this.data.getDailyOrders(uniqueId);
    }

}
