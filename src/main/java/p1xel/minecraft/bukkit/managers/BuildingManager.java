package p1xel.minecraft.bukkit.managers;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import p1xel.minecraft.bukkit.managers.buildings.BuildingArea;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.storage.CompanyData;

import javax.annotation.Nullable;
import java.util.UUID;

public class BuildingManager {

    private CompanyData data;
    private final NamespacedKey shopKey = new NamespacedKey("mycompany", "is_company_shop");
    private final NamespacedKey companyKey = new NamespacedKey("mycompany", "company");
    private final NamespacedKey shopUUIDKey = new NamespacedKey("mycompany", "shop_uuid");
    private BuildingArea area;
    public BuildingManager(CompanyData data) {
        this.data = data;
    }

    public void setModule(BuildingArea area) {
        this.area = area;
        area.init();
    }

    public boolean isInBuilding(UUID employerUniqueId, Block block, String name) {
        if (!Config.getBool("company-settings.chestshop-create-in-building")) {
            return true;
        }
        return this.area.isInBuilding(employerUniqueId, block, name);
    }

    public boolean isEmployerArea(UUID employerUniqueId, Location location) {
        return this.area.isEmployerArea(employerUniqueId, location);
    }

    public Location getLocation(UUID companyUniqueId) {
        return this.data.getLocation(companyUniqueId);
    }

    public void setLocation(UUID companyUniqueId, Location location) {
        this.data.setLocation(companyUniqueId, location);
    }

    public String getName(Location location) {
        return this.area.getName(location);
    }

    public void setName(UUID companyUniqueId, String name) {
        this.data.set(companyUniqueId, "info", "location.area", name);
    }

    @Nullable
    public String getName(UUID companyUniqueId) {
        return (String) this.data.get(companyUniqueId, "info", "location.area");
    }



}
