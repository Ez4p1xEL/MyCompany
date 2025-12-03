package p1xel.minecraft.bukkit.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import p1xel.minecraft.bukkit.managers.areas.AreaTradeMarket;
import p1xel.minecraft.bukkit.managers.areas.CompanyArea;
import p1xel.minecraft.bukkit.utils.storage.CompanyData;

import java.util.*;

public class AreaManager {

    private CompanyData data;
    private HashMap<String, List<CompanyArea>> gridIndex = new HashMap<>();
    private static final int GRID_SIZE = 32;
    private AreaTradeMarket atm;
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

    public void createArea(UUID uniqueId, CompanyArea companyArea, OfflinePlayer creator, Location firstBlock, Location secondBlock) {
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

        atm = new AreaTradeMarket();
        atm.init();
    }

    public AreaTradeMarket getMarketManager() {
        return atm;
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

    public List<CompanyArea> getOverlappingAreas(CompanyArea newArea) {
        List<CompanyArea> overlaps = new ArrayList<>();

        int minGridX = newArea.getMinX() / GRID_SIZE;
        int maxGridX = newArea.getMaxX() / GRID_SIZE;
        int minGridZ = newArea.getMinZ() / GRID_SIZE;
        int maxGridZ = newArea.getMaxZ() / GRID_SIZE;

        Set<CompanyArea> checked = new HashSet<>();

        for (int gx = minGridX; gx <= maxGridX; gx++) {
            for (int gz = minGridZ; gz <= maxGridZ; gz++) {
                String key = newArea.getWorldName() + ":" + gx + ":" + gz;
                List<CompanyArea> candidates = gridIndex.getOrDefault(key, Collections.emptyList());

                for (CompanyArea existing : candidates) {
                    if (checked.add(existing)) { // Avoid repeat
                        if (isOverlap(newArea, existing)) {
                            overlaps.add(existing);
                        }
                    }
                }
            }
        }
        return overlaps;
    }

    private boolean isOverlap(CompanyArea area1, CompanyArea area2) {
        if (!area1.getWorldName().equals(area2.getWorldName())) {return false; }

        boolean overlapX = area1.getMinX() <= area2.getMaxX() && area1.getMaxX() >= area2.getMinX();
        boolean overlapY = area1.getMinY() <= area2.getMaxY() && area1.getMaxY() >= area2.getMinY();
        boolean overlapZ = area1.getMinZ() <= area2.getMaxZ() && area1.getMaxZ() >= area2.getMinZ();

        return overlapX && overlapY && overlapZ;
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
        String world = (String) data.get(uniqueId, "area", "areas." + area + ".info.first-block.world");
        int x = (int) data.get(uniqueId, "area", "areas." + area + ".info.first-block.x");
        int y = (int) data.get(uniqueId, "area", "areas." + area + ".info.first-block.y");
        int z = (int) data.get(uniqueId, "area", "areas." + area + ".info.first-block.z");
        return Bukkit.getWorld(world).getBlockAt(x,y,z).getLocation();
    }

    public Location getSecondBlockLocation(UUID uniqueId, String area) {
        String world = (String) data.get(uniqueId, "area", "areas." + area + ".info.second-block.world");
        int x = (int) data.get(uniqueId, "area", "areas." + area + ".info.second-block.x");
        int y = (int) data.get(uniqueId, "area", "areas." + area + ".info.second-block.y");
        int z = (int) data.get(uniqueId, "area", "areas." + area + ".info.second-block.z");
        return Bukkit.getWorld(world).getBlockAt(x,y,z).getLocation();
    }

    public HashMap<UUID, List<String>> getAreasRented(UUID uniqueId) {
        return this.data.getAreasRented(uniqueId);
    }

    public Long getRentEndTime(UUID uniqueId, UUID areaCompanyUniqueId, String area) {
        return Long.parseLong(Objects.toString(data.get(uniqueId, "area", "rent-area." + areaCompanyUniqueId + ".rent#"+area + ".end-time")));
    }

    public boolean isAreaRented(UUID uniqueId, String area) {
        return Long.parseLong(Objects.toString(data.get(uniqueId, "area", "areas." + area + ".trade.rent.end-time"))) != 0L;
    }

    public UUID getAreaRenter(UUID uniqueId, String area) {
        return UUID.fromString((String) data.get(uniqueId, "area", "areas." + area + ".trade.rent.renter"));
    }


}
