package p1xel.minecraft.bukkit.api;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.*;
import p1xel.minecraft.bukkit.managers.areas.AreaSelectionMode;
import p1xel.minecraft.bukkit.managers.buildings.CompanyArea;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.Logger;
import p1xel.minecraft.bukkit.utils.permissions.Permission;
import p1xel.minecraft.bukkit.utils.storage.EmployeeOrders;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PersonalAPI {

    private Player player;
    private UUID playerUniqueId;
    private UUID companyUniqueId;
    private final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();
    private final UserManager userManager = MyCompany.getCacheManager().getUserManager();
    private final ShopManager shopManager = MyCompany.getCacheManager().getShopManager();
    private final BuildingManager buildingManager = MyCompany.getCacheManager().getBuildingManager();
    private final AreaManager areaManager = MyCompany.getCacheManager().getAreaManager();

    public PersonalAPI(UUID playerUniqueId) {
        this.playerUniqueId = playerUniqueId;
        this.player = Bukkit.getPlayer(playerUniqueId);
        this.companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
    }

    public PersonalAPI(Player player) {
        this.player = player;
        if (player != null) {
            this.playerUniqueId = player.getUniqueId();
            this.companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        }
    }

    // For admin
    public PersonalAPI(Player player, UUID companyUniqueId) {
        this.player = player;
        this.companyUniqueId = companyUniqueId;
        if (player != null) {
            this.playerUniqueId = player.getUniqueId();
        }
    }

    public boolean foundCompany(String name) {
        if (!player.hasPermission("mycompany.commands.found")) {
            player.sendMessage(Locale.getMessage("no-perm"));
            return false;
        }

        if (companyUniqueId != null) {
            player.sendMessage(Locale.getMessage("has-company"));
            return false;
        }

        int length = name.length();
        int max_length = Config.getInt("company-settings.max-length");

        if (length > max_length) {
            player.sendMessage(Locale.getMessage("out-of-length").replaceAll("%length%", String.valueOf(max_length)));
            return false;
        }

        if (MyCompany.getCacheManager().getCompanyManager().getCompaniesName().contains(name)) {
            player.sendMessage(Locale.getMessage("company-exist").replaceAll("%company%", name));
            return false;
        }

        double money = Config.getDouble("company-settings.founding-cost.money");
        if (MyCompany.getEconomy().getBalance(player) < money) {
            player.sendMessage(Locale.getMessage("not-enough-money").replaceAll("%money%", String.valueOf(money)));
            return false;
        }

        String labelled = name.replaceAll("_", " ");

        MyCompany.getCacheManager().getCompanyManager().createCompany(labelled, playerUniqueId);
        MyCompany.getEconomy().withdrawPlayer(player, money);
        player.sendMessage(Locale.getMessage("found-success").replaceAll("%company%", labelled));

        // Broadcast
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Locale.getMessage("broadcast.company-found").replaceAll("%company%", labelled).replaceAll("%player%", player.getName()));
            p.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 3f, 3f);
        }

        return true;
    }

    public boolean setPositionLabel(String position, String label) {

        if (!userManager.getPosition(playerUniqueId).equalsIgnoreCase("employer")) {
            if (position.equalsIgnoreCase("employer")) {
                player.sendMessage(Locale.getMessage("setlabel-employer-only"));
                return true;
            }
        }

        if (!companyManager.getPositions(companyUniqueId).contains(position)) {
            player.sendMessage(Locale.getMessage("position-not-existed").replaceAll("%position%", position));
            return true;
        }

        String labelled = label.replaceAll("_", " ");


        if (position.equalsIgnoreCase("employer")) {
            companyManager.setEmployerLabel(companyUniqueId, labelled);
        } else if (position.equalsIgnoreCase("employee")) {
            companyManager.setEmployeeLabel(companyUniqueId, labelled);
        } else {
            companyManager.setPositionLabel(companyUniqueId, position, labelled);
        }

        player.sendMessage(Locale.getMessage("position-setlabel").replaceAll("%position%", position).replaceAll("%label%", labelled));
        return true;
    }

    public boolean setEmployeePosition(UUID targetUniqueId, String name, String position) {

        // args[2] = player name, args[3] = position id

        OfflinePlayer off_target = Bukkit.getPlayer(targetUniqueId);
        if (!off_target.hasPlayedBefore() || !userManager.isUserExist(targetUniqueId)) {
            player.sendMessage(Locale.getMessage("player-not-exist").replaceAll("%player%", name));
            return true;
        }

        if (targetUniqueId.equals(playerUniqueId)) {
            player.sendMessage(Locale.getMessage("cant-do-to-self"));
            return true;
        }

        if (!userManager.getCompanyUUID(targetUniqueId).equals(companyUniqueId)) {
            player.sendMessage(Locale.getMessage("not-your-employee").replaceAll("%player%", name));
            return true;
        }

        if (position.equalsIgnoreCase("employer")) {
            player.sendMessage(Locale.getMessage("cant-set-to-employer"));
            return true;
        }

        if (!companyManager.getPositions(companyUniqueId).contains(position)) {
            player.sendMessage(Locale.getMessage("position-not-existed").replaceAll("%position%", position));
            return true;
        }

        if (companyManager.getEmployeeList(companyUniqueId, position).contains(targetUniqueId)) {
            player.sendMessage(Locale.getMessage("player-position-conflict").replaceAll("%player%", position).replaceAll("%label%", companyManager.getPositionLabel(companyUniqueId, position)));
            return true;
        }

        if (!userManager.getPosition(playerUniqueId).equalsIgnoreCase("employer")) {
            if (userManager.hasPermission(targetUniqueId, Permission.SET_POSITION)) {
                player.sendMessage(Locale.getMessage("cant-set-same-level"));
                return true;
            }
        }

        companyManager.setEmployeePosition(companyUniqueId, targetUniqueId, position);
        player.sendMessage(Locale.getMessage("position-set").replaceAll("%player%", name).replaceAll("%position%", position).replaceAll("%label%", companyManager.getPositionLabel(companyUniqueId, position)));
        if (off_target.isOnline()) {
            Player target = (Player) off_target;
            target.sendMessage(Locale.getMessage("position-set-received").replaceAll("%company%", companyManager.getName(companyUniqueId)).replaceAll("%label%", companyManager.getPositionLabel(companyUniqueId, position)));
            player.playSound(player, Sound.ENTITY_WITHER_HURT, 3f, 3f);
        }
        return true;
    }

    public boolean addPosition(String position, String label) {

        List<String> positions = companyManager.getPositions(companyUniqueId);
        if (positions.contains(position)) {
            player.sendMessage(Locale.getMessage("position-already-existed").replaceAll("%position%", position));
            return true;
        }

        if (positions.size() >= Config.getInt("company-settings.maximum-position") +2) {
            player.sendMessage(Locale.getMessage("position-reach-maximum"));
            return true;
        }

//                    if (args[2].equalsIgnoreCase("employer") || args[2].equalsIgnoreCase("employee")) {
//                        sender.sendMessage(Locale.getMessage("incorrect-position-id"));
//                        return true;
//                    }

        String LABEL = position;
        if (label != null) {
            LABEL = label;
        }

        companyManager.addPosition(companyUniqueId, position);
        companyManager.setPositionLabel(companyUniqueId, position, LABEL);
        player.sendMessage(Locale.getMessage("position-add").replaceAll("%position%", position).replaceAll("%label%", LABEL));
        return true;
    }

    public boolean removePosition(String position) {

        if (!companyManager.getPositions(companyUniqueId).contains(position)) {
            player.sendMessage(Locale.getMessage("position-not-existed").replaceAll("%position%", position));
            return true;
        }

        if (position.equalsIgnoreCase("employer") || position.equalsIgnoreCase("employee")) {
            player.sendMessage(Locale.getMessage("incorrect-position-id"));
            return true;
        }

        companyManager.removePosition(companyUniqueId, position);
        player.sendMessage(Locale.getMessage("position-remove").replaceAll("%position%", position));
        return true;
    }

    public boolean teleportToCompany() {
        Location location = buildingManager.getLocation(companyUniqueId);
        teleportToCompany(location);
        return true;
    }

    public boolean teleportToCompany(Location location) {
        if (location == null) {
            player.sendMessage(Locale.getMessage("location-not-exist"));
            return true;
        }

        player.teleport(location);
        player.sendMessage(Locale.getMessage("tp-success"));
        return true;
    }

    public boolean resignFromCompany() {
        companyManager.dismissEmployee(companyUniqueId, playerUniqueId);
        EmployeeOrders.removeAllCacheOrders(playerUniqueId);
        String companyName = companyManager.getName(companyUniqueId);
        player.sendMessage(Locale.getMessage("resign-success").replaceAll("%company%", companyName));

        UUID employerUniqueId = MyCompany.getCacheManager().getCompanyManager().getEmployer(companyUniqueId);
        Player employer = Bukkit.getPlayer(employerUniqueId);
        if (employer != null) {
            employer.sendMessage(Locale.getMessage("resigned").replaceAll("%player%", player.getName()));
            employer.playSound(employer, Sound.ENTITY_VILLAGER_NO, 3f, 3f);
        }
        return true;
    }

    public boolean setSalary(String position, Object amount) {

        if (!companyManager.getPositions(companyUniqueId).contains(position)) {
            player.sendMessage(Locale.getMessage("position-not-existed").replaceAll("%position%", position));
            return true;
        }

        int salary;
        try {
            salary = Integer.parseInt((String) amount);
        } catch (NumberFormatException e) {
            salary = -1;
        }

        if (salary < 0) {
            player.sendMessage(Locale.getMessage("salary-invalid"));
            return true;
        }

        companyManager.setSalary(companyUniqueId, position, salary);
        player.sendMessage(Locale.getMessage("salary-set").replaceAll("%position%", position).replaceAll("%salary%", String.valueOf(salary)));
        return true;
    }

    public boolean createArea(String name) {
        Location firstBlock = AreaSelectionMode.getFBMap().get(playerUniqueId);
        Location secondBlock = AreaSelectionMode.getSBMap().get(playerUniqueId);
        if (firstBlock == null || secondBlock == null) {
            player.sendMessage(Locale.getMessage("selection-missing"));
            return true;
        }

        if (areaManager.getAreas(companyUniqueId).contains(name)) {
            player.sendMessage(Locale.getMessage("area-name-existed").replaceAll("%name%", name));
            return true;
        }

        String world = firstBlock.getWorld().getName();
        int minX = Math.min(firstBlock.getBlockX(), secondBlock.getBlockX());
        int maxX = Math.max(firstBlock.getBlockX(), secondBlock.getBlockX());
        int minY = Math.min(firstBlock.getBlockY(), secondBlock.getBlockY());
        int maxY = Math.max(firstBlock.getBlockY(), secondBlock.getBlockY());
        int minZ = Math.min(firstBlock.getBlockZ(), secondBlock.getBlockZ());
        int maxZ = Math.max(firstBlock.getBlockZ(), secondBlock.getBlockZ());

        if ((maxX-minX) < Config.getInt("company-area.area.size.minX") || (maxX-minX) > Config.getInt("company-area.area.size.maxX")) {
            // send Message that the size should between the minimum and the maximum
            player.sendMessage(Locale.getMessage("not-in-x-range").replaceAll("%min%", Config.getString("company-area.area.size.minX"))
                    .replaceAll("%max%", Config.getString("company-area.area.size.maxX")));
            return true;
        }

        if ((maxY-minY) < Config.getInt("company-area.area.size.minY") || (maxY-minY) > Config.getInt("company-area.area.size.maxY")) {
            // send Message
            player.sendMessage(Locale.getMessage("not-in-y-range").replaceAll("%min%", Config.getString("company-area.area.size.minY"))
                    .replaceAll("%max%", Config.getString("company-area.area.size.maxY")));
            return true;
        }

        if ((maxZ-minZ) < Config.getInt("company-area.area.size.minZ") || (maxZ-minZ) > Config.getInt("company-area.area.size.maxZ")) {
            // send Message
            player.sendMessage(Locale.getMessage("not-in-z-range").replaceAll("%min%", Config.getString("company-area.area.size.minZ"))
                    .replaceAll("%max%", Config.getString("company-area.area.size.maxZ")));
            return true;
        }

        // Create area here
        CompanyArea companyArea = new CompanyArea(companyUniqueId, name, world, minX, maxX, minY, maxY, minZ, maxZ, firstBlock, secondBlock);
        areaManager.createArea(companyUniqueId, companyArea, player, firstBlock, secondBlock);
        player.sendMessage(Locale.getMessage("area-created").replaceAll("%name%", name));
        return true;

    }

    public boolean getSelectionTool() {
        ItemStack tool = AreaSelectionMode.getToolItem();
        if (tool == null) {
            Logger.log(Level.WARNING, Locale.getMessage("tool-item-null"));
            return true;
        }

        player.getInventory().addItem(tool);
        player.sendMessage(Locale.getMessage("givetool-target-success"));
        return true;
    }

    public boolean getAreaInfo() {

        Location location = player.getLocation();
        CompanyArea area = areaManager.getAreaByLoc(location);
        if (area == null) {
            player.sendMessage(Locale.getMessage("no-area-in-location"));
            return true;
        }

        UUID cuuid = area.getCompanyUUID();
        for (String message : Locale.yaml.getStringList("info.area")) {

            message = ChatColor.translateAlternateColorCodes('&', message);
            message = message.replaceAll("%company%", companyManager.getName(cuuid));
            message = message.replaceAll("%cid%", String.valueOf(companyManager.getId(cuuid)));
            message = message.replaceAll("%area%", area.getName());
            message = message.replaceAll("%creator%", area.getCreatorName());

            player.sendMessage(message);
        }
        return true;

    }

    public boolean deleteArea(String name) {
        if (!areaManager.getAreas(companyUniqueId).contains(name)) {
            player.sendMessage(Locale.getMessage("area-name-not-existed").replaceAll("%name%", name));
            return true;
        }

        CompanyArea area = new CompanyArea(companyUniqueId, name);
        areaManager.deleteArea(companyUniqueId, area);
        player.sendMessage(Locale.getMessage("area-delete-success").replaceAll("%name%", name));
        return true;

    }

    public boolean setAreaLocation(String area, Location location) {
        areaManager.setLocation(companyUniqueId, area, location);
        player.sendMessage(Locale.getMessage("area-setloc-success"));
        return true;
    }

    public boolean setAreaLocation() {
        Location location = player.getLocation();
        CompanyArea area = areaManager.getAreaByLoc(location);
        if (area == null) {
            player.sendMessage(Locale.getMessage("no-area-in-location"));
            return true;
        }

        setAreaLocation(area.getName(), location);
        return true;

    }

    public boolean teleportToLocation(UUID targetCompanyUniqueId, String area) {

        if (!areaManager.getAreas(targetCompanyUniqueId).contains(area)) {
            player.sendMessage(Locale.getMessage("area-name-not-existed").replaceAll("%name%", area));
            return true;
        }

        Location location = areaManager.getLocation(targetCompanyUniqueId, area);
        if (location == null) {
            player.sendMessage(Locale.getMessage("area-loc-not-set").replaceAll("%name%", area));
            return true;
        }

        player.sendMessage(Locale.getMessage("area-tp-success").replaceAll("%name%", area).replaceAll("%company%", companyManager.getName(targetCompanyUniqueId)));
        player.teleportAsync(location);
        return true;

    }

}
