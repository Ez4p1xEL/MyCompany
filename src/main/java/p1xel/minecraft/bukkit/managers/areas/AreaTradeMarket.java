package p1xel.minecraft.bukkit.managers.areas;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.AreaManager;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.Logger;

import java.util.*;
import java.util.logging.Level;

public class AreaTradeMarket {

    private HashSet<RentableCompanyArea> rents = new HashSet<>();
    private HashSet<TradableCompanyArea> trades = new HashSet<>();
    private final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();
    private final AreaManager areaManager = MyCompany.getCacheManager().getAreaManager();
    private AreaRentManager areaRentManager;

    public void init() {
        // Put data of local files into hashset cache

        for (UUID companyUniqueId : companyManager.getAllCompanies()) {

            String companyName = companyManager.getName(companyUniqueId);

            for (String areaName : areaManager.getAreas(companyUniqueId)) {

                String mode = (String) companyManager.getData().get(companyUniqueId, "area", "areas." + areaName + ".trade.mode");
                boolean onMarket = (boolean) companyManager.getData().get(companyUniqueId, "area", "areas." + areaName + ".trade.on-market");
                if (!onMarket) { continue;}
                switch (mode) {
                    case "none":
                        continue;
                    case "rent":
                        long startTime = Long.parseLong(Objects.toString(companyManager.getData().get(companyUniqueId, "area", "areas." + areaName + ".trade.rent.start-time")));
                        long endTime = Long.parseLong(Objects.toString(companyManager.getData().get(companyUniqueId, "area", "areas." + areaName + ".trade.rent.end-time")));
                        double rentPrice = (double) companyManager.getData().get(companyUniqueId, "area", "areas." + areaName + ".trade.rent.price");
                        RentableCompanyArea rentArea = new RentableCompanyArea(companyUniqueId, areaName, startTime, endTime, rentPrice);
                        rents.add(rentArea);
                        continue;
                    case "sell":
                        double sellPrice = (double) companyManager.getData().get(companyUniqueId, "area", "areas." + areaName + ".trade.sell.price");
                        TradableCompanyArea sellArea = new TradableCompanyArea(companyUniqueId, areaName, sellPrice);
                        trades.add(sellArea);
                        continue;
                }

                Logger.debug(Level.INFO, "Area " + areaName + " of " + companyName + " has been cached into trade market!");

            }

        }

        areaRentManager = new AreaRentManager();
        areaRentManager.init();

    }

    public HashSet<RentableCompanyArea> getRentMarketAreas() {
        return rents;
    }

    public HashSet<TradableCompanyArea> getTradeMarketAreas() {
        return trades;
    }

