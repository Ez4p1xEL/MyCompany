package p1xel.minecraft.bukkit.utils.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.Company;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.Logger;
import p1xel.minecraft.bukkit.utils.storage.cidstorage.CIdData;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class YamlCompanyData extends CompanyData{

    private HashMap<UUID, HashMap<String, File>> com_files = new HashMap<>();
    private HashMap<UUID, HashMap<String, FileConfiguration>> com_yamls = new HashMap<>();
    private List<UUID> companyList = new ArrayList<>();

    @Override
    public void init() {
        com_files.clear();
        com_yamls.clear();
        companyList.clear();
        File companies = new File(MyCompany.getInstance().getDataFolder(), "/companies");
        if (!companies.exists()) {
            companies.mkdirs();
            Logger.debug(Level.INFO, "companies folder has been created for the first time.");
        }

        // Folder companies - BEGIN
        // Update the current yaml file to cache
        // Check if the cache contains file/yaml.
        File[] companies_files = companies.listFiles();
        if (companies_files != null) {
            for (File folder : companies_files) {

                UUID uniqueId = UUID.fromString(folder.getName());

                String[] filesName = new String[]{"info", "settings", "inventory", "shop", "asset"};

                if (com_files.get(uniqueId) == null) {
                    HashMap<String, File> files_map = new HashMap<>();
                    HashMap<String, FileConfiguration> yamls_map = new HashMap<>();
                    for (String fileName : filesName) {
                        File file = new File(MyCompany.getInstance().getDataFolder() + "/companies/" + uniqueId, fileName + ".yml");

                        //com_files.put(uniqueId, k -> new HashMap<>()).put(fileName, file);
                        files_map.put(fileName, file);
                        yamls_map.put(fileName, YamlConfiguration.loadConfiguration((file)));
                    }
                    com_files.put(uniqueId, files_map);
                    com_yamls.put(uniqueId, yamls_map);
//                } else {
//                    for (String fileName : filesName) {
//                        File file = new File(MyCompany.getInstance().getDataFolder() + "/companies/" + uniqueId, fileName + ".yml");
//
//                        HashMap<String, File> files_map = com_files.get(uniqueId);
//                        HashMap<String, FileConfiguration> yamls_map = com_yamls.get(uniqueId);
//                        files_map.replace(fileName, file);
//                        yamls_map.replace(fileName, YamlConfiguration.loadConfiguration(file));
//
//                    }
                }

                companyList.add(uniqueId);
                int cid = getId(uniqueId);
                MyCompany.getCacheManager().getCompanyManager().getCIds().put(cid, uniqueId);
            }

        }

        // Folder companies - END
//        // Folder users - BEGIN
//
//        File[] users_files = companies.listFiles();
//        if (users_files != null) {
//            for (File folder : companies_files) {
//
//                UUID uniqueId = UUID.fromString(folder.getName());
//
//                String[] filesName = new String[]{"info", "settings", "inventory", "shop"};
//                for (String fileName : filesName) {
//                    File file = new File(MyCompany.getInstance().getDataFolder() + "/companies/" + uniqueId, fileName + ".yml");
//
//                    //com_files.put(uniqueId, k -> new HashMap<>()).put(fileName, file);
//                    if (com_files.get(uniqueId) == null) {
//
//                        HashMap<String, File> files_map = new HashMap<>();
//                        HashMap<String, FileConfiguration> yamls_map = new HashMap<>();
//                        files_map.put(fileName, file);
//                        yamls_map.put(fileName, YamlConfiguration.loadConfiguration((file)));
//
//                    } else {
//                        HashMap<String, File> files_map = com_files.get(uniqueId);
//                        HashMap<String, FileConfiguration> yamls_map = com_yamls.get(uniqueId);
//                        files_map.replace(fileName, file);
//                        yamls_map.replace(fileName, YamlConfiguration.loadConfiguration(file));
//                    }
//
//
//                }
//            }
//
//        }
//
//        // Folder users - END
    }

//    private void createCache(UUID uniqueId, File file, FileConfiguration yaml) {
//        HashMap<String, File> files_map = new HashMap<>();
//        HashMap<String, FileConfiguration> yamls_map = new HashMap<>();
//        files_map.put();
//    }

    @Override
    public List<UUID> getCompaniesUUID() {
        List<UUID> list = new ArrayList<>();
        File companies = new File(MyCompany.getInstance().getDataFolder(), "/companies");
        File[] companies_files = companies.listFiles();
        for (File company : companies_files) {
            list.add(UUID.fromString(company.getName()));
        }
        return list;
    }

    @Override
    public List<String> getCompaniesName() {
        List<String> list = new ArrayList<>();
        File companies = new File(MyCompany.getInstance().getDataFolder(), "/companies");
        File[] companies_files = companies.listFiles();
        for (File company : companies_files) {
            UUID uniqueId = UUID.fromString(company.getName());
            // File file = new File(MyCompany.getInstance().getDataFolder() + "/companies/" + uniqueId, "info.yml");
            list.add(getName(uniqueId));
        }
        return list;
    }

    @Override
    public Company getCompany(UUID uniqueId) {
        return new Company(uniqueId);
    }

    @Override
    public void set(UUID uniqueId, String type, String key, Object value) {
        File file = com_files.get(uniqueId).get(type);
        FileConfiguration yaml = com_yamls.get(uniqueId).get(type);
        yaml.set(uniqueId + "." + key, value);
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 備注: 先從 CreateCompany 將 cache 丟入 Company, Company 如有改動再從内部調用 CompanyManager 修改, 再從上方 set() 存入 Company 以更改緩存
    @Override
    public void createCompany(String companyName, UUID playerUniqueId) {
        UUID uuid = UUID.randomUUID();
        File folder = new File(MyCompany.getInstance().getDataFolder() + "/companies", uuid.toString());
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 逐個創建文件
        HashMap<String, File> files_map = new HashMap<>();
        HashMap<String, FileConfiguration> yamls_map = new HashMap<>();
        String[] filesName = new String[]{"info", "settings", "inventory", "shop", "asset"};
        for (String fileName : filesName) {
            File file = new File(MyCompany.getInstance().getDataFolder() + "/companies/" + uuid, fileName+ ".yml");
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            if (fileName.equals("info")) {
                yaml.set(uuid + ".name", companyName);
                yaml.set(uuid + ".id", CIdData.getAndUpdateCID());
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Config.getString("company-settings.time-format"));
                String formattedTime = now.format(formatter);
                yaml.set(uuid + ".found-date", formattedTime);
                yaml.set(uuid + ".founder.uuid", String.valueOf(playerUniqueId));

                OfflinePlayer player = Bukkit.getOfflinePlayer(playerUniqueId);

                yaml.set(uuid + ".founder.name", player.getName());
                yaml.set(uuid + ".members.employer", String.valueOf(playerUniqueId));
                //yaml.set(uuid + ".members.employee", null);

                try {
                    yaml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            if (fileName.equalsIgnoreCase("asset")) {

                yaml.set(uuid + ".cash", Config.getDouble(("company-funds.default-asset")));
                yaml.set(uuid + ".income.total", 0.0);
                yaml.set(uuid + ".income.daily", 0.0);

                try {
                    yaml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            if (fileName.equalsIgnoreCase("settings")) {

                // Salary
                yaml.set(uuid + ".salary.employer", 1000.0);
                yaml.set(uuid + ".salary.employee", 500.0);

                // Position
                // Label
                yaml.set(uuid + ".position.default.employer.label", "Employer");
                yaml.set(uuid + ".position.default.employee.label", "Employee");
                // Permission
                yaml.set(uuid + ".position.default.employer.permission", Collections.singletonList("all"));
                yaml.set(uuid + ".position.default.employee.permission", Config.getStringList("company-settings.employee-default-permission"));

                try {
                    yaml.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            files_map.put(fileName, file);
            yamls_map.put(fileName, yaml);
        }

        com_files.put(uuid,files_map);
        com_yamls.put(uuid,yamls_map);
        companyList.add(uuid);
        int cid = getId(uuid);
        MyCompany.getCacheManager().getCompanyManager().getCIds().put(cid, uuid);

        MyCompany.getCacheManager().getUserManager().createUser(playerUniqueId);
        MyCompany.getCacheManager().getUserManager().setCompany(playerUniqueId, uuid);
        MyCompany.getCacheManager().getUserManager().setPosition(playerUniqueId, "employer");

    }

    @Override
    public Object get(UUID uniqueId, String type, String path) {
        FileConfiguration yaml = com_yamls.get(uniqueId).get(type);
        return yaml.get(uniqueId + "." + path);
    }

    // info.yml - BEGIN

    @Override
    public String getName(UUID uniqueId) {
        return (String) get(uniqueId, "info", "name");
    }

    @Override
    public int getId(UUID uniqueId) {
        return (Integer) get(uniqueId, "info", "id");
    }

    @Override
    public UUID getFounder(UUID uniqueId) {
        return  UUID.fromString((String)get(uniqueId, "info", "founder.uuid"));
    }

    // This is not recommended for use to get the current name of founder.
    // In online-mode server, player name can be changed but uuid cannot.
    @Override
    public String getFounderName(UUID uniqueId) {
        return (String) get(uniqueId, "info", "founder.name");
    }

    // Get the current owner/employer of the company.
    // Founder and current employer can be different.
    @Override
    public UUID getEmployer(UUID uniqueId) {
        return UUID.fromString((String) get(uniqueId, "info", "members.employer"));
    }

    @Override
    @Nullable
    public List<UUID> getEmployeeList(UUID uniqueId, String position) {
        List<UUID> list = new ArrayList<>();
        Object origin = get(uniqueId, "info", "members." + position);
        if (!(origin instanceof List<?>)) {
            return list;
        }
        for (String member : (List<String>) origin) {
            list.add(UUID.fromString(member));
        }

        return list;

    }

    @Override
    public void dismissEmployee(UUID companyUniqueId, UUID employeeUniqueId) {
        String position = MyCompany.getCacheManager().getUserManager().getPosition(employeeUniqueId);
        List<String> list = (List<String>) get(companyUniqueId, "info", "members." + position);
        list.remove(String.valueOf(employeeUniqueId));
        set(companyUniqueId, "info", "members." + position, list);
        MyCompany.getCacheManager().getUserManager().setCompany(employeeUniqueId, null);
        MyCompany.getCacheManager().getUserManager().setPosition(employeeUniqueId, null);
    }

    @Override
    public void disbandCompany(UUID uniqueId) {
        int cid = getId(uniqueId);

        // Remove employer
        UUID employerUniqueId = getEmployer(uniqueId);
        MyCompany.getCacheManager().getUserManager().setCompany(employerUniqueId, null);
        MyCompany.getCacheManager().getUserManager().setPosition(employerUniqueId, null);

        FileConfiguration yaml = com_yamls.get(uniqueId).get("info");
        // Remove employees
        for (String position : yaml.getConfigurationSection(uniqueId + ".members").getKeys(false)) {
            if (position.equalsIgnoreCase("employer")) {
                continue;
            }

            for (String playerUniqueIdString : yaml.getStringList(uniqueId + ".members." + position)) {
                UUID playerUniqueId = UUID.fromString(playerUniqueIdString);
                MyCompany.getCacheManager().getUserManager().setCompany(playerUniqueId, null);
                MyCompany.getCacheManager().getUserManager().setPosition(playerUniqueId, null);
            }
        }

        // Files removal
        File folder = new File(MyCompany.getInstance().getDataFolder() + "/companies", "/" + uniqueId);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        folder.delete();

        com_files.remove(uniqueId);
        com_yamls.remove(uniqueId);
        MyCompany.getCacheManager().getCompanyManager().getCIds().remove(cid);
        companyList.remove(uniqueId);
    }

    @Override
    public void employPlayer(UUID companyUniqueId, UUID playerUniqueId, String position) {
        List<String> list = (List<String>) get(companyUniqueId, "info", "members." + position);
        if (list == null) { list = new ArrayList<>(); }
        list.add(String.valueOf(playerUniqueId));
        set(companyUniqueId, "info", "members." + position, list);
        MyCompany.getCacheManager().getUserManager().setCompany(playerUniqueId, companyUniqueId);
        MyCompany.getCacheManager().getUserManager().setPosition(playerUniqueId, position);
    }

    @Override
    public String getFoundDate(UUID uniqueId) {
        return (String) get(uniqueId, "info", "found-date");
    }

    @Override
    public int getMemberAmount(UUID uniqueId) {
        int amount = 1;
        FileConfiguration yaml = com_yamls.get(uniqueId).get("info");
        for (String position : yaml.getConfigurationSection(uniqueId + ".members").getKeys(false)) {
            if (position.equalsIgnoreCase("employer")) {
                continue;
            }
            amount = amount + yaml.getStringList(uniqueId + ".members." + position).size();
        }
        return amount;
    }

    @Override
    @Nullable
    public UUID getUUIDFromId(int cid) {
        for (UUID uniqueId : com_yamls.keySet()) {
            if ((int) get(uniqueId, "info", "id") == cid) {
                return uniqueId;
            }
        }
        return null;
    }

    @Override
    public List<String> getPositions(UUID uniqueId) {
        List<String> list = new ArrayList<>();
        list.add("employer");
        list.add("employee");
        ConfigurationSection section = com_yamls.get(uniqueId).get("settings").getConfigurationSection(uniqueId + ".position.custom");
        if (section == null) {
            return list;
        }
        list.addAll(section.getKeys(false));
        return list;
    }

    // info.yml - END

    // asset.yml - BEGIN

    @Override
    public double getCash(UUID uniqueId) {
        return (double) get(uniqueId, "asset", "cash");
    }

    @Override
    public void setCash(UUID uniqueId, double amount) {
        set(uniqueId, "asset", "cash", amount);
    }

    @Override
    public double getDailyIncome(UUID uniqueId) {
        return (double) get(uniqueId, "asset", "income.daily");
    }

    @Override
    public double getTotalIncome(UUID uniqueId) {
        return (double) get(uniqueId, "asset", "income.total");
    }

    @Override
    public void giveMoney(UUID uniqueId, double amount) {
        double currentIncomeDaily = getDailyIncome(uniqueId);
        double currentIncomeTotal = getTotalIncome(uniqueId);
        double cash = getCash(uniqueId);
        set(uniqueId, "asset", "income.daily", currentIncomeDaily + amount);
        set(uniqueId, "asset", "income.total", currentIncomeTotal + amount);
        setCash(uniqueId, cash+amount);
    }

    @Override
    public void takeMoney(UUID uniqueId, double amount) {
        double cash = getCash(uniqueId);
        setCash(uniqueId, cash-amount);
    }

    @Override
    public void resetDailyIncome(UUID uniqueId) {
        set(uniqueId, "asset", "income.daily", 0.0);
    }

    // asset.yml - END

    // settings.yml - BEGIN

    @Override
    public double getSalary(UUID uniqueId, String position) {
        return (double) get(uniqueId, "settings", "salary." + position);
    }

    @Override
    public void setSalary(UUID uniqueId, String position, double amount) {
        set(uniqueId, "settings", "salary." + position, amount);
    }

    // settings.yml - END

    // shop.yml - BEGIN

    @Override
    public UUID createShop(UUID uniqueId, Location location, double price, String creatorName) {
        UUID shopUniqueId = UUID.randomUUID();
        set(uniqueId, "shop", shopUniqueId + ".creator", creatorName);
        set(uniqueId, "shop", shopUniqueId + ".location.world", location.getWorld().getName());
        set(uniqueId, "shop", shopUniqueId + ".location.x", location.getBlockX());
        set(uniqueId, "shop", shopUniqueId + ".location.y", location.getBlockY());
        set(uniqueId, "shop", shopUniqueId + ".location.z", location.getBlockZ());
        set(uniqueId, "shop", shopUniqueId + ".price", price);
        return shopUniqueId;
    }

    @Override
    @Nullable
    public List<Shop> getShops(UUID uniqueId) {
        List<Shop> shops = new ArrayList<>();
        for (String key : com_yamls.get(uniqueId).get("shop").getConfigurationSection(String.valueOf(uniqueId)).getKeys(false)) {
            shops.add(new Shop(uniqueId, UUID.fromString(key)));
        }
        return shops;
    }

    @Override
    @Nullable
    public List<UUID> getShopsUUID(UUID uniqueId) {
        List<UUID> shops = new ArrayList<>();
        for (String key : com_yamls.get(uniqueId).get("shop").getConfigurationSection(String.valueOf(uniqueId)).getKeys(false)) {
            shops.add(UUID.fromString(key));
        }
        return shops;
    }

    @Override
    @Nullable
    public ItemStack getItem(UUID companyUniqueId, UUID shopUniqueId) {
        FileConfiguration yaml = com_yamls.get(companyUniqueId).get("shop");
        ItemStack item = yaml.getItemStack(companyUniqueId + "." + shopUniqueId + ".item");
        if (item ==null) { return null;}
        return item.clone();
    }

    // shop.yml - END

    // Others - BEGIN

    @Override
    public List<UUID> getAllCompanies() {
        return companyList;
    }

    // Others - END



}
