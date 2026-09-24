package p1xel.minecraft.bukkit.util.storage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.util.storage.driver.UserStorageDriver;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class UserData extends AbstractUserData {

    private final UserStorageDriver driver;

    public UserData(UserStorageDriver driver) {
        this.driver = driver;
    }

    @Override
    public void init() {
        driver.init();
    }

    // Also create Cache
    @Override
    public void createUser(UUID uniqueId) {
        driver.createUserEntry(uniqueId);

        OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueId);
        set(uniqueId, "name", player.getName());

        save(uniqueId);

    }

    @Override
    public User buildUser(UUID uniqueId) {
        User user = User.builder(uniqueId)
                .companyUniqueId(getCompanyUUID(uniqueId))
                .position(getPosition(uniqueId))
                .employeeOrders(new HashMap<>())
                .build();
        return user;
    }

    @Override
    public Object get(UUID uniqueId, String path) {
        return driver.get(uniqueId, path);
    }

    public String getString(UUID uniqueId, String key) {
        return driver.getString(uniqueId, key);
    }

    public int getInt(UUID uniqueId, String key) {
        return driver.getInt(uniqueId, key);
    }

    public double getDouble(UUID uniqueId, String key) {
        return driver.getDouble(uniqueId, key);
    }

    public long getLong(UUID uniqueId, String key) {
        return driver.getLong(uniqueId, key);
    }

    public boolean getBoolean(UUID uniqueId, String key) {
        return driver.getBoolean(uniqueId, key);
    }

    public List<String> getStringList(UUID uniqueId, String key) {
        return driver.getStringList(uniqueId, key);
    }

    public float getFloat(UUID uniqueId, String key) {
        return driver.getFloat(uniqueId, key);
    }

    public Set<String> getKeys(UUID uniqueId, String key) {
        return driver.getKeys(uniqueId, key);
    }

    @Override
    public void set(UUID uniqueId, String key, Object value) {
        driver.set(uniqueId, key, value);
    }

    public void save(UUID uniqueId) {
        driver.save(uniqueId);
    }

    @Override
    public String getName(UUID uniqueId) {
        return (String) get(uniqueId, "name");
    }

    @Override
    public boolean isUserExist(UUID uniqueId) {
        File file = new File(MyCompany.getInstance().getDataFolder() +"/users", uniqueId + ".yml");
        return file.exists();
    }

    @Override
    public void setCompany(UUID playerUniqueId, UUID companyUniqueId) {
        String uuid;
        if (companyUniqueId != null ) {
            uuid = String.valueOf(companyUniqueId);
        } else {
            uuid = null;
        }
        set(playerUniqueId, "company.uuid", uuid);
    }

    @Override
    @Nullable
    public UUID getCompanyUUID(UUID uniqueId) {
        try {
            return UUID.fromString((String) get(uniqueId, "company.uuid"));
        } catch (NullPointerException exception){
            return null;
        }
    }

    @Override
    public void setPosition(UUID uniqueId, String position) {
        set(uniqueId, "company.position", position);
    }

    @Override
    public String getPosition(UUID uniqueId) {
        return (String) get(uniqueId, "company.position");
    }

    @Override
    public List<String> getOrdersInProgress(UUID uniqueId) {
        // Format: "orderName:questName:actionName:Value"
        List<String> list = new ArrayList<>();
        for (String order : getKeys(uniqueId,  "orders.progress")) {
            for (String quest : getKeys(uniqueId, "orders.progress." + order)) {
                String actionName = "";
                int value = -1;
                // Should be one object only!!!
                for (String action : getKeys(uniqueId, "orders.progress." + order + "." + quest)) {
                    actionName = action;
                    value = getInt(uniqueId, "orders.progress." + order + "." + quest + "." + action);
                }

                if (actionName.isEmpty() || value < 0) {
                    continue;
                }

                String name = order + ":" + quest + ":" + actionName + ":" + String.valueOf(value);
                list.add(name);

            }
        }
        return list;
    }

    @Override
    public void createOrderForPlayer(UUID uniqueId, String order) {
        for (String quest : EmployeeOrders.yaml.getConfigurationSection(order + ".quest").getKeys(false)) {
            String action = EmployeeOrders.yaml.getString(order + ".quest." + quest + ".type");
            set(uniqueId, "orders.progress." + order + "." + quest + "." + action, 0);
        }

        save(uniqueId);

    }

    @Override
    public void updateOrderValue(UUID uniqueId, String order, String quest, int value) {
        // Should be one object only!!!
        for (String action : getKeys(uniqueId, "orders.progress." + order + "." + quest)) {
            set(uniqueId, "orders.progress." + order + "." + quest + "." + action, value);
        }
        save(uniqueId);
    }

    @Override
    public void removeOrder(UUID uniqueId, String order) {
        set(uniqueId, "orders.progress." + order, null);
        save(uniqueId);
    }

    @Override
    public void removeAllOrders(UUID uniqueId) {
        //for (String order : yaml.getConfigurationSection(uniqueId.toString()+ ".orders.progress").getKeys(false)) {
        set(uniqueId, "orders.progress", null);
        //}
        save(uniqueId);
    }

    @Override
    public void randomizeDailyOrder(UUID uniqueId) {
        //for (String order : yaml.getConfigurationSection(uniqueId.toString()+ ".orders.progress").getKeys(false)) {
        set(uniqueId, "orders.progress", null);
        List<String> orderList = new ArrayList<>(EmployeeOrders.getOrderList().stream().toList());
        Collections.shuffle(orderList);
        List<String> randomList = orderList.subList(0, Math.min(orderList.size(), 5));
        set(uniqueId, "orders.daily", randomList);
        save(uniqueId);
        for (String order : randomList) {
            createOrderForPlayer(uniqueId, order);
        }
        EmployeeOrders.saveToCache(uniqueId);

    }

    @Override
    public List<String> getDailyOrders(UUID uniqueId) {
        return getStringList(uniqueId, "orders.daily");
    }

}
