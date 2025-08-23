package p1xel.minecraft.bukkit.utils.storage.backups;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.BuildingManager;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.managers.ShopManager;
import p1xel.minecraft.bukkit.managers.UserManager;
import p1xel.minecraft.bukkit.utils.Logger;
import p1xel.minecraft.bukkit.utils.permissions.Permission;
import p1xel.minecraft.bukkit.utils.storage.Shop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BackupCreator {

    private static final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();
    private static final UserManager userManager = MyCompany.getCacheManager().getUserManager();
    private static final ShopManager shopManager = MyCompany.getCacheManager().getShopManager();
    private static final BuildingManager buildingManager = MyCompany.getCacheManager().getBuildingManager();

    public static void createBackup(UUID uuid) {
        File folder = new File(MyCompany.getInstance().getDataFolder() + "/backup/companies", uuid.toString());
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String[] filesName = new String[]{"info", "settings", "inventory", "shop", "asset"};
        for (String fileName : filesName) {
            File file = new File(MyCompany.getInstance().getDataFolder() + "/backup/companies/" + uuid, fileName + ".yml");
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        updateBackup(uuid);
    }

    public static void updateBackup(UUID uuid) {
        String[] filesName = new String[]{"info", "settings", "inventory", "shop", "asset"};
        for (String fileName : filesName) {
            File file = new File(MyCompany.getInstance().getDataFolder() + "/backup/companies/" + uuid, fileName + ".yml");
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            if (fileName.equals("info")) {
                yaml.set(uuid + ".name", companyManager.getName(uuid));
                yaml.set(uuid + ".id", companyManager.getId(uuid));
                yaml.set(uuid + ".found-date", companyManager.getFoundDate(uuid));
                yaml.set(uuid + ".founder.uuid", companyManager.getFounder(uuid).toString());
                yaml.set(uuid + ".founder.name", companyManager.getFounderName(uuid));
                yaml.set(uuid + ".members.employer", companyManager.getEmployer(uuid).toString());
                for (String position : companyManager.getPositions(uuid)) {
                    if (position.equals("employer")) {
                        continue;
                    }
                    yaml.set(uuid + ".members." + position, companyManager.getEmployeeList(uuid, position).stream().map(UUID::toString).collect(Collectors.toList()));
                }
                Location location = buildingManager.getLocation(uuid);
                yaml.set(uuid + ".location.location.world", location.getWorld().getName());
                yaml.set(uuid + ".location.location.x", location.getX());
                yaml.set(uuid + ".location.location.y", location.getY());
                yaml.set(uuid + ".location.location.z", location.getZ());
                yaml.set(uuid + ".location.location.yaw", location.getYaw());
                yaml.set(uuid + ".location.location.pitch", location.getPitch());
                yaml.set(uuid + ".location.area", buildingManager.getName(uuid));
            }

            if (fileName.equals("settings")) {
                for (String position : companyManager.getPositions(uuid)) {
                    yaml.set(uuid + ".salary." + position, companyManager.getSalary(uuid, position));
                    if (position.equals("employer")) {
                        yaml.set(uuid + ".position.default.employer.label", companyManager.getEmployerLabel(uuid));
                        List<String> list = new ArrayList<>();
                        for (Permission perm: companyManager.getPositionPermission(uuid, position)) {
                            list.add(perm.getName());
                        }
                        yaml.set(uuid + ".position.default.employer.permission", list);
                        continue;
                    }
                    if (position.equals("employee")) {
                        yaml.set(uuid + ".position.default.employee.label", companyManager.getEmployeeLabel(uuid));
                        List<String> list = new ArrayList<>();
                        for (Permission perm: companyManager.getPositionPermission(uuid, position)) {
                            list.add(perm.getName());
                        }
                        yaml.set(uuid + ".position.default.employee.permission", list);
                        continue;
                    }

                    yaml.set(uuid + ".position.custom." + position + ".label", companyManager.getPositionLabel(uuid, position));
                    List<String> list = new ArrayList<>();
                    for (Permission perm: companyManager.getPositionPermission(uuid, position)) {
                        list.add(perm.getName());
                    }
                    yaml.set(uuid + ".position.custom." + position + ".permission", list);

                }
            }

            if (fileName.equals("shop")) {
                for (Shop shop : shopManager.getShops(uuid)) {
                    UUID shopUniqueId = shop.getShopUUID();
                    yaml.set(uuid + "." + shopUniqueId + ".creator", shop.getCreator());
                    Location location = shop.getLocation();
                    yaml.set(uuid + "." + shopUniqueId + ".location.world", location.getWorld().getName());
                    yaml.set(uuid + "." + shopUniqueId + ".location.x", location.getBlockX());
                    yaml.set(uuid + "." + shopUniqueId + ".location.y", location.getBlockY());
                    yaml.set(uuid + "." + shopUniqueId + ".location.z", location.getBlockZ());
                    yaml.set(uuid + "." + shopUniqueId + ".price", shop.getPrice());
                    yaml.set(uuid + "." + shopUniqueId + ".item", shop.getItem());

                }
            }

            if (fileName.equals("asset")) {
                yaml.set(uuid + ".cash", companyManager.getCash(uuid));
                yaml.set(uuid + ".income.total", companyManager.getTotalIncome(uuid));
                yaml.set(uuid + ".income.daily", companyManager.getDailyIncome(uuid));
            }

            try {
                yaml.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        Logger.debug(Level.INFO, "Backup for " + uuid + " has been updated!");
    }

}
