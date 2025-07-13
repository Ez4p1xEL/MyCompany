package p1xel.minecraft.bukkit.utils.storage;

import javax.annotation.Nullable;
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



}
