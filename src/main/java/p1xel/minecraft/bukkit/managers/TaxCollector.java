package p1xel.minecraft.bukkit.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.events.SalaryPaymentEvent;
import p1xel.minecraft.bukkit.events.TaxCollectEvent;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.storage.Locale;

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

            CompanyManager manager = MyCompany.getCacheManager().getCompanyManager();
            List<UUID> companies = MyCompany.getCacheManager().getCompanyManager().getAllCompanies();
            TaxCollectEvent event = new TaxCollectEvent(companies);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                task.cancel();
                return;
            }

            for (UUID uniqueId : companies) {

                // Priority: Income Tax > Property Tax > Management Fee

                // Income Tax
                double balance = manager.getCash(uniqueId);
                double dailyIncome = manager.getDailyIncome(uniqueId);
                double incomeTaxRate = Config.getDouble("company-funds.cost-per-day.income-tax.default-tax-rate");
                String incomePhase = getPhase("income-tax", dailyIncome);
                if (!incomePhase.equals("default")) {
                    incomeTaxRate = Config.getDouble("company-funds.cost-per-day.income-tax.phases." + incomePhase + ".tax-rate");
                }

                double incomeTax = dailyIncome * incomeTaxRate;
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
                manager.resetDailyIncome(uniqueId);

            }

            // Salary Time~~~~~~~~~~
            SalaryPaymentEvent salaryEvent = new SalaryPaymentEvent(companies);
            Bukkit.getPluginManager().callEvent(salaryEvent);
            double outcome = 0.0;

            for (UUID uniqueId : companies) {

                if (salaryEvent.isCancelled()) {
                    task.cancel();
                    return;
                }

                String companyName = manager.getName(uniqueId);

                // employer
                UUID employerUniqueId = manager.getEmployer(uniqueId);
                OfflinePlayer employer = Bukkit.getOfflinePlayer(employerUniqueId);
                double rawEmployerSalary = manager.getSalary(uniqueId, "employer");
                double employerSalary =  + Config.getDouble("company-funds.salaries.employer");
                MyCompany.getEconomy().depositPlayer(employer,  employerSalary);
                outcome += rawEmployerSalary;
                if (employer.isOnline()) {
                    Player player = (Player) employer;
                    player.sendMessage(Locale.getMessage("salary-received").replaceAll("%company%", companyName).replaceAll("%money%", String.valueOf(employerSalary)));
                    player.playSound(player, Sound.ENTITY_WITHER_HURT, 3f, 3f);
                }

                // employee
                for (String position : manager.getPositions(uniqueId)) {

                    double plus = 0.0;
                    // Check if there is the sponsorship
                    if (MyCompany.getInstance().getConfig().isSet("company-funds.salaries." + position)) {
                        plus = Config.getDouble("company-funds.salaries." + position);
                    }

                    for (UUID employeeUniqueId : manager.getEmployeeList(uniqueId, position)) {

                        OfflinePlayer employee = Bukkit.getOfflinePlayer(employeeUniqueId);
                        double rawEmployeeSalary = manager.getSalary(uniqueId, position);
                        double employeeSalary =  rawEmployeeSalary + plus;
                        MyCompany.getEconomy().depositPlayer(employee, employeeSalary);
                        outcome += rawEmployeeSalary;
                        if (employee.isOnline()) {
                            Player player = (Player) employee;
                            player.sendMessage(Locale.getMessage("salary-received").replaceAll("%company%", companyName).replaceAll("%money%", String.valueOf(employeeSalary)));
                            player.playSound(player, Sound.ENTITY_WITHER_HURT, 3f, 3f);
                        }

                    }

                }
                manager.setCash(uniqueId, manager.getCash(uniqueId) - outcome);
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
