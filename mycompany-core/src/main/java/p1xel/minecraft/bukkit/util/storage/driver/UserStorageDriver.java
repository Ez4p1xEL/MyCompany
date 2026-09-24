package p1xel.minecraft.bukkit.util.storage.driver;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserStorageDriver {

    void init();
    void close();

    Object get(UUID uniqueId, String key);
    void set(UUID uniqueId, String key, Object value);
    Set<String> getKeys(UUID uniqueId, String key);
    void save(UUID uniqueId);

    double getDouble(UUID uniqueId, String key);
    String getString(UUID uniqueId, String key);
    boolean getBoolean(UUID uniqueId, String key);
    int getInt(UUID uniqueId, String key);
    float getFloat(UUID uniqueId, String key);
    List<String> getStringList(UUID uniqueId, String key);
    long getLong(UUID uniqueId, String key);

    void createUserEntry(UUID uniqueId);
    void deleteUserEntry(UUID uniqueId);
    boolean isUserExist(UUID uniqueId);

}
