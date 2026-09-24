package p1xel.minecraft.bukkit.util.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.area.CompanyArea;
import p1xel.minecraft.bukkit.object.Shop;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.ItemSerializer;
import p1xel.minecraft.bukkit.object.price.PriceGroup;
import p1xel.minecraft.bukkit.util.storage.cidstorage.CIdData;
import p1xel.minecraft.bukkit.util.storage.driver.CompanyStorageDriver;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CompanyData extends AbstractCompanyData {

    private final CompanyStorageDriver driver;
    private final List<UUID> companyList = new ArrayList<>();

    public CompanyData(CompanyStorageDriver driver) {
        this.driver = driver;
    }

    @Override
    public void init() {
        companyList.clear();
        driver.init();
        companyList.addAll(driver.loadAllCompanyEntries());

        for (UUID uniqueId : companyList) {
            int cid = getId(uniqueId);
            MyCompany.getCacheManager().getCompanyManager().getCIds().put(cid, uniqueId);
        }

    }

    @Override
    public List<UUID> getCompaniesUUID() {
        return companyList;
    }

    @Override
    public List<String> getCompaniesName() {
        List<String> nameList = new ArrayList<>();
        for (UUID uniqueId : companyList) {
            String name = getName(uniqueId);
            // File file = new File(MyCompany.getInstance().getDataFolder() + "/companies/" + uniqueId, "info.yml");
            nameList.add(name);
        }
        return nameList;
    }

    @Override
    public Company getCompany(UUID uniqueId) {
        return new Company(uniqueId);
    }

    @Override
    public void set(UUID uniqueId, String type, String key, Object value) {
        driver.set(uniqueId, type, key, value);
    }

    public void save(UUID uniqueId, String type) {
        driver.save(uniqueId, type);
    }

    @Override
    public UUID createCompany(String companyName, UUID playerUniqueId) {
        UUID uuid = UUID.randomUUID();
        driver.createCompanyEntry(uuid);

        // 逐個創建
        //String[] filesName = new String[]{"info", "settings", "inventory", "shop", "asset", "area"};

        // Type: info
        set(uuid, "info", "name", companyName);
        set(uuid, "info", "id", CIdData.getAndUpdateCID());
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Config.getString("company-settings.time-format"));
        String formattedTime = now.format(formatter);
        set(uuid, "info", "found-date", formattedTime);
        set(uuid, "info", "founder.uuid", playerUniqueId.toString());

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUniqueId);
        set(uuid, "info", "founder.name", player.getName());
        set(uuid, "info", "members.employer", playerUniqueId.toString());
        set(uuid, "info", "price-group", PriceGroup.NORMAL.getName());
        // END

        // Type: asset
        set(uuid, "asset", "cash", Config.getDouble("company-funds.default-asset"));
        set(uuid, "asset", "income.total", 0.0);
        set(uuid, "asset", "income.daily", 0.0);
        // END

        // Type: settings
        set(uuid, "settings", "salary.employer", 1000.0);
        set(uuid, "settings", "salary.employee", 500.0);
        set(uuid, "settings", "position.default.employer.label", "Employer");
        set(uuid, "settings", "position.default.employee.label", "Employee");
        set(uuid, "settings", "position.default.employer.permission", Collections.singletonList("all"));
        set(uuid, "settings", "position.default.employee.permission", Config.getStringList("company-settings.employee-default-permission"));
        // END

        // Save
        save(uuid, "info");
        save(uuid, "asset");
        save(uuid, "settings");

        companyList.add(uuid);
        int cid = getId(uuid);
        MyCompany.getCacheManager().getCompanyManager().getCIds().put(cid, uuid);

        MyCompany.getCacheManager().getUserManager().createUser(playerUniqueId);
        MyCompany.getCacheManager().getUserManager().setCompany(playerUniqueId, uuid);
        MyCompany.getCacheManager().getUserManager().setPosition(playerUniqueId, "employer");

        return uuid;

    }

    @Override
    public Object get(UUID uniqueId, String type, String path) {
        return driver.get(uniqueId, type, path);
    }

    public String getString(UUID uniqueId, String type, String path) {
        return driver.getString(uniqueId, type, path);
    }

    public int getInt(UUID uniqueId, String type, String path) {
        return driver.getInt(uniqueId, type, path);
    }

    public double getDouble(UUID uniqueId, String type, String path) {
        return driver.getDouble(uniqueId, type, path);
    }

    public long getLong(UUID uniqueId, String type, String path) {
        return driver.getLong(uniqueId, type, path);
    }

    public boolean getBoolean(UUID uniqueId, String type, String path) {
        return driver.getBoolean(uniqueId, type, path);
    }

    public List<String> getStringList(UUID uniqueId, String type, String path) {
        return driver.getStringList(uniqueId, type, path);
    }

    public float getFloat(UUID uniqueId, String type, String path) {
        return driver.getFloat(uniqueId, type, path);
    }

    public Set<String> getKeys(UUID uniqueId, String type, String path) {
        return driver.getKeys(uniqueId, type, path);
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
        save(companyUniqueId, "info");
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

        driver.deleteCompanyEntry(uniqueId);
        // Remove employees
        for (String position : getPositions(uniqueId)) {
            if (position.equalsIgnoreCase("employer")) {
                continue;
            }

            for (UUID employeeUniqueId : getEmployeeList(uniqueId, position)) {
                MyCompany.getCacheManager().getUserManager().setCompany(employeeUniqueId, null);
                MyCompany.getCacheManager().getUserManager().setPosition(employeeUniqueId, null);
            }
        }

        MyCompany.getCacheManager().getCompanyManager().getCIds().remove(cid);
        companyList.remove(uniqueId);
    }

    @Override
    public void employPlayer(UUID companyUniqueId, UUID playerUniqueId, String position) {
        List<String> list = (List<String>) get(companyUniqueId, "info", "members." + position);
        if (list == null) { list = new ArrayList<>(); }
        list.add(String.valueOf(playerUniqueId));
        set(companyUniqueId, "info", "members." + position, list);
        save(companyUniqueId, "info");
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
        for (String position : getPositions(uniqueId)) {
            if (position.equalsIgnoreCase("employer")) {
                continue;
            }

            List<UUID> employeeList = getEmployeeList(uniqueId,position);
            if (employeeList != null && !employeeList.isEmpty()) {
                amount = amount + employeeList.size();
            }
        }
        return amount;
    }

    @Override
    @Nullable
    public UUID getUUIDFromId(int cid) {
        for (UUID uniqueId : companyList) {
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
        Set<String> customPositions = getKeys(uniqueId, "settings", "position.custom");
        if (customPositions == null) {
            return list;
        }
        list.addAll(customPositions);
        return list;
    }

    @Override
    public Location getLocation(UUID uniqueId) {
        String world = getString(uniqueId, "info", "location.location.world");
        if (world == null) { return null; }

        double x = getDouble(uniqueId, "info", "location.location.x");
        double y = getDouble(uniqueId, "info","location.location.y");
        double z = getDouble(uniqueId, "info","location.location.z");
        float yaw = getFloat(uniqueId , "info","location.location.yaw");
        float pitch = getFloat(uniqueId, "info","location.location.pitch");
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    @Override
    public void setLocation(UUID uniqueId, Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        set(uniqueId, "info", "location.location.world", location.getWorld().getName());
        set(uniqueId, "info", "location.location.x", location.getX());
        set(uniqueId, "info", "location.location.y", location.getY());
        set(uniqueId, "info", "location.location.z", location.getZ());
        set(uniqueId, "info", "location.location.yaw", (double) location.getYaw());
        set(uniqueId, "info", "location.location.pitch", (double) location.getPitch());
        save(uniqueId, "info");
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
        save(uniqueId, "asset");
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
        save(uniqueId, "asset");
    }

    @Override
    public void takeMoney(UUID uniqueId, double amount) {
        double cash = getCash(uniqueId);
        setCash(uniqueId, cash-amount);
    }

    @Override
    public void resetDailyIncome(UUID uniqueId) {
        set(uniqueId, "asset", "income.daily", 0.0);
        save(uniqueId, "asset");
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
        save(uniqueId, "settings");
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
        save(uniqueId, "shop");
        return shopUniqueId;
    }

    @Override
    @Nullable
    public List<Shop> getShops(UUID uniqueId) {
        List<Shop> shops = new ArrayList<>();
        Set<String> keys = getKeys(uniqueId, "shop", null);
        for (String key : keys) {
            shops.add(new Shop(uniqueId, UUID.fromString(key)));
        }
        return shops;
    }

    @Override
    @Nullable
    public List<UUID> getShopsUUID(UUID uniqueId) {
        List<UUID> shops = new ArrayList<>();
        Set<String> keys = getKeys(uniqueId, "shop", null);
            for (String key : keys){
                shops.add(UUID.fromString(key));
            }
        return shops;
    }

    @Override
    @Nullable
    public ItemStack getItem(UUID companyUniqueId, UUID shopUniqueId) {
        String itemInString = getString(companyUniqueId, "shop", shopUniqueId + ".item");
        if (itemInString ==null) { return null;}
        ItemStack item = ItemSerializer.fromBase64(itemInString);
        if (item == null) { return null; }
        return item.clone();
    }

    // shop.yml - END

    // area.yml - BEGIN

    @Override
    public int getLocationPos(UUID uniqueId, String area, String type) {
        return getInt(uniqueId, "area", "areas." + area + ".location." + type);
    }

    @Override
    public void createArea(UUID uniqueId, CompanyArea companyArea, OfflinePlayer creator, Location firstBlock, Location secondBlock) {
        String area = companyArea.getName();
        set(uniqueId, "area", "areas." + area + ".location.world", companyArea.getWorldName());
        set(uniqueId, "area", "areas." + area + ".location.minX", companyArea.getMinX());
        set(uniqueId, "area", "areas." + area + ".location.maxX", companyArea.getMaxX());
        set(uniqueId, "area", "areas." + area + ".location.minY", companyArea.getMinY());
        set(uniqueId, "area", "areas." + area + ".location.maxY", companyArea.getMaxY());
        set(uniqueId, "area", "areas." + area + ".location.minZ", companyArea.getMinZ());
        set(uniqueId, "area", "areas." + area + ".location.maxZ", companyArea.getMaxZ());
        set(uniqueId, "area", "areas." + area + ".creator.uuid", creator.getUniqueId().toString());
        set(uniqueId, "area", "areas." + area + ".creator.name", creator.getName());
        set(uniqueId, "area", "areas." + area + ".info.first-block.world", firstBlock.getWorld().getName());
        set(uniqueId, "area", "areas." + area + ".info.first-block.x", firstBlock.getBlockX());
        set(uniqueId, "area", "areas." + area + ".info.first-block.y", firstBlock.getBlockY());
        set(uniqueId, "area", "areas." + area + ".info.first-block.z", firstBlock.getBlockZ());
        set(uniqueId, "area", "areas." + area + ".info.second-block.world", secondBlock.getWorld().getName());
        set(uniqueId, "area", "areas." + area + ".info.second-block.x", secondBlock.getBlockX());
        set(uniqueId, "area", "areas." + area + ".info.second-block.y", secondBlock.getBlockY());
        set(uniqueId, "area", "areas." + area + ".info.second-block.z", secondBlock.getBlockZ());
        set(uniqueId, "area", "areas." + area + ".trade.mode", "none");
        set(uniqueId, "area", "areas." + area + ".trade.on-market", false);
        set(uniqueId, "area", "areas." + area + ".trade.rent.start-time", 0L);
        set(uniqueId, "area", "areas." + area + ".trade.rent.end-time", 0L);
        //set(uniqueId, "area", "areas." + area + ".trade.sell.available", false);

        save(uniqueId, "area");
    }

    @Override
    public void deleteArea(UUID uniqueId, CompanyArea companyArea) {
        String area = companyArea.getName();
        set(uniqueId, "area", "areas." + area, null);
        save(uniqueId, "area");
    }

    @Override
    public Set<String> getAreas(UUID uniqueId) {
        try {
            return getKeys(uniqueId, "area", "areas");
        } catch (NullPointerException exception) {
            return Collections.emptySet();
        }
    }

    @Override
    public void setAccessibleCompanies(UUID uniqueId, String area, List<UUID> companyList) {
        List<String> stringList = companyList.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        set(uniqueId, "area", "areas." + area + ".accessible", stringList);
        save(uniqueId, "area");
    }

    @Override
    public List<UUID> getAccessibleCompanies(UUID uniqueId, String area) {
        List<UUID> list = new ArrayList<>();
        for (String uuid : getStringList(uniqueId, "area", "areas." + area + ".accessible")) {
            list.add(UUID.fromString(uuid));
        }
        return list;
    }

    @Override
    public void setAreaLocation(UUID uniqueId, String area, Location location) {
        set(uniqueId, "area", "areas." + area + ".tp-loc.world", location.getWorld().getName());
        set(uniqueId, "area", "areas." + area + ".tp-loc.x", location.getX());
        set(uniqueId, "area", "areas." + area + ".tp-loc.y", location.getY());
        set(uniqueId, "area", "areas." + area + ".tp-loc.z", location.getZ());
        set(uniqueId, "area", "areas." + area + ".tp-loc.yaw", location.getYaw());
        set(uniqueId, "area", "areas." + area + ".tp-loc.pitch", location.getPitch());
        save(uniqueId, "area");
    }

    @Override
    public Location getAreaLocation(UUID uniqueId, String area) {
        String world = getString(uniqueId, "area", "areas." + area + ".tp-loc.world");
        if (world == null) {
            return null;
        }
        double x = getDouble(uniqueId, "area", "areas." + area + ".tp-loc.x");
        double y = getDouble(uniqueId,"area", "areas." + area + ".tp-loc.y");
        double z = getDouble(uniqueId, "area", "areas." + area + ".tp-loc.z");
        float yaw = getFloat(uniqueId, "area","areas." + area + ".tp-loc.yaw");
        float pitch = getFloat(uniqueId, "area", "areas." + area + ".tp-loc.pitch");
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

    }

    @Override
    public HashMap<UUID, List<String>> getAreasRented(UUID uniqueId) {
        HashMap<UUID, List<String>> map = new HashMap<>();
        Set<String> keys = getKeys(uniqueId, "area", "rent-area");
        if (keys != null) {
            for (String uuid_string : keys) {
                UUID originalCompanyUniqueId = UUID.fromString(uuid_string);
                List<String> list = new ArrayList<>(getKeys(uniqueId, "area", "rent-area." + uuid_string));
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
