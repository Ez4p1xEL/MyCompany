package p1xel.minecraft.bukkit.object.focus;

import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.CacheManager;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.focus.tree.Expertise;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.storage.AbstractCompanyData;

import java.util.*;

public class FocusManager {

    private final AbstractCompanyData data;
    private final HashMap<UUID, CompanyFocus> companyFocusMap = new HashMap<>();

    public FocusManager(AbstractCompanyData data) {
        this.data = data;
    }

    public void init() {
        CacheManager cache = MyCompany.getCacheManager();
        for (UUID uniqueId : data.getAllCompanies()) {

            String focusName = data.getFocusInName(uniqueId);

            HashSet<Expertise> expertiseSet = new HashSet<>();

            // get expertises list
            List<String> expertiseList = data.getExpertises(uniqueId);
            for (String string : expertiseList) {
                String expertiseId = string.split(";")[0];
                Expertise expertise = Expertise.getById(expertiseId);
                if (expertise == null) {
                    Logger.warn("Expertise with id '" + expertiseId + "' not found for company " + uniqueId);
                    continue;
                }
                expertise.setByString(string);
                expertiseSet.add(expertise);

            }

            Company company = cache.getCompany(uniqueId);

            // get Focus by focusName
            // save CompanyFocus to companyFocusMap
            if (focusName.equals("mob")) {
                Mob mob = new Mob("mob", expertiseSet);
                CompanyFocus companyFocus = new CompanyFocus(mob, company);
                companyFocusMap.put(uniqueId, companyFocus);
                continue;
            }

            if (focusName.equals("none")) {
                NoneFocus noneFocus = new NoneFocus("none", expertiseSet);
                CompanyFocus companyFocus = new CompanyFocus(noneFocus, company);
                companyFocusMap.put(uniqueId, companyFocus);
                continue;
            }


        }


        System.out.println(Arrays.toString(companyFocusMap.keySet().toArray()));
    }

    public void setFocus(UUID companyUniqueId, Focus focus) {

    }

}
