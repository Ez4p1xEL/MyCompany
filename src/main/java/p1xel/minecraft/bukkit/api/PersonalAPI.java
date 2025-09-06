package p1xel.minecraft.bukkit.api;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.BuildingManager;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.managers.ShopManager;
import p1xel.minecraft.bukkit.managers.UserManager;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.storage.Locale;

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

}
