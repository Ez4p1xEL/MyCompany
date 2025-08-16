package p1xel.minecraft.bukkit.managers.buildings;

import org.bukkit.Location;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class BuildingArea {

    public abstract void init();

    public abstract boolean isInBuilding(UUID employerUniqueId, Block block, String name);

    //public abstract boolean isInArea(Player player);

    public abstract boolean isEmployerArea(UUID employerUniqueId, Location location);

    @Nullable
    public abstract String getName(Location location);


}
