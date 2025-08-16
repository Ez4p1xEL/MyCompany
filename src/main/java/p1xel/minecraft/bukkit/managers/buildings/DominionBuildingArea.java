package p1xel.minecraft.bukkit.managers.buildings;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import org.bukkit.Location;
import org.bukkit.block.Block;
import cn.lunadeer.dominion.api.DominionAPI;
import p1xel.minecraft.bukkit.utils.Logger;

import java.util.UUID;
import java.util.logging.Level;

public class DominionBuildingArea extends BuildingArea {

    private DominionAPI plugin;

    @Override
    public void init() {
        try {
            plugin = DominionAPI.getInstance();
        } catch (IllegalStateException exception) {
            Logger.log(Level.INFO, "Dominion is not enabled, please confirm the complete installation.");
            throw exception;
        }
    }

    @Override
    public boolean isInBuilding(UUID employerUniqueId, Block block, String name) {
        Location loc = block.getLocation();
        DominionDTO dom = plugin.getDominion(loc);
        if (dom == null) { return false; }
        UUID ownerUniqueId = dom.getOwner();
        if (!employerUniqueId.equals(ownerUniqueId)) {
            return false;
        }
        return dom.getName().equalsIgnoreCase(name);
    }

    @Override
    public boolean isEmployerArea(UUID employerUniqueId, Location location) {
        DominionDTO dom = plugin.getDominion(location);
        if (dom == null) { return false; }
        UUID ownerUniqueId = dom.getOwner();
        return employerUniqueId.equals(ownerUniqueId);
    }

    @Override
    public String getName(Location location) {
        DominionDTO dom = plugin.getDominion(location);
        if (dom == null) { return null; }
        return dom.getName();
    }

}