    public void rentArea(UUID originalCompanyUniqueId, UUID newCompanyUniqueId, String areaName) {
        //
        long time = Config.getInt("company-area.trade-market.rent.default-rent-time") * 24 * 60 * 60 * 1000L;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + time;
        companyManager.getData().set(originalCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.start-time", startTime);
        companyManager.getData().set(originalCompanyUniqueId, "area", "areas." + areaName + ".trade.on-market", false);
        companyManager.getData().set(originalCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.rented", true);
        companyManager.getData().set(originalCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.renter", newCompanyUniqueId.toString());
        companyManager.getData().set(originalCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.end-time", endTime);

        companyManager.getData().set(newCompanyUniqueId, "area", "rent-area." + originalCompanyUniqueId + "." + "rent#"+areaName + ".end-time", endTime);

        // Put the renter company into the accessible list of the area
        List<UUID> access = areaManager.getAccessibleCompanies(originalCompanyUniqueId, areaName);
        access.add(newCompanyUniqueId);
        areaManager.setAccessibleCompanies(originalCompanyUniqueId, areaName, access);

        areaRentManager.addAreaInTimer(new RentableCompanyArea(originalCompanyUniqueId, areaName, endTime, newCompanyUniqueId));

        double rentPrice = (double) companyManager.getData().get(originalCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.price");
        RentableCompanyArea rentArea = new RentableCompanyArea(originalCompanyUniqueId, areaName, 0L, 0L, rentPrice);
        rents.remove(rentArea);
    }

    public boolean onMarket(UUID companyUniqueId, String areaName) {
        return (boolean) companyManager.getData().get(companyUniqueId, "area", "areas." + areaName + ".trade.on-market");
    }

    public void setOnMarket(UUID companyUniqueId, String areaName, boolean bool) {
        companyManager.getData().set(companyUniqueId,"area", "areas." + areaName + ".trade.on-market", bool);
    }

    public void setTradeMode(UUID companyUniqueId, String areaName, String mode) {
        companyManager.getData().set(companyUniqueId, "area", "areas." + areaName + ".trade.mode", mode);
    }

    public String getTradeMode(UUID companyUniqueId, String areaName) {
        return (String) companyManager.getData().get(companyUniqueId, "area", "areas." + areaName + ".trade.mode");
    }

    public void createRentArea(UUID areaCompanyUniqueId, String areaName, double rentPrice) {

        RentableCompanyArea area = new RentableCompanyArea(areaCompanyUniqueId, areaName, 0L, 0L, rentPrice);
        rents.add(area);

        //companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.start-time", 0L);
        //companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.end-time", 0L);
        companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.price", rentPrice);
        companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.rented", false);

    }

    public void rentExpired(RentableCompanyArea area) {
        UUID areaCompanyUniqueId = area.getCompanyUUID();
        String areaName = area.getName();
        UUID renterCompanyUniqueId = area.getRenter();
        companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.mode", "none");
        companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.start-time", 0L);
        companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.end-time", 0L);
        companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.rented", false);
        companyManager.getData().set(renterCompanyUniqueId, "area", "rent-area." + areaCompanyUniqueId + "." + "rent#"+areaName, null);

        // Put the renter company out of the accessible list of the area
        List<UUID> access = areaManager.getAccessibleCompanies(areaCompanyUniqueId, areaName);
        access.remove(renterCompanyUniqueId);
        areaManager.setAccessibleCompanies(areaCompanyUniqueId, areaName, access);
    }

    public void addAreaIntoRentMarket(RentableCompanyArea area) {
        rents.add(area);
    }

    public void addAreaIntoRentMarket(UUID areaCompanyUniqueId, String areaName) {
        long startTime = Long.parseLong(Objects.toString(companyManager.getData().get(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.start-time")));
        long endTime = Long.parseLong(Objects.toString(companyManager.getData().get(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.end-time")));
        double rentPrice = (double) companyManager.getData().get(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.price");
        RentableCompanyArea rentArea = new RentableCompanyArea(areaCompanyUniqueId, areaName, startTime, endTime, rentPrice);
        addAreaIntoRentMarket(rentArea);
    }

    public void createTradableArea(UUID areaCompanyUniqueId, String areaName, double sellPrice) {

        companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.sell.price", sellPrice);

        TradableCompanyArea area = new TradableCompanyArea(areaCompanyUniqueId, areaName, sellPrice);
        trades.add(area);

    }

    public void sellArea(UUID originalCompanyUniqueId, UUID newCompanyUniqueId, String areaName) {

        //
        companyManager.getData().set(originalCompanyUniqueId, "area", "areas." + areaName + ".trade.on-market", false);

        double sellPrice = (double) companyManager.getData().get(originalCompanyUniqueId, "area", "areas." + areaName + ".trade.sell.price");
        TradableCompanyArea tradeArea = new TradableCompanyArea(originalCompanyUniqueId, areaName, sellPrice);
        trades.remove(tradeArea);

        // Check repeated area
        // Add "_2" at the end if there is same area name existed.
        Set<String> areas = areaManager.getAreas(newCompanyUniqueId);
        int attempts = 0;
        while (areas.contains(areaName) && attempts < 3) {
            areaName = areaName + "_2";
            attempts++;
        }

        // Delete CompanyArea from the seller company
        areaManager.deleteArea(originalCompanyUniqueId, tradeArea);

        // Create CompanyArea for the buyer company
        UUID employerUniqueId = companyManager.getEmployer(newCompanyUniqueId);
        OfflinePlayer employer = Bukkit.getOfflinePlayer(employerUniqueId);
        Location firstBlock = tradeArea.getFirst();
        Location secondBlock = tradeArea.getSecond();
        String world = firstBlock.getWorld().getName();
        int minX = Math.min(firstBlock.getBlockX(), secondBlock.getBlockX());
        int maxX = Math.max(firstBlock.getBlockX(), secondBlock.getBlockX());
        int minY = Math.min(firstBlock.getBlockY(), secondBlock.getBlockY());
        int maxY = Math.max(firstBlock.getBlockY(), secondBlock.getBlockY());
        int minZ = Math.min(firstBlock.getBlockZ(), secondBlock.getBlockZ());
        int maxZ = Math.max(firstBlock.getBlockZ(), secondBlock.getBlockZ());

        CompanyArea companyArea = new CompanyArea(newCompanyUniqueId, areaName, world, minX, maxX, minY, maxY, minZ, maxZ, firstBlock, secondBlock);
        areaManager.createArea(newCompanyUniqueId, companyArea, employer, firstBlock, secondBlock);

    }

    public void removeFromMarket(UUID areaCompanyUniqueId, String areaName) {
        setOnMarket(areaCompanyUniqueId, areaName, false);

        String mode = getTradeMode(areaCompanyUniqueId, areaName);

        if (mode.equals("rent")) {
            double rentPrice = (double) companyManager.getData().get(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.price");
            RentableCompanyArea area = new RentableCompanyArea(areaCompanyUniqueId, areaName, 0L, 0L, rentPrice);
            rents.remove(area);
            System.out.println(rents);
            companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.price", null);
        } else {
            double sellPrice = (double) companyManager.getData().get(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.sell.price");
            TradableCompanyArea area = new TradableCompanyArea(areaCompanyUniqueId, areaName, sellPrice);
            trades.remove(area);
            companyManager.getData().set(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.sell.price", null);
        }

        setTradeMode(areaCompanyUniqueId, areaName, "none");
    }

    public double getRentPrice(UUID areaCompanyUniqueId, String areaName) {
        return (double) companyManager.getData().get(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.rent.price");
    }

    public double getSellPrice(UUID areaCompanyUniqueId, String areaName) {
        return (double) companyManager.getData().get(areaCompanyUniqueId, "area", "areas." + areaName + ".trade.sell.price");
    }

    public AreaRentManager getRentManager() {return areaRentManager;}
}
