package p1xel.minecraft.bukkit.managers.buildings;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DefaultBuildingArea extends BuildingArea{

    @Override
    public void init() {

    }

    @Override
    public boolean isInBuilding(UUID employerUniqueId, Block block, String name) {
        return true;
    }

    @Override
    public boolean isEmployerArea(UUID employerUniqueId, Location location) {
        return true;
    }

    @Override
    public @Nullable String getName(Location location) {
        return null;
    }
}
