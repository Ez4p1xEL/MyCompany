package p1xel.minecraft.bukkit.managers;

import p1xel.minecraft.bukkit.utils.storage.UserData;

import javax.annotation.Nullable;
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


}
