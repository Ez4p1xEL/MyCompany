package p1xel.minecraft.bukkit.util.storage.driver;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.util.Logger;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class UserYamlDriver implements UserStorageDriver {

    private final JavaPlugin plugin;

    private final HashMap<UUID, File> files = new HashMap<>();
    private final HashMap<UUID, FileConfiguration> yamls = new HashMap<>();
    private static final long EXPIRE_TIME_SECONDS = 60L*10; // 10 分鐘

    public UserYamlDriver(JavaPlugin plugin) {
        this.plugin = plugin;
        cleanCache();
    }

    private void cleanCache() {
        new BukkitRunnable() {
            @Override
            public void run() {

                // 安全遍歷 Map
                files.entrySet().removeIf(entry -> {
                    UUID uuid = entry.getKey();

                    // 條件 1：玩家當前「在線」，絕對不清除
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        return false;
                    }

                    // 離線玩家，如果超時未被存取，保存並從 Map 移除

                    return true;
                });

                yamls.entrySet().removeIf(entry -> {
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

    @Override
    public void init() {
        File users = new File(plugin.getDataFolder(), "/users");
        if (!users.exists()) {
            users.mkdirs();
            Logger.debug(Level.INFO, "users folder has been created for the first time.");
        }
    }

    @Override
    public void close() {

    }

    public void saveToCache(UUID uniqueId) {
        if (files.containsKey(uniqueId)) {
            return;
        }

        File folder = new File(plugin.getDataFolder(), "/users");
        File file = new File(folder, uniqueId.toString() + ".yml");
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        this.files.put(uniqueId, file);
        this.yamls.put(uniqueId, yaml);
    }

    @Override
    public Object get(UUID uniqueId, String key) {
        saveToCache(uniqueId);
        FileConfiguration yaml = this.yamls.get(uniqueId);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.get(path);
    }

    @Override
    public void set(UUID uniqueId, String key, Object value) {
        saveToCache(uniqueId);
        FileConfiguration yaml = this.yamls.get(uniqueId);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        yaml.set(path, value);
    }

    @Override
    public Set<String> getKeys(UUID uniqueId, String key) {
        saveToCache(uniqueId);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        ConfigurationSection section = this.yamls.get(uniqueId).getConfigurationSection(path);
        if (section == null) { return Collections.emptySet(); }
        return section.getKeys(false);
    }

    @Override
    public void save(UUID uniqueId) {
        saveToCache(uniqueId);
        File file = this.files.get(uniqueId);
        FileConfiguration yaml = this.yamls.get(uniqueId);
        try {
            yaml.save(file);
        } catch (Exception e) {
            Logger.debug(Level.SEVERE, "Failed to save file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    @Override
    public double getDouble(UUID uniqueId, String key) {
        saveToCache(uniqueId);
        FileConfiguration yaml = this.yamls.get(uniqueId);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getDouble(path);
    }

    @Override
    public String getString(UUID uniqueId, String key) {
        saveToCache(uniqueId);
        FileConfiguration yaml = this.yamls.get(uniqueId);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getString(path);
    }

    @Override
    public boolean getBoolean(UUID uniqueId, String key) {
        saveToCache(uniqueId);
        FileConfiguration yaml = this.yamls.get(uniqueId);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getBoolean(path);
    }

    @Override
    public int getInt(UUID uniqueId, String key) {
        saveToCache(uniqueId);
        FileConfiguration yaml = this.yamls.get(uniqueId);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getInt(path);
    }

    @Override
    public float getFloat(UUID uniqueId, String key) {
        saveToCache(uniqueId);
        FileConfiguration yaml = this.yamls.get(uniqueId);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return (float) yaml.getDouble(path);
    }

    @Override
    public List<String> getStringList(UUID uniqueId, String key) {
        saveToCache(uniqueId);
        FileConfiguration yaml = this.yamls.get(uniqueId);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getStringList(path);
    }

    @Override
    public long getLong(UUID uniqueId, String key) {
        saveToCache(uniqueId);
        FileConfiguration yaml = this.yamls.get(uniqueId);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getLong(path);
    }

    @Override
    public void createUserEntry(UUID uniqueId) {
        File folder = new File(plugin.getDataFolder(), "/users");
        if (!folder.exists()) {
            folder.mkdirs();
            Logger.debug(Level.INFO, "Created new user folder: " + folder.getAbsolutePath());
        }

        File file = new File(folder, uniqueId.toString() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                this.files.put(uniqueId, file);
                this.yamls.put(uniqueId, YamlConfiguration.loadConfiguration(file));
                Logger.debug(Level.INFO, "Created new user file: " + file.getAbsolutePath());
            } catch (Exception e) {
                Logger.debug(Level.SEVERE, "Failed to create user file: " + file.getAbsolutePath());
                e.printStackTrace();
            }

        }

    }

    @Override
    public void deleteUserEntry(UUID uniqueId) {
        File folder = new File(plugin.getDataFolder(), "/users");
        File file = new File(folder, uniqueId.toString() + ".yml");
        if (file.exists()) {
            if (file.delete()) {
                this.files.remove(uniqueId);
                this.yamls.remove(uniqueId);
                Logger.debug(Level.INFO, "Deleted user file: " + file.getAbsolutePath());
            } else {
                Logger.debug(Level.SEVERE, "Failed to delete user file: " + file.getAbsolutePath());
            }
        }
    }

    @Override
    public boolean isUserExist(UUID uniqueId) {
        File folder = new File(plugin.getDataFolder(), "/users");
        File file = new File(folder, uniqueId.toString() + ".yml");
        return file.exists();
    }

}
