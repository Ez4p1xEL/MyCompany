package p1xel.minecraft.bukkit.utils.storage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public abstract class UserData {

    // Initialize the database
    public abstract void init();

    public abstract void createUser(UUID uniqueId);

    public abstract Object get(UUID uniqueId, String path);

    public abstract void set(UUID uniqueId, String path, Object value);

    public abstract String getName(UUID uniqueId);

    public abstract boolean isUserExist(UUID uniqueId);

    public abstract void setCompany(UUID playerUniqueId, UUID companyUniqueId);

    @Nullable
    public abstract UUID getCompanyUUID(UUID uniqueId);

    public abstract void setPosition(UUID uniqueId, String position);

    public abstract String getPosition(UUID uniqueId);

    public abstract List<String> getOrdersInProgress(UUID uniqueId);

    public abstract void createOrderForPlayer(UUID uniqueId, String order);

    public abstract void updateOrderValue(UUID uniqueId, String order, String quest, int value);

    public abstract void removeOrder(UUID uniqueId, String order);

    public abstract void removeAllOrders(UUID uniqueId);

    public abstract void randomizeDailyOrder(UUID uniqueId);

    public abstract List<String> getDailyOrders(UUID uniqueId);



}
