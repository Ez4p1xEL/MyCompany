package p1xel.minecraft.bukkit.managers.areas;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.AreaManager;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class AreaRentManager {

    private HashMap<RentableCompanyArea, AreaRentTimer> activeTimer = new HashMap<>();
    private final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();
    private final AreaManager areaManager = MyCompany.getCacheManager().getAreaManager();
    private final AreaTradeMarket market = areaManager.getMarketManager();

    public void init() {
        for (UUID companyUniqueId : companyManager.getAllCompanies()) {

            for (String areaName : areaManager.getAreas(companyUniqueId)) {

                String mode = (String) companyManager.getData().get(companyUniqueId, "area", "areas." + areaName + ".trade.mode");
                if (!mode.equalsIgnoreCase("rent")) {
                    continue;
                }

                boolean onRent = areaManager.isAreaRented(companyUniqueId, areaName);
                if (!onRent) {
                    continue;
                }

                UUID renter = areaManager.getAreaRenter(companyUniqueId, areaName);
                long endTime = areaManager.getRentEndTime(renter, companyUniqueId, areaName);

                RentableCompanyArea area = new RentableCompanyArea(companyUniqueId, areaName, endTime, renter);
                startTimer(area, endTime);

            }
        }
    }

    public void addAreaInTimer(RentableCompanyArea area) {
        startTimer(area, area.getEndTime());
    }


    public boolean startTimer(RentableCompanyArea area, long endTime) {
        if (activeTimer.containsKey(area)) {
            // In cooldown
            return false;
        }

        // Create a request/contract
        AreaRentTimer timer = new AreaRentTimer(area, System.currentTimeMillis(), null);

        long delayMillis = endTime - System.currentTimeMillis();
        long delayTicks = delayMillis / 50;
        if (delayTicks < 0) delayTicks = 0; // prevent negative figure
        // Task
        BukkitTask task = Bukkit.getScheduler().runTaskLater(MyCompany.getInstance(), () -> {
            expireTimer(timer);
        }, delayTicks);

        timer.setExpirationTask(task);

        activeTimer.put(area, timer);
        return true;
    }

    private void expireTimer(AreaRentTimer timer) {
        RentableCompanyArea area = timer.getRentableCompanyArea();
        activeTimer.remove(area);

        market.rentExpired(area);

        String areaName = area.getName();
        UUID areaCompanyUniqueId = area.getCompanyUUID();
        UUID renterCompanyUniqueId = area.getRenter();
        String companyName = companyManager.getName(areaCompanyUniqueId);
        Player owner = Bukkit.getPlayer(companyManager.getEmployer(areaCompanyUniqueId));
        if (owner != null) {
            owner.sendMessage(Locale.getMessage("area-rent-expired").replaceAll("%area%", areaName));
        }
        Player renter = Bukkit.getPlayer(companyManager.getEmployer(renterCompanyUniqueId));
        if (renter != null) {
            renter.sendMessage(Locale.getMessage("area-rent-expired-renter").replaceAll("%area%", areaName).replaceAll("%company%", companyName));
        }
    }
    
}
