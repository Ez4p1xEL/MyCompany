package p1xel.minecraft.bukkit.util.extension;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.CacheManager;
import p1xel.minecraft.bukkit.manager.CompanyManager;
import p1xel.minecraft.bukkit.manager.UserManager;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.util.storage.Locale;

import java.util.List;
import java.util.UUID;

public class Placeholders extends PlaceholderExpansion {

    private final CacheManager cache;
    private final CompanyManager companyManager;
    private final UserManager userManager;

    public Placeholders(CacheManager cache) {
        this.cache = cache;
        this.companyManager = cache.getCompanyManager();
        this.userManager = cache.getUserManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mycompany";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Ez4p1xEL";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.10";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        UUID playerUniqueId = player.getUniqueId();
        User user = userManager.getUser(playerUniqueId);
        UUID companyUniqueId = user.getCompanyUUID();

        String[] args = params.split("_");

        if (args.length > 0) {

            switch (args[0]) {

                case "player":
                    if (args.length < 2) { return null; }

                    if (companyUniqueId == null) {
                        return Locale.getMessage("none");
                    }

                    Company company = cache.getCompany(companyUniqueId);

                    // %mycompany_player_<xxx>%
                    switch (args[1]) {

                        case "cid":
                            return String.valueOf(company.getId());
                        case "company":
                            return company.getName();
                        case "position":
                            return user.getPosition();
                        case "salary":
                            return String.valueOf(company.getPositionSalary(user.getPosition()));
                        case "employer":
                            OfflinePlayer employer = Bukkit.getOfflinePlayer(company.getEmployer());
                            return (user.getPosition().equalsIgnoreCase("employer") ? Locale.getMessage("yourself") : employer.getName());
                        default:
                            return null;
                    }

                    // %mycompany_company_<cid>_<xxx>%
                case "company":
                    if (args.length < 3) { return null; }

                    int cid;
                    if (args[1].equals("player")) {
                        if (companyUniqueId == null) {
                            return null;
                        }
                        company = cache.getCompany(companyUniqueId);
                        cid = company.getId();
                    } else {
                        try {
                            cid = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            return null;
                        }

                        if (companyManager.getUUID(cid) == null) {
                            return null;
                        }

                        companyUniqueId = companyManager.getUUID(cid);
                        company = cache.getCompany(companyUniqueId);
                    }

                    switch (args[2]) {

                        case "name":
                            return company.getName();
                        case "employer":
                            OfflinePlayer employer = Bukkit.getOfflinePlayer(company.getEmployer());
                            return (employer.getName());
                        case "founder":
                            return companyManager.getFounderName(companyUniqueId);
                        case "total":
                            return String.valueOf(company.getPersonnelAmount());
                        case "founddate":
                            return companyManager.getFoundDate(companyUniqueId);
                        case "totalof":
                            if (args.length < 4) { return null; }
                            List<UUID> list = company.getEmployeeList(args[3]);
                            if (list == null) { return null; }
                            return String.valueOf(list.size());
                        case "cash":
                            return String.valueOf(company.getCash());
                        case "income":
                            if (args.length < 4) { return null; }
                            switch (args[3]) {
                                case "daily":
                                    return String.valueOf(company.getDailyIncome());
                                case "total":
                                    return String.valueOf(company.getTotalIncome());
                                default:
                                    return null;
                            }
                        case "salary":
                            if (args.length < 4) { return null; }
                            return String.valueOf(company.getPositionSalary(args[3]));
                        case "taxrate":
                            if (args.length < 4) { return null; }
                            switch (args[3]) {
                                case "property":
                                    return MyCompany.getTaxCollector().getPhase("property-tax", company.getCash());
                                case "income":
                                    return MyCompany.getTaxCollector().getPhase("income-tax", company.getCash());
                                default:
                                    return null;
                            }
                    }
            }

        }

        return null;
    }

}
