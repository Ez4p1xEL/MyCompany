package p1xel.minecraft.bukkit.managers.buildings;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.AreaManager;
import p1xel.minecraft.bukkit.managers.CacheManager;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.managers.UserManager;

import java.util.UUID;

public class CompanyArea {

    private UUID companyUniqueId;
    private String area;
    private String world;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private Location first;
    private Location second;
    private CacheManager cache = MyCompany.getCacheManager();
    private CompanyManager companyManager = cache.getCompanyManager();
    private UserManager userManager = cache.getUserManager();
    private AreaManager areaManager = cache.getAreaManager();

    public CompanyArea(UUID companyUniqueId, String areaName, String world,int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Location firstBlock, Location secondBlock) {
        this.companyUniqueId = companyUniqueId;
        this.area = areaName;
        this.world = world;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.first = firstBlock;
        this.second = secondBlock;
    }

    public CompanyArea(UUID companyUniqueId, String areaName) {
        this.companyUniqueId = companyUniqueId;
        this.area = areaName;
        world = areaManager.getWorldName(companyUniqueId, area);
        minX = areaManager.getLocationPos(companyUniqueId, area, "minX");
        maxX = areaManager.getLocationPos(companyUniqueId, area, "maxX");
        minY = areaManager.getLocationPos(companyUniqueId, area, "minY");
        maxY = areaManager.getLocationPos(companyUniqueId, area, "maxY");
        minZ = areaManager.getLocationPos(companyUniqueId, area, "minZ");
        maxZ = areaManager.getLocationPos(companyUniqueId, area, "maxZ");
        first = areaManager.getFirstBlockLocation(companyUniqueId, area);
    }

    public boolean isLocInArea(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ &&
                location.getWorld().getName().equals(world);
    }

    public boolean canPlayerAccess(UUID playerUniqueId) {
        UUID targetCompanyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (targetCompanyUniqueId == null) {
            return false;
        }

        if (areaManager.getAccessibleCompanies(companyUniqueId, area).contains(targetCompanyUniqueId)) {
            return true;
        }

        if (!targetCompanyUniqueId.equals(companyUniqueId)) {
            return false;
        }
        return true;

    }

    public boolean canPlayerAccess(OfflinePlayer player) {
        return canPlayerAccess(player.getUniqueId());
    }

    public UUID getCompanyUUID() { return companyUniqueId; }
    public String getName() { return area; }
    public String getWorldName() { return world; }
    public String getCreatorName() { return areaManager.getCreatorName(companyUniqueId, area);}
    public UUID getCreatorUUID() { return areaManager.getCreatorUUID(companyUniqueId, area);}
    public int getMinX() { return minX;}
    public int getMaxX() { return maxX;}
    public int getMinY() { return minY;}
    public int getMaxY() { return maxY;}
    public int getMinZ() { return minZ;}
    public int getMaxZ() { return maxZ;}
    public Location getFirst() { return first; }

}
