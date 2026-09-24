package p1xel.minecraft.bukkit.util.storage.driver;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import p1xel.minecraft.bukkit.util.Logger;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class CompanyYamlDriver implements CompanyStorageDriver {

    private final String[] types = new String[]{"info", "settings", "inventory", "shop", "asset", "area"};
    private final JavaPlugin plugin;

    private final HashMap<UUID, HashMap<String, File>> files = new HashMap<>();
    private final HashMap<UUID, HashMap<String, FileConfiguration>> yamls = new HashMap<>();

    public CompanyYamlDriver(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        File companies = new File(plugin.getDataFolder(), "/companies");
        if (!companies.exists()) {
            companies.mkdirs();
            Logger.debug(Level.INFO, "companies folder has been created for the first time.");
        }

        File[] companies_files = companies.listFiles();
        if (companies_files != null) {
            for (File folder : companies_files) {
                if (folder.isDirectory()) {

                    HashMap<String, File> files = new HashMap<>();
                    HashMap<String, FileConfiguration> yamls = new HashMap<>();
                    UUID uniqueId = UUID.fromString(folder.getName());

                    for (String type : types) {
                        File type_file = new File(folder, type + ".yml");
                        if (!type_file.exists()) {
                            try {
                                type_file.createNewFile();
                                Logger.debug(Level.INFO, "Created new file: " + type_file.getAbsolutePath());
                            } catch (Exception e) {
                                Logger.debug(Level.SEVERE, "Failed to create file: " + type_file.getAbsolutePath());
                                e.printStackTrace();
                            }
                        }

                        files.put(type, type_file);
                        yamls.put(type, YamlConfiguration.loadConfiguration(type_file));

                    }

                    this.files.put(uniqueId, files);
                    this.yamls.put(uniqueId, yamls);
                }
            }
        } else {
            Logger.debug(Level.INFO, "No company files found in the companies folder.");
        }
    }

    @Override
    public void close() {

    }

    @Override
    public Object get(UUID uniqueId, String type, String key) {
        FileConfiguration yaml = this.yamls.get(uniqueId).get(type);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.get(path);
    }

    @Override
    public void set(UUID uniqueId, String type, String key, Object value) {
        FileConfiguration yaml = this.yamls.get(uniqueId).get(type);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        yaml.set(path, value);
    }

    @Override
    public Set<String> getKeys(UUID uniqueId, String type, String key) {
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        ConfigurationSection section = this.yamls.get(uniqueId).get(type).getConfigurationSection(path);
        if (section == null) { return Collections.emptySet(); }
        return section.getKeys(false);
    }

    @Override
    public void save(UUID uniqueId, String type) {
        File file = this.files.get(uniqueId).get(type);
        FileConfiguration yaml = this.yamls.get(uniqueId).get(type);
        try {
            yaml.save(file);
        } catch (Exception e) {
            Logger.debug(Level.SEVERE, "Failed to save file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    @Override
    public double getDouble(UUID uniqueId, String type, String key) {
        FileConfiguration yaml = this.yamls.get(uniqueId).get(type);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getDouble(path);
    }

    @Override
    public String getString(UUID uniqueId, String type, String key) {
        FileConfiguration yaml = this.yamls.get(uniqueId).get(type);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getString(path);
    }

    @Override
    public boolean getBoolean(UUID uniqueId, String type, String key) {
        FileConfiguration yaml = this.yamls.get(uniqueId).get(type);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getBoolean(path);
    }

    @Override
    public int getInt(UUID uniqueId, String type, String key) {
        FileConfiguration yaml = this.yamls.get(uniqueId).get(type);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getInt(path);
    }

    @Override
    public float getFloat(UUID uniqueId, String type, String key) {
        FileConfiguration yaml = this.yamls.get(uniqueId).get(type);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return (float) yaml.getDouble(path);
    }

    @Override
    public List<String> getStringList(UUID uniqueId, String type, String key) {
        FileConfiguration yaml = this.yamls.get(uniqueId).get(type);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getStringList(path);
    }

    @Override
    public long getLong(UUID uniqueId, String type, String key) {
        FileConfiguration yaml = this.yamls.get(uniqueId).get(type);
        String path = key != null ? uniqueId + "." + key : uniqueId.toString();
        return yaml.getLong(path);
    }

    @Override
    public void createCompanyEntry(UUID uniqueId) {
        File folder = new File(plugin.getDataFolder(), "/companies/" + uniqueId.toString());
        if (!folder.exists()) {
            folder.mkdirs();
            Logger.debug(Level.INFO, "Created new company folder: " + folder.getAbsolutePath());
        }

        for (String type : types) {
            File type_file = new File(folder, type + ".yml");
            if (!type_file.exists()) {
                try {
                    type_file.createNewFile();
                    Logger.debug(Level.INFO, "Created new file: " + type_file.getAbsolutePath());
                } catch (Exception e) {
                    Logger.debug(Level.SEVERE, "Failed to create file: " + type_file.getAbsolutePath());
                    e.printStackTrace();
                }
            }

            this.files.get(uniqueId).put(type, type_file);
            this.yamls.get(uniqueId).put(type, YamlConfiguration.loadConfiguration(type_file));
        }

    }

    @Override
    public void deleteCompanyEntry(UUID uniqueId) {
        File folder = new File(plugin.getDataFolder(), "/companies/" + uniqueId.toString());
        if (folder.exists()) {
            for (File file : folder.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                    Logger.debug(Level.INFO, "Deleted file: " + file.getAbsolutePath());
                }
            }
            folder.delete();
            Logger.debug(Level.INFO, "Deleted company folder: " + folder.getAbsolutePath());
        }
    }

    @Override
    public List<UUID> loadAllCompanyEntries() {
        List<UUID> companyUUIDs = new ArrayList<>();
        File companiesFolder = new File(plugin.getDataFolder(), "/companies");
        if (companiesFolder.exists() && companiesFolder.isDirectory()) {
            for (File folder : companiesFolder.listFiles()) {
                if (folder.isDirectory()) {
                    try {
                        UUID uniqueId = UUID.fromString(folder.getName());
                        companyUUIDs.add(uniqueId);
                    } catch (IllegalArgumentException e) {
                        Logger.debug(Level.WARNING, "Invalid UUID folder name: " + folder.getName());
                    }
                }
            }
        }
        return companyUUIDs;
    }
}
