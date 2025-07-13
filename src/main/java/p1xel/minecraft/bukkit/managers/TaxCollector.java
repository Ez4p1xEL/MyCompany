package p1xel.minecraft.bukkit.managers;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.events.TaxCollectEvent;
import p1xel.minecraft.bukkit.utils.Config;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class TaxCollector {

    private BukkitTask task;

    private Duration durationUntilNextTime() {
        LocalDateTime now = LocalDateTime.now();
        String timeStr = Config.getString("company-funds.cost-per-day.tax-time");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime targetTime = LocalTime.parse(timeStr, formatter);
        LocalDateTime todayTarget = now.toLocalDate().atTime(targetTime);

        if (now.isBefore(todayTarget)) {
            // If the time is earlier than the target time.
            return Duration.between(now, todayTarget);
        }

        // If the time is later than the target time.
        LocalDateTime tmrTarget = now.toLocalDate().plusDays(1).atTime(targetTime);
        return Duration.between(now, tmrTarget);

    }

    public long getInitialDelayTicks() {
        Duration duration = durationUntilNextTime();
        long delayMillis = duration.toMillis();
        return delayMillis / 50; // 1tick = 50ms
    }

    public void startTask() {
        long initialDelay = getInitialDelayTicks();
        long period = 24 * 60 * 60 * 20;

        // between, every 24hrs
        task = Bukkit.getScheduler().runTaskTimer(MyCompany.getInstance(), () -> {

            List<UUID> companies = MyCompany.getCacheManager().getCompanyManager().getAllCompanies();
            TaxCollectEvent event = new TaxCollectEvent(companies);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                task.cancel();
            }

            for (UUID uniqueId : companies) {

                // Priority: Income Tax > Property Tax > Management Fee

                // Income Tax
                CompanyManager manager = MyCompany.getCacheManager().getCompanyManager();
                double balance = manager.getCash(uniqueId);
                double incomeTaxRate = Config.getDouble("company-funds.cost-per-day.income-tax.default-tax-rate");
                String incomePhase = getPhase("income-tax", balance);
                if (!incomePhase.equals("default")) {
                    incomeTaxRate = Config.getDouble("company-funds.cost-per-day.income-tax.phases." + incomePhase + ".tax-rate");
                }

                double incomeTax = balance * incomeTaxRate;
                balance = balance - incomeTax;
                //manager.setCash(uniqueId, balance);


                // Property Tax
                double propertyTaxRate = Config.getDouble("company-funds.cost-per-day.property-tax.default-tax-rate");
                String propertyPhase = getPhase("property-tax", balance);
                if (!propertyPhase.equals("default")) {
                    propertyTaxRate = Config.getDouble("company-funds.cost-per-day.property-tax.phases." + propertyPhase + ".tax-rate");
                }

                double propertyTax = balance * propertyTaxRate;
                balance = balance - propertyTax;
                //manager.setCash(uniqueId, balance);

                // Management fee
                double mf = Config.getDouble("company-funds.cost-per-day.management-fee");
                balance = balance - mf;

                manager.setCash(uniqueId, balance);

            }

        }, initialDelay, period );
    }

    public void cancelTask() {
        task.cancel();
    }

    public String getPhase(String tax, double balance) {

        String phaseName = "default";
        double phaseCash = 0.0; // choose bigger one

        for (String phase : MyCompany.getInstance().getConfig().getConfigurationSection("company-funds.cost-per-day." + tax + ".phases").getKeys(false)) {

            double cash = Config.getDouble("company-funds.cost-per-day." + tax + ".phases." + phase + ".cash");
            if (balance >= cash) {
                phaseCash = Math.max(phaseCash, cash);
                phaseName = phase;
            }

        }

        return phaseName;

    }

}
