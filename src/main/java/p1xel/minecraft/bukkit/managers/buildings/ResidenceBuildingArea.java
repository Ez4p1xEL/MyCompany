package p1xel.minecraft.bukkit.managers.buildings;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.UUID;

public class ResidenceBuildingArea extends BuildingArea {

    private final Residence plugin = Residence.getInstance();
    private final ResidenceManager manager = plugin.getResidenceManager();

    @Override
    public void init() {

    }

    @Override
    public boolean isInBuilding(UUID employerUniqueId, Block block, String name) {
        Location loc = block.getLocation();
        ClaimedResidence residence = manager.getByLoc(loc);
        if (residence == null) { return false; }
        UUID ownerUniqueId = residence.getOwnerUUID();
        if (!employerUniqueId.equals(ownerUniqueId)) {
            return false;
        }
        return residence.getResidenceName().equalsIgnoreCase(name);
    }

    @Override
    public boolean isEmployerArea(UUID employerUniqueId, Location location) {
        ClaimedResidence residence = manager.getByLoc(location);
        if (residence == null) { return false; }
        UUID ownerUniqueId = residence.getOwnerUUID();
        return employerUniqueId.equals(ownerUniqueId);
    }

    @Override
    public String getName(Location location) {
        ClaimedResidence residence = manager.getByLoc(location);
        if (residence == null) { return null; }
        return residence.getResidenceName();
    }

}
