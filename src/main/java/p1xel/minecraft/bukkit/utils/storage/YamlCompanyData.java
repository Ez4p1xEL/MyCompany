package p1xel.minecraft.bukkit.utils.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.Company;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.areas.CompanyArea;
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
import java.util.stream.Collectors;

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

                String[] filesName = new String[]{"info", "settings", "inventory", "shop", "asset", "area"};

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
        String[] filesName = new String[]{"info", "settings", "inventory", "shop", "asset", "area"};
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

//            if (fileName.equalsIgnoreCase("area")) {
//
//                yaml.set(uuid + ".areas", null);
//
//            }

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

    @Override
    public Location getLocation(UUID uniqueId) {
        FileConfiguration yaml = com_yamls.get(uniqueId).get("info");
        if (yaml.getString(uniqueId + ".location.location.world") == null) {
            return null;
        }
        String world = yaml.getString(uniqueId + ".location.location.world");
        double x = yaml.getDouble(uniqueId + ".location.location.x");
        double y = yaml.getDouble(uniqueId + ".location.location.y");
        double z = yaml.getDouble(uniqueId + ".location.location.z");
        float yaw = (float) yaml.getDouble(uniqueId + ".location.location.yaw");
        float pitch = (float) yaml.getDouble(uniqueId + ".location.location.pitch");
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    @Override
    public void setLocation(UUID uniqueId, Location location) {
        File file = com_files.get(uniqueId).get("info");
        FileConfiguration yaml = com_yamls.get(uniqueId).get("info");
        yaml.set(uniqueId + ".location.location.world", location.getWorld().getName());
        yaml.set(uniqueId + ".location.location.x", location.getX());
        yaml.set(uniqueId + ".location.location.y", location.getY());
        yaml.set(uniqueId + ".location.location.z", location.getZ());
        yaml.set(uniqueId + ".location.location.yaw", location.getYaw());
        yaml.set(uniqueId + ".location.location.pitch", location.getPitch());
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        ConfigurationSection section = com_yamls.get(uniqueId).get("shop").getConfigurationSection(String.valueOf(uniqueId));
        if (section != null) {
            for (String key : section.getKeys(false)) {
                shops.add(new Shop(uniqueId, UUID.fromString(key)));
            }
        }
        return shops;
    }

    @Override
    @Nullable
    public List<UUID> getShopsUUID(UUID uniqueId) {
        List<UUID> shops = new ArrayList<>();
        ConfigurationSection section = com_yamls.get(uniqueId).get("shop").getConfigurationSection(String.valueOf(uniqueId));
        if (section != null) {
            for (String key : section.getKeys(false)){
                shops.add(UUID.fromString(key));
            }
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

    // area.yml - BEGIN

    @Override
    public int getLocationPos(UUID uniqueId, String area, String type) {
        FileConfiguration yaml = com_yamls.get(uniqueId).get("area");
        return yaml.getInt(uniqueId + ".areas." + area + ".location." + type);
    }

    @Override
    public void createArea(UUID uniqueId, CompanyArea companyArea, OfflinePlayer creator, Location firstBlock, Location secondBlock) {
        File file = com_files.get(uniqueId).get("area");
        FileConfiguration yaml = com_yamls.get(uniqueId).get("area");
        String area = companyArea.getName();
        yaml.set(uniqueId + ".areas." + area + ".location.world", companyArea.getWorldName());
        yaml.set(uniqueId + ".areas." + area + ".location.minX", companyArea.getMinX());
        yaml.set(uniqueId + ".areas." + area + ".location.maxX", companyArea.getMaxX());
        yaml.set(uniqueId + ".areas." + area + ".location.minY", companyArea.getMinY());
        yaml.set(uniqueId + ".areas." + area + ".location.maxY", companyArea.getMaxY());
        yaml.set(uniqueId + ".areas." + area + ".location.minZ", companyArea.getMinZ());
        yaml.set(uniqueId + ".areas." + area + ".location.maxZ", companyArea.getMaxZ());
        yaml.set(uniqueId + ".areas." + area + ".creator.uuid", creator.getUniqueId().toString());
        yaml.set(uniqueId + ".areas." + area + ".creator.name", creator.getName());
        yaml.set(uniqueId + ".areas." + area + ".info.first-block.world", firstBlock.getWorld().getName());
        yaml.set(uniqueId + ".areas." + area + ".info.first-block.x", firstBlock.getBlockX());
        yaml.set(uniqueId + ".areas." + area + ".info.first-block.y", firstBlock.getBlockY());
        yaml.set(uniqueId + ".areas." + area + ".info.first-block.z", firstBlock.getBlockZ());
        yaml.set(uniqueId + ".areas." + area + ".info.second-block.world", secondBlock.getWorld().getName());
        yaml.set(uniqueId + ".areas." + area + ".info.second-block.x", secondBlock.getBlockX());
        yaml.set(uniqueId + ".areas." + area + ".info.second-block.y", secondBlock.getBlockY());
        yaml.set(uniqueId + ".areas." + area + ".info.second-block.z", secondBlock.getBlockZ());
        yaml.set(uniqueId + ".areas." + area + ".trade.mode", "none");
        yaml.set(uniqueId + ".areas." + area + ".trade.on-market", false);
        yaml.set(uniqueId + ".areas." + area + ".trade.rent.start-time", 0L);
        yaml.set(uniqueId + ".areas." + area + ".trade.rent.end-time", 0L);
        //yaml.set(uniqueId + ".areas." + area + ".trade.sell.available", false);
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteArea(UUID uniqueId, CompanyArea companyArea) {
        File file = com_files.get(uniqueId).get("area");
        FileConfiguration yaml = com_yamls.get(uniqueId).get("area");
        String area = companyArea.getName();
        yaml.set(uniqueId + ".areas." + area, null);
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> getAreas(UUID uniqueId) {
        FileConfiguration yaml = com_yamls.get(uniqueId).get("area");
        try {
            return yaml.getConfigurationSection(uniqueId + ".areas").getKeys(false);
        } catch (NullPointerException exception) {
            return Collections.emptySet();
        }
    }

    @Override
    public void setAccessibleCompanies(UUID uniqueId, String area, List<UUID> companyList) {
        List<String> stringList = companyList.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        File file = com_files.get(uniqueId).get("area");
        FileConfiguration yaml = com_yamls.get(uniqueId).get("area");
        yaml.set(uniqueId + ".areas." + area + ".accessible", stringList);
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UUID> getAccessibleCompanies(UUID uniqueId, String area) {
        FileConfiguration yaml = com_yamls.get(uniqueId).get("area");
        List<UUID> list = new ArrayList<>();
        for (String uuid : yaml.getStringList(uniqueId + ".areas." + area + ".accessible")) {
            list.add(UUID.fromString(uuid));
        }
        return list;
    }

    @Override
    public void setAreaLocation(UUID uniqueId, String area, Location location) {
        File file = com_files.get(uniqueId).get("area");
        FileConfiguration yaml = com_yamls.get(uniqueId).get("area");
        yaml.set(uniqueId + ".areas." + area + ".tp-loc.world", location.getWorld().getName());
        yaml.set(uniqueId + ".areas." + area + ".tp-loc.x", location.getX());
        yaml.set(uniqueId + ".areas." + area + ".tp-loc.y", location.getY());
        yaml.set(uniqueId + ".areas." + area + ".tp-loc.z", location.getZ());
        yaml.set(uniqueId + ".areas." + area + ".tp-loc.yaw", location.getYaw());
        yaml.set(uniqueId + ".areas." + area + ".tp-loc.pitch", location.getPitch());
        try {
            yaml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Location getAreaLocation(UUID uniqueId, String area) {
        FileConfiguration yaml = com_yamls.get(uniqueId).get("area");
        if (yaml.getString(uniqueId + ".areas." + area + ".tp-loc.world") == null) {
            return null;
        }
        String world = yaml.getString(uniqueId + ".areas." + area + ".tp-loc.world");
        double x = yaml.getDouble(uniqueId + ".areas." + area + ".tp-loc.x");
        double y = yaml.getDouble(uniqueId + ".areas." + area + ".tp-loc.y");
        double z = yaml.getDouble(uniqueId + ".areas." + area + ".tp-loc.z");
        float yaw = (float) yaml.getDouble(uniqueId + ".areas." + area + ".tp-loc.yaw");
        float pitch = (float) yaml.getDouble(uniqueId + ".areas." + area + ".tp-loc.pitch");
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

    }

    @Override
    public HashMap<UUID, List<String>> getAreasRented(UUID uniqueId) {
        FileConfiguration yaml = com_yamls.get(uniqueId).get("area");
        HashMap<UUID, List<String>> map = new HashMap<>();
        ConfigurationSection section = yaml.getConfigurationSection(uniqueId + ".rent-area");
        if (section != null) {
            for (String uuid_string : section.getKeys(false)) {
                UUID originalCompanyUniqueId = UUID.fromString(uuid_string);
                List<String> list = new ArrayList<>(section.getConfigurationSection(uuid_string).getKeys(false));
                map.put(originalCompanyUniqueId, list);
            }
        }
        return map;
    }

    // area.yml - END

    // Others - BEGIN

    @Override
    public List<UUID> getAllCompanies() {
        return companyList;
    }

    // Others - END



}
