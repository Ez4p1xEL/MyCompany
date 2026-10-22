package p1xel.minecraft.bukkit.util.storage.menu;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.util.storage.Locale;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuUtil {

    private static final HashMap<String, File> files = new HashMap<>();
    private static final HashMap<String, FileConfiguration> yamls = new HashMap<>();
    private static final HashMap<String, Menu> menus = new HashMap<>();
    private static MenuItem noPermItem;

    private static final String[] menuFiles = {
        "main","internal_store", "orders", "areas", "area_trade_market", "area_rent_market", "area_sale_market",
            "area_list", "found", "personnel", "player_list", "position_list", "position", "position_permission_list",
            "chestshop", "confirmation", "focus", "focus_list"
    };

    public static void createFiles() {

        File folder = new File(MyCompany.getInstance().getDataFolder(), "/menu");
        folder.mkdirs();

        for (String fileName : menuFiles) {
            File file = new File(folder, fileName + ".yml");
            if (!file.exists()) {
                MyCompany.getInstance().saveResource("menu/" + fileName + ".yml", false);
            }
            files.put(fileName, file);
            yamls.put(fileName, YamlConfiguration.loadConfiguration(file));
        }

        init();
    }

    public static void init() {


        for (Map.Entry<String, FileConfiguration> entry : yamls.entrySet()) {
            String id = entry.getKey();
            FileConfiguration yaml = entry.getValue();

            String locale_path = "menu." + id + ".";
            String title = Locale.getMessage(locale_path + "title");
            int size = yaml.getInt("size");

            Menu menu = new Menu(id, title, size);
            HashMap<String, MenuItem> items = new HashMap<>();

            for (String itemId : yaml.getConfigurationSection("items").getKeys(false)) {

                String itemPath = "items." + itemId + ".";

                String materialString = yaml.getString(itemPath + "material");

                // 处理 如果只有一个 slot 的情况，直接读取 int 类型
                List<Integer> slots = new ArrayList<>();
                int slot = yaml.getInt(itemPath + "slot", -1);
                if (slot < 0) {
                    // 如果 slot 小于 0，尝试读取 slots 列表
                    List<Integer> slotList = yaml.getIntegerList(itemPath + "slots");
                    if (!slotList.isEmpty()) {
                        slots.addAll(slotList);
                    }
                } else {
                    slots.add(slot);
                }

                String displayName = Locale.yaml.getString(locale_path + itemPath + "display_name", "");
                List<String> lore = Locale.yaml.getStringList(locale_path + itemPath + "lore");

                MenuItem menuItem = new MenuItem(itemId, materialString, slots);
                menuItem.setDisplayName(displayName);
                menuItem.setLore(lore);
                items.put(itemId, menuItem);

            }

            menu.setItems(items);

            // Options (Optional)
            if (yaml.contains("options")) {
                for (String optionKey : yaml.getConfigurationSection("options").getKeys(false)) {
                    String optionValue = yaml.getString("options." + optionKey);
                    boolean isString = false;
                    try {
                        int intValue = Integer.parseInt(optionValue);
                        menu.getOptionsInt().put(optionKey, intValue);
                    } catch (NumberFormatException e) {
                        if (optionValue.equalsIgnoreCase("true") || optionValue.equalsIgnoreCase("false")) {
                            boolean boolValue = Boolean.parseBoolean(optionValue);
                            menu.getOptionsBool().put(optionKey, boolValue);
                        } else {
                            isString = true;
                        }
                    }

                    if (isString) {
                        menu.getOptions().put(optionKey, optionValue);
                    }
                }
            }

            // Locale Actions (Optional)
            // 这个是多用途菜单使用的，比如领地列表，每个传送、删除等打开的菜单里的执行操作都不一样
            if (Locale.yaml.contains(locale_path+"actions")) {
                for (String actionKey : Locale.yaml.getConfigurationSection(locale_path+"actions").getKeys(false)) {
                    HashMap<String, String> actionMap = new HashMap<>();

                    for (String action : Locale.yaml.getConfigurationSection(locale_path+"actions."+actionKey).getKeys(false)) {
                        String value = Locale.yaml.getString(locale_path+"actions."+actionKey+"."+action);
                        actionMap.put(action, value);
                    }

                    menu.getActions().put(actionKey, actionMap);

                }
            }

            menus.put(id, menu);

        }

        // init global items
        String noPermMaterial = MenuConfig.GLOBAL_NO_PERM;
        List<String> noPermLore = Locale.yaml.getStringList("menu.global.no-perm.lore");
        noPermItem = new MenuItem("no-perm", noPermMaterial, List.of());
        noPermItem.setLore(noPermLore);
        noPermItem.setDisplayName(null);

    }

    public static Menu getMenu(String id) {
        return menus.get(id).clone();
    }

}
