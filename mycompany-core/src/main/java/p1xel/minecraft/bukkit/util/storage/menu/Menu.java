package p1xel.minecraft.bukkit.util.storage.menu;

import java.util.HashMap;

public class Menu {

    private final String id;
    private final String title;
    private final int size;
    private HashMap<String, MenuItem> items = new HashMap<>();
    private HashMap<String, Integer> optionsInt = new HashMap<>();
    private HashMap<String, String> options = new HashMap<>();
    private HashMap<String, Boolean> optionsBool = new HashMap<>();
    private HashMap<String, HashMap<String, String>> actions = new HashMap<>();

    public Menu(String id, String title, int size) {
        this.id = id;
        this.title = title;
        this.size = size;
    }

    public Menu(Menu menu) {
        this.id = menu.getId();
        this.title = menu.getTitle();
        this.size = menu.getSize();
        for (var entry : menu.getItems().entrySet()) {
            this.items.put(entry.getKey(), entry.getValue().clone());
        }
        this.optionsInt = new HashMap<>(menu.getOptionsInt());
        this.options = new HashMap<>(menu.getOptions());
        this.optionsBool = new HashMap<>(menu.getOptionsBool());
        this.actions = new HashMap<>(menu.getActions());
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public HashMap<String, MenuItem> getItems() {
        return items;
    }

    public HashMap<String, Integer> getOptionsInt() {
        return optionsInt;
    }

    public HashMap<String, String> getOptions() {
        return options;
    }

    public HashMap<String, Boolean> getOptionsBool() {
        return optionsBool;
    }

    public int getInt(String key, int defaultValue) {
        return optionsInt.getOrDefault(key, defaultValue);
    }

    public String getString(String key, String defaultValue) {
        return options.getOrDefault(key, defaultValue);
    }

    public boolean getBool(String key, boolean defaultValue) { return optionsBool.getOrDefault(key, defaultValue); }

    public HashMap<String, HashMap<String, String>> getActions() { return actions; }

    // Setters
    public void setItems(HashMap<String, MenuItem> items) {
        this.items.clear();
        this.items.putAll(items);
    }

    public void setOptionsInt(HashMap<String, Integer> optionsInt) {
        this.optionsInt.clear();
        this.optionsInt.putAll(optionsInt);
    }

    public void setOptions(HashMap<String, String> options) {
        this.options.clear();
        this.options.putAll(options);
    }

    public void setOptionsBool(HashMap<String, Boolean> optionsBool) {
        this.optionsBool.clear();
        this.optionsBool.putAll(optionsBool);
    }

    public void setActions(HashMap<String, HashMap<String, String>> actions) {
        this.actions.clear();
        this.actions.putAll(actions);
    }

    public Menu clone() {
        return new Menu(this);
    }

}
