package p1xel.minecraft.bukkit.utils.storage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.utils.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class YamlUserData extends UserData{

    private HashMap<UUID, File> files = new HashMap<>();
    private HashMap<UUID, FileConfiguration> yamls = new HashMap<>();

    @Override
    public void init() {
        File folder = new File(MyCompany.getInstance().getDataFolder(), "/users");
        if (!folder.exists()) {
            folder.mkdirs();
            Logger.debug(Level.INFO, "users folder has been created for the first time.");
        }
//        files.clear();
//        yamls.clear();
//        File[] users = folder.listFiles();
//        if (users != null) {
//            for (File file : users) {
//                UUID uniqueId = UUID.fromString(file.getName().replaceFirst("[.][^.]+$", ""));
//                files.put(uniqueId,file);
//                yamls.put(uniqueId, YamlConfiguration.loadConfiguration(file));
//
//            }
//        }
    }

    // Also create Cache
    @Override
    public void createUser(UUID uniqueId) {
        File file = new File(MyCompany.getInstance().getDataFolder() + "/users", uniqueId + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueId);
            yaml.set(uniqueId + ".name", player.getName());
            try {
                yaml.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            files.put(uniqueId, file);
            yamls.put(uniqueId, yaml);

            Logger.log(Level.INFO, "User of " + player.getName() + " has been created for the first time");
            return;

        }

        files.put(uniqueId, file);
        yamls.put(uniqueId, YamlConfiguration.loadConfiguration(file));

    }

    @Override
    public Object get(UUID uniqueId, String path) {
        FileConfiguration yaml = yamls.get(uniqueId);
        return yaml.get(uniqueId + "." + path);
    }

    @Override
    public void set(UUID uniqueId, String path, Object value) {
        File file = files.get(uniqueId);
        FileConfiguration yaml = yamls.get(uniqueId);
        yaml.set(uniqueId + "." + path, value);
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //yamls.replace(uniqueId,yaml);
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
        FileConfiguration yaml = yamls.get(uniqueId);
        for (String order : yaml.getConfigurationSection(uniqueId.toString() + ".orders.progress").getKeys(false)) {
            for (String quest : yaml.getConfigurationSection(uniqueId.toString()+ ".orders.progress." + order).getKeys(false)) {
                String actionName = "";
                int value = -1;
                // Should be one object only!!!
                for (String action : yaml.getConfigurationSection(uniqueId.toString() + ".orders.progress." + order + "." + quest).getKeys(false)) {
                    actionName = action;
                    value = yaml.getInt(uniqueId.toString()+".orders.progress." + order + "." + quest + "." + action);
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
        File file = files.get(uniqueId);
        FileConfiguration yaml = yamls.get(uniqueId);
        for (String quest : EmployeeOrders.yaml.getConfigurationSection(order + ".quest").getKeys(false)) {
            String action = EmployeeOrders.yaml.getString(order + ".quest." + quest + ".type");
            yaml.set(uniqueId.toString()+".orders.progress." + order + "." + quest + "." + action, 0);
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateOrderValue(UUID uniqueId, String order, String quest, int value) {
        File file = files.get(uniqueId);
        FileConfiguration yaml = yamls.get(uniqueId);
        // Should be one object only!!!
        for (String action : yaml.getConfigurationSection(uniqueId.toString() + ".orders.progress." + order + "." + quest).getKeys(false)) {
            yaml.set(uniqueId.toString()+".orders.progress." + order + "." + quest + "." + action, value);
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeOrder(UUID uniqueId, String order) {
        File file = files.get(uniqueId);
        FileConfiguration yaml = yamls.get(uniqueId);
        yaml.set(uniqueId.toString() + ".orders.progress." + order, null);
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeAllOrders(UUID uniqueId) {
        File file = files.get(uniqueId);
        FileConfiguration yaml = yamls.get(uniqueId);
        //for (String order : yaml.getConfigurationSection(uniqueId.toString()+ ".orders.progress").getKeys(false)) {
        yaml.set(uniqueId.toString() + ".orders.progress", null);
        //}
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void randomizeDailyOrder(UUID uniqueId) {
        File file = files.get(uniqueId);
        FileConfiguration yaml = yamls.get(uniqueId);
        //for (String order : yaml.getConfigurationSection(uniqueId.toString()+ ".orders.progress").getKeys(false)) {
        yaml.set(uniqueId + ".orders.progress", null);
        List<String> orderList = new ArrayList<>(EmployeeOrders.getOrderList().stream().toList());
        Collections.shuffle(orderList);
        List<String> randomList = orderList.subList(0, Math.min(orderList.size(), 5));
        yaml.set(uniqueId + ".orders.daily", randomList);
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String order : randomList) {
            createOrderForPlayer(uniqueId, order);
        }
        EmployeeOrders.saveToCache(uniqueId);

    }

    @Override
    public List<String> getDailyOrders(UUID uniqueId) {
        FileConfiguration yaml = yamls.get(uniqueId);
        return yaml.getStringList(uniqueId.toString()+ ".orders.daily");
    }

}
