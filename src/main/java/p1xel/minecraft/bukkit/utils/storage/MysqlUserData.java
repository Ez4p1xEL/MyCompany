package p1xel.minecraft.bukkit.utils.storage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class MysqlUserData extends UserData{
    @Override
    public void init() {

    }

    @Override
    public void createUser(UUID uniqueId) {

    }

    @Override
    public Object get(UUID uniqueId, String path) {
        return null;
    }

    @Override
    public void set(UUID uniqueId, String path, Object value) {

    }

    @Override
    public String getName(UUID uniqueId) {
        return "";
    }

    @Override
    public boolean isUserExist(UUID uniqueId) {
        return false;
    }

    @Override
    public void setCompany(UUID playerUniqueId, UUID companyUniqueId) {

    }

    @Nullable
    @Override
    public UUID getCompanyUUID(UUID uniqueId) {
        return null;
    }

    @Override
    public void setPosition(UUID uniqueId, String position) {

    }

    @Override
    public String getPosition(UUID uniqueId) {
        return "";
    }

    @Override
    public List<String> getOrdersInProgress(UUID uniqueId) {
        return List.of();
    }

    @Override
    public void createOrderForPlayer(UUID uniqueId, String order) {

    }

    @Override
    public void updateOrderValue(UUID uniqueId, String order, String quest, int value) {

    }

    @Override
    public void removeOrder(UUID uniqueId, String order) {

    }

    @Override
    public void removeAllOrders(UUID uniqueId) {

    }

    @Override
    public void randomizeDailyOrder(UUID uniqueId) {

    }

    @Override
    public List<String> getDailyOrders(UUID uniqueId) {
        return List.of();
    }
}
