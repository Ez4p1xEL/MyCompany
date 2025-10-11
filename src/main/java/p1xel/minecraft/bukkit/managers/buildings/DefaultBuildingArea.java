package p1xel.minecraft.bukkit.managers.buildings;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.AreaManager;
import p1xel.minecraft.bukkit.managers.UserManager;

import java.util.UUID;

public class DefaultBuildingArea extends BuildingArea{

    private final AreaManager areaManager = MyCompany.getCacheManager().getAreaManager();
    private final UserManager userManager = MyCompany.getCacheManager().getUserManager();
    @Override
    public void init() {

    }

    @Override
    public boolean isInBuilding(UUID employerUniqueId, Block block, String name) {
        CompanyArea area = areaManager.getAreaByLoc(block.getLocation());
        if (area == null) {
            return false;
        }

        return area.canPlayerAccess(employerUniqueId);
    }

    @Override
    public boolean isEmployerArea(UUID employerUniqueId, Location location) {
        CompanyArea area = areaManager.getAreaByLoc(location);
        if (area == null) {
            return false;
        }

        return area.canPlayerAccess(employerUniqueId);
    }

    @Override
    public @Nullable String getName(Location location) {
        return null;
    }
}
