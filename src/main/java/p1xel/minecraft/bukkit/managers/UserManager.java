package p1xel.minecraft.bukkit.managers;

import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.utils.permissions.Permission;
import p1xel.minecraft.bukkit.utils.storage.EmployeeOrders;
import p1xel.minecraft.bukkit.utils.storage.UserData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class UserManager {

    private UserData data;

    public UserManager(UserData data) {
        this.data = data;
    }

    public void init() {
        this.data.init();
    }

    public UserData getData() {
        return data;
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
