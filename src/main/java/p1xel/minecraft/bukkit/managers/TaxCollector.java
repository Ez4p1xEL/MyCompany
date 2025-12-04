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
    private final UserManager userManager = MyCompany.getCacheManager().getUserManager();
    private final CompanyManager manager = MyCompany.getCacheManager().getCompanyManager();

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

            List<UUID> companies = manager.getAllCompanies();
            TaxCollectEvent event = new TaxCollectEvent(companies);
            Bukkit.getPluginManager().callEvent(event);

            for (UUID uniqueId : companies) {

                if (event.isCancelled()) {
                    task.cancel();
                    break;
                }

                // Priority: Income Tax > Property Tax > Management Fee

                // Cancel if company has no money
                if (manager.getCash(uniqueId) <= 0) {
                    break;
                }

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

            for (UUID uniqueId : companies) {

                double outcome = 0.0;

                if (salaryEvent.isCancelled()) {
                    task.cancel();
                    break;
                }

                // Cancel if company has no money
                double balance = manager.getCash(uniqueId);
                if (balance <= 0) {
                    for (String position : manager.getPositions(uniqueId)) {
                        // employer
                        if (position.equals("employer")) {
                            Player employer = Bukkit.getPlayer(manager.getEmployer(uniqueId));
                            if (employer != null) {
                                employer.sendMessage(Locale.getMessage("salary-cancel-due-to-break"));
                                employer.playSound(employer, Sound.ENTITY_VILLAGER_NO, 3f, 3f);
                            }
                            continue;
                        }
                        // employee
                        for (UUID employeeUniqueId : manager.getEmployeeList(uniqueId, position)) {
                            Player employee = Bukkit.getPlayer(employeeUniqueId);
                            if (employee != null) {
                                employee.sendMessage(Locale.getMessage("salary-cancel-due-to-break"));
                                employee.playSound(employee, Sound.ENTITY_VILLAGER_NO, 3f, 3f);
                            }
                            continue;
                        }
                    }
                    break;
                }

                String companyName = manager.getName(uniqueId);

                // employer
                UUID employerUniqueId = manager.getEmployer(uniqueId);
                OfflinePlayer employer = Bukkit.getOfflinePlayer(employerUniqueId);
                double rawEmployerSalary = manager.getSalary(uniqueId, "employer");
                if (balance - outcome < rawEmployerSalary) {
                    rawEmployerSalary = Math.max(balance-outcome, 0);
                }
                double employerSalary =  rawEmployerSalary + Config.getDouble("company-funds.salaries.employer");
                MyCompany.getEconomy().depositPlayer(employer, employerSalary);
                outcome += rawEmployerSalary;
                if (employer.isOnline()) {
                    Player player = (Player) employer;
                    player.sendMessage(Locale.getMessage("salary-received").replaceAll("%company%", companyName).replaceAll("%money%", String.valueOf(employerSalary)));
                    player.playSound(player, Sound.ENTITY_WITHER_HURT, 3f, 3f);
                }

                // employee
                for (String position : manager.getPositions(uniqueId)) {

                    double plus = 0.0;
                    plus = Config.getDouble("company-funds.salaries.employee");

                    for (UUID employeeUniqueId : manager.getEmployeeList(uniqueId, position)) {

                        OfflinePlayer employee = Bukkit.getOfflinePlayer(employeeUniqueId);
                        double rawEmployeeSalary = manager.getSalary(uniqueId, position);
                        if (balance - outcome < rawEmployeeSalary) {
                            rawEmployeeSalary = Math.max(balance-outcome, 0);
                        }
                        double employeeSalary = rawEmployeeSalary + plus;
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

            // Refresh employees' daily orders
            for (UUID uniqueId : companies) {
                List<String> positions = manager.getPositions(uniqueId);
                for (String position : positions) {
//                    if (position.equals("employer")) {
//                        continue;
//                    }
                    for (UUID employeeUniqueId : manager.getEmployeeList(uniqueId, position)) {
                        userManager.createUser(employeeUniqueId);
                        userManager.randomizeDailyOrder(employeeUniqueId);
                    }
                }
                UUID employerUniqueId = manager.getEmployer(uniqueId);
                userManager.createUser(employerUniqueId);
                userManager.randomizeDailyOrder(employerUniqueId);
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
