package p1xel.minecraft.bukkit.utils.storage;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.EmployeeOrder;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.UserManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeOrders {

    public static File file;
    public static FileConfiguration yaml;
    private static HashMap<UUID, HashMap<String, EmployeeOrder>> orders = new HashMap<>(); // orderName, EmployeeOrder
    private static final UserManager userManager = MyCompany.getCacheManager().getUserManager();

    public static void init() {

        File file = new File(MyCompany.getInstance().getDataFolder(), "orders.yml");
        if (!file.exists()) {
            MyCompany.getInstance().saveResource("orders.yml", false);
        }

        upload(file, YamlConfiguration.loadConfiguration(file));
    }

    public static void upload(File f, FileConfiguration y) {
        file = f;
        yaml = y;
    }

    public static void set(String path, Object value) {
        yaml.set(path,value);
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLabel(String name) {
        return ChatColor.translateAlternateColorCodes('&',yaml.getString(name + ".label"));
    }

    public static List<String> getDescription(String name) {
        return yaml.getStringList(name+".description").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
    }

    public static Set<String> getQuests(String name) {
        return yaml.getConfigurationSection(name + ".quest").getKeys(false);
    }

    public static Set<String> getOrderList() {
        return yaml.getKeys(false);
    }

    public static String getType(String name, String quest) {
        return yaml.getString(name + ".quest." + quest + ".type");
    }

    public static Object getValue(String name, String quest, String key) {
        return yaml.get(name + ".quest." + quest + "." + key);
    }

    public static int getValue(String name, String quest) {
        return yaml.getInt(name + ".quest." + quest + ".amount");
    }

    public static List<String> getRewards(String name) {
        return yaml.getStringList(name + ".reward");
    }

    public static HashMap<UUID, HashMap<String, EmployeeOrder>> getAllOrders() {
        return orders;
    }

    public static HashMap<String, EmployeeOrder> getPlayerOrders(UUID playerUniqueId) {
        return orders.get(playerUniqueId);
    }

    public static void createEmptyMap(UUID playerUniqueId) {
        HashMap<String, EmployeeOrder> map = new HashMap<>();
        orders.put(playerUniqueId, map);
    }

    public static void clearOrders(UUID playerUniqueId) {
        HashMap<String, EmployeeOrder> orders_map =  orders.get(playerUniqueId);
        if (orders_map != null) {
            orders_map.clear();
        }
    }

    public static void saveToCache(UUID playerUniqueId) {

        clearOrders(playerUniqueId);
        List<String> list = userManager.getOrdersInProgress(playerUniqueId);
        if (list.isEmpty()) {
            return;
        }
        list.add("default:default");
        String tempOrder = "";
        List<String> tempProgress = new ArrayList<>();
        boolean fuckYou = true;
        for (String objects : list) {
            HashMap<String, EmployeeOrder> orders_map =  orders.get(playerUniqueId);
            String[] obj = objects.split(":", 2);
            // 0 = orderName, 1 = rest
            if (!tempOrder.equalsIgnoreCase(obj[0])) {
                if (!fuckYou) {
                    EmployeeOrder order = new EmployeeOrder(playerUniqueId, tempOrder, tempProgress);
                    if (orders_map != null) {
                        orders_map.put(tempOrder, order);
                    } else {
                        HashMap<String, EmployeeOrder> new_map = new HashMap<>();
                        new_map.put(tempOrder, order);
                        orders.put(playerUniqueId, new_map);
                    }

                }
                if (obj[0].equalsIgnoreCase("default")) {
                    break;
                }
                tempOrder = obj[0];
                tempProgress.clear();
                fuckYou = false;
                tempProgress.add(obj[1]);
            } else {
                tempProgress.add(obj[1]);
            }

        }

    }

    public static void acceptOrder(UUID playerUniqueId, String order) {
        userManager.createOrderForPlayer(playerUniqueId, order);
        saveToCache(playerUniqueId);
    }

    public static void saveCacheToLocal() {
        for (UUID playerUniqueId : orders.keySet()) {
            for (String order_name : orders.get(playerUniqueId).keySet()) {
                EmployeeOrder order = orders.get(playerUniqueId).get(order_name);
                for (String quest_name : order.getValues().keySet()) {
                    String fixed_quest_name = quest_name.split(":")[0]; // Remove action_name
                    userManager.updateOrderValue(playerUniqueId, order_name, fixed_quest_name, order.getValues().get(quest_name));
                }
            }
        }
    }


    public static void removeCacheOrder(UUID playerUniqueId, String order) {
        HashMap<String, EmployeeOrder> map = orders.get(playerUniqueId);
        map.remove(order);
        userManager.removeOrder(playerUniqueId, order);
    }

    public static void removeAllCacheOrders(UUID playerUniqueId) {
        HashMap<String, EmployeeOrder> map = orders.get(playerUniqueId);
        map.clear();
        orders.remove(playerUniqueId);
    }

    public static String getProgressMessage(UUID playerUniqueId, String order, String quest) {
        EmployeeOrder employeeOrder = orders.get(playerUniqueId).get(order);
        String type = EmployeeOrders.getType(order, quest);
        String progressMessage = Locale.getMessage("quest-progress");
        String description = "";
        String target = "";
        String progress_value = "";
        String target_value = "";

        switch (type) {
            case "break_block":
                description = Locale.getMessage("action-description.break_block");
                target = EmployeeOrders.yaml.getString(order + ".quest." + quest + ".item").toUpperCase();
                progress_value = String.valueOf(employeeOrder.getProgressValue(quest + ":break_block"));
                target_value = String.valueOf(EmployeeOrders.getValue(order, quest));
                break;

            case "place_block":
                description = Locale.getMessage("action-description.place_block");
                target = EmployeeOrders.yaml.getString(order + ".quest." + quest + ".item").toUpperCase();
                progress_value = String.valueOf(employeeOrder.getProgressValue(quest + ":place_block"));
                target_value = String.valueOf(EmployeeOrders.getValue(order, quest));
                break;

            case "mob_kill":
                description = Locale.getMessage("action-description.mob_kill");
                target = EmployeeOrders.yaml.getString(order + ".quest." + quest + ".mob").toUpperCase();
                progress_value = String.valueOf(employeeOrder.getProgressValue(quest + ":mob_kill"));
                target_value = String.valueOf(EmployeeOrders.getValue(order, quest));
                break;
        }
        progressMessage = progressMessage.replaceAll("%action_description%", description)
                .replaceAll("%target%", target)
                .replaceAll("%progress_value%", progress_value)
                .replaceAll("%target_value%", target_value);
        return progressMessage;
    }


}
