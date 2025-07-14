package p1xel.minecraft.bukkit.utils.extensions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.CacheManager;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.managers.UserManager;
import p1xel.minecraft.bukkit.utils.storage.Locale;
import p1xel.minecraft.bukkit.utils.storage.cidstorage.CIdData;

import java.util.List;
import java.util.UUID;

public class Placeholders extends PlaceholderExpansion {

    private final CacheManager cache;
    private final CompanyManager company;
    private final UserManager user;

    public Placeholders(CacheManager cache) {
        this.cache = cache;
        this.company = cache.getCompanyManager();
        this.user = cache.getUserManager();
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
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        UUID playerUniqueId = player.getUniqueId();
        UUID companyUniqueId = user.getCompanyUUID(playerUniqueId);

        String[] args = params.split("_");

        if (args.length > 0) {

            switch (args[0]) {

                case "player":
                    if (args.length < 2) { return null; }

                    if (companyUniqueId == null) {
                        return Locale.getMessage("none");
                    }

                    // %mycompany_player_<xxx>%
                    switch (args[1]) {

                        case "cid":
                            return String.valueOf(company.getId(companyUniqueId));
                        case "company":
                            return company.getName(companyUniqueId);
                        case "position":
                            return user.getPosition(playerUniqueId);
                        case "salary":
                            return String.valueOf(company.getSalary(playerUniqueId, user.getPosition(playerUniqueId)));
                        case "employer":
                            OfflinePlayer employer = Bukkit.getOfflinePlayer(company.getEmployer(companyUniqueId));
                            return (user.getPosition(playerUniqueId).equalsIgnoreCase("employer") ? Locale.getMessage("yourself") : employer.getName());
                        default:
                            return null;
                    }

                    // %mycompany_company_<cid>_<xxx>%
                case "company":
                    if (args.length < 3) { return null; }

                    int cid;
                    try {
                        cid = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        return null;
                    }

                    if (company.getUUID(cid) == null) {
                        return null;
                    }

                    switch (args[2]) {

                        case "name":
                            return company.getName(companyUniqueId);
                        case "employer":
                            OfflinePlayer employer = Bukkit.getOfflinePlayer(company.getEmployer(companyUniqueId));
                            return (employer.getName());
                        case "founder":
                            return company.getFounderName(companyUniqueId);
                        case "total":
                            return String.valueOf(company.getMemberAmount(companyUniqueId));
                        case "founddate":
                            return company.getFoundDate(companyUniqueId);
                        case "totalof":
                            if (args.length < 4) { return null; }
                            List<UUID> list = company.getEmployeeList(companyUniqueId, args[3]);
                            if (list == null) { return null; }
                            return String.valueOf(list.size());
                        case "cash":
                            return String.valueOf(company.getCash(companyUniqueId));
                        case "income":
                            if (args.length < 4) { return null; }
                            switch (args[3]) {
                                case "daily":
                                    return String.valueOf(company.getDailyIncome(companyUniqueId));
                                case "total":
                                    return String.valueOf(company.getTotalIncome(companyUniqueId));
                                default:
                                    return null;
                            }
                        case "salary":
                            if (args.length < 4) { return null; }
                            return String.valueOf(company.getSalary(companyUniqueId, args[3]));
                        case "taxrate":
                            if (args.length < 4) { return null; }
                            switch (args[3]) {
                                case "property":
                                    return MyCompany.getTaxCollector().getPhase("property-tax", company.getCash(companyUniqueId));
                                case "income":
                                    return MyCompany.getTaxCollector().getPhase("income-tax", company.getCash(companyUniqueId));
                                default:
                                    return null;
                            }
                    }
            }

        }

        return null;
    }

}
