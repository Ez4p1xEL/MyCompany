package p1xel.minecraft.bukkit.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.managers.buildings.CompanyArea;
import p1xel.minecraft.bukkit.utils.storage.CompanyData;

import java.util.*;

public class AreaManager {

    private CompanyData data;
    private HashMap<String, List<CompanyArea>> gridIndex = new HashMap<>();
    private static final int GRID_SIZE = 32;
    public AreaManager(CompanyData data) {
        this.data = data;
    }

    // type can only be minX-Z and maxX-Z
    public int getLocationPos(UUID uniqueId, String area, String type) {
        return this.data.getLocationPos(uniqueId, area, type);
    }

    public String getWorldName(UUID uniqueId, String area) {
        return (String) this.data.get(uniqueId, "area", "areas." + area + ".location.world");
    }

    public String getCreatorName(UUID uniqueId, String area) {
        return (String) this.data.get(uniqueId, "area", "areas." + area + ".creator.name");
    }

    public UUID getCreatorUUID(UUID uniqueId, String area) {
        return UUID.fromString((String) this.data.get(uniqueId, "area", "areas." + area + ".creator.uuid"));
    }

    public void createArea(UUID uniqueId, CompanyArea companyArea, Player creator, Location firstBlock, Location secondBlock) {
        this.data.createArea(uniqueId, companyArea, creator, firstBlock, secondBlock);
        buildGridIndex(companyArea);
    }

    public Set<String> getAreas(UUID uniqueId) {
        return this.data.getAreas(uniqueId);
    }

    public void buildGridIndex(CompanyArea area) {

        int minGridX = area.getMinX() / GRID_SIZE;
        int maxGridX = area.getMaxX() / GRID_SIZE;
        int minGridZ = area.getMinZ() / GRID_SIZE;
        int maxGridZ = area.getMaxZ() / GRID_SIZE;

        for (int gx = minGridX; gx <= maxGridX; gx++) {
            for (int gz = minGridZ; gz <= maxGridZ; gz++) {
                String key = area.getWorldName() + ":" + gx + ":" + gz;
                gridIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(area);
            }
        }
    }

    public void init() {
        List<CompanyArea> areas = new ArrayList<>(Collections.emptyList());
        for (UUID companyUniqueId : data.getAllCompanies()) {
            for (String area : getAreas(companyUniqueId)) {
                areas.add(new CompanyArea(companyUniqueId, area));
            }
        }

        for (CompanyArea area : areas) {
            buildGridIndex(area);
        }
    }

    public String getGridKey(Location loc) {
        int gridX = loc.getBlockX() / GRID_SIZE;
        int gridZ = loc.getBlockZ() / GRID_SIZE;
        return loc.getWorld().getName() + ":" + gridX + ":" + gridZ;
    }

    public CompanyArea getAreaByLoc(Location loc) {
        String key = getGridKey(loc);
        List<CompanyArea> candidates = gridIndex.getOrDefault(key, Collections.emptyList());

        for (CompanyArea area : candidates) {
            if (area.isLocInArea(loc)) {
                return area;
            }
        }
        return null;
    }

    public void deleteArea(UUID uniqueId, CompanyArea area) {
        this.data.deleteArea(uniqueId, area);
        deleteCompanyCache(area);
    }

    public void deleteCompanyCache(CompanyArea area) {
        //Location loc = area.getFirst();
        //World world = Bukkit.getWorld(area.getWorldName());
        //if (world == null) { return; }
        //String key = getGridKey(block.getLocation());
        for (Map.Entry<String, List<CompanyArea>> entry : gridIndex.entrySet()) {
            String k = entry.getKey();
            List<CompanyArea> v = entry.getValue().stream().toList();
            for (CompanyArea a : v) {
                if (a.getName().equalsIgnoreCase(area.getName())) {
                    gridIndex.get(k).remove(a);
                }
            }
        }

    }

    public void clearCompanyCache(UUID companyUniqueId) {
        for (String areaName : getAreas(companyUniqueId)) {

            CompanyArea area = new CompanyArea(companyUniqueId, areaName);
            deleteCompanyCache(area);

        }
    }

    public void setAccessibleCompanies(UUID uniqueId, String area, List<UUID> companyList) {
        this.data.setAccessibleCompanies(uniqueId, area, companyList);
    }

    public List<UUID> getAccessibleCompanies(UUID uniqueId, String area) {
        return this.data.getAccessibleCompanies(uniqueId, area);
    }

    public boolean rentArea(UUID ownerCompanyUniqueId, String area, UUID targetCompanyUniqueId) {

        List<UUID> accessibleCompanies = getAccessibleCompanies(ownerCompanyUniqueId, area);
        if (accessibleCompanies.contains(targetCompanyUniqueId)) {
            return false;
        }

        accessibleCompanies.add(targetCompanyUniqueId);
        setAccessibleCompanies(ownerCompanyUniqueId, area, accessibleCompanies);
        return true;
    }

    public boolean rentArea(UUID ownerCompanyUniqueId, CompanyArea area, UUID targetCompanyUniqueId) {
        return rentArea(ownerCompanyUniqueId, area.getName(), targetCompanyUniqueId);
    }

    public void setLocation(UUID uniqueId, String area, Location location) {
        this.data.setAreaLocation(uniqueId, area, location);
    }

    public Location getLocation(UUID uniqueId, String area) {
        return this.data.getAreaLocation(uniqueId, area);
    }

    public Location getFirstBlockLocation(UUID uniqueId, String area) {
        String world = (String) data.get(uniqueId, "area", ".areas." + area + ".info.first-block.world");
        int x = (int) data.get(uniqueId, "area", ".areas." + area + ".info.first-block.x");
        int y = (int) data.get(uniqueId, "area", ".areas." + area + ".info.first-block.y");
        int z = (int) data.get(uniqueId, "area", ".areas." + area + ".info.first-block.z");
        return Bukkit.getWorld(world).getBlockAt(x,y,z).getLocation();
    }


}
