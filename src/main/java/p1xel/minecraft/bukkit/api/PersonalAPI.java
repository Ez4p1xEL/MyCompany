package p1xel.minecraft.bukkit.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.BuildingManager;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.managers.ShopManager;
import p1xel.minecraft.bukkit.managers.UserManager;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.permissions.Permission;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.List;
import java.util.UUID;

public class PersonalAPI {

    private Player player;
    private UUID playerUniqueId;
    private UUID companyUniqueId;
    private final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();
    private final UserManager userManager = MyCompany.getCacheManager().getUserManager();
    private final ShopManager shopManager = MyCompany.getCacheManager().getShopManager();
    private final BuildingManager buildingManager = MyCompany.getCacheManager().getBuildingManager();

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

}
