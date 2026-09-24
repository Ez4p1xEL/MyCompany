package p1xel.minecraft.bukkit.util.storage.driver;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CompanyStorageDriver {

    void init();
    void close();

    Object get(UUID uniqueId, String type, String key);
    void set(UUID uniqueId, String type, String key, Object value);
    Set<String> getKeys(UUID uniqueId, String type, String key);
    void save(UUID uniqueId, String type);

    double getDouble(UUID uniqueId, String type, String key);
    String getString(UUID uniqueId, String type, String key);
    boolean getBoolean(UUID uniqueId, String type, String key);
    int getInt(UUID uniqueId, String type, String key);
    float getFloat(UUID uniqueId, String type, String key);
    List<String> getStringList(UUID uniqueId, String type, String key);
    long getLong(UUID uniqueId, String type, String key);

    void createCompanyEntry(UUID uniqueId);
    void deleteCompanyEntry(UUID uniqueId);
    List<UUID> loadAllCompanyEntries();

}
