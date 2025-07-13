package p1xel.minecraft.bukkit.utils.storage;

import javax.annotation.Nullable;
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
}
