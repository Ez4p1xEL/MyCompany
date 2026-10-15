package p1xel.minecraft.bukkit.object.focus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.CacheManager;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.focus.tree.Expertise;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseValue;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseCondition;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseConfig;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.storage.AbstractCompanyData;

import java.io.File;
import java.util.*;

public class FocusManager {

    private final AbstractCompanyData data;
    private final HashMap<UUID, CompanyFocus> companyFocusMap = new HashMap<>();
    private final HashMap<String, ExpertiseConfig> expertiseConfigMap = new HashMap<>();
    private File file;
    private FileConfiguration yaml;

    public FocusManager(AbstractCompanyData data) {
        this.data = data;
    }

    public void init() {
        companyFocusMap.clear();
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

        expertiseConfigMap.clear();
        initFile();
    }

    public void initFile() {
        File file = new File(MyCompany.getInstance().getDataFolder(), "focus.yml");
        if (!file.exists()) {
            MyCompany.getInstance().saveResource("focus.yml", false);
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        this.yaml = yaml;
        this.file = file;
        saveExpertiseConfigs();
    }

    public void saveExpertiseConfigs() {
        for (String focus : yaml.getConfigurationSection("focus-tree").getKeys(false)) {

            for (String expertiseId : yaml.getConfigurationSection("focus-tree." + focus).getKeys(false)) {
                String path = "focus-tree." + focus + "." + expertiseId + ".condition";
                double money = yaml.getDouble(path + ".money", 0.0);
                ExpertiseCondition condition = new ExpertiseCondition(money);
                ExpertiseConfig config = new ExpertiseConfig(expertiseId, condition);
                expertiseConfigMap.put(expertiseId, config);
            }

        }
    }



    public void setFocus(UUID companyUniqueId, Focus focus) {

    }

    public CompanyFocus getCompanyFocus(UUID companyUniqueId) {
        return companyFocusMap.get(companyUniqueId);
    }

    public ExpertiseConfig getExpertiseConfig(String expertiseId) {
        return expertiseConfigMap.get(expertiseId);
    }

    public void unlockExpertise(UUID companyUniqueId, Expertise expertise, Object value) {
        expertise.setUnlocked(true);

        Class<?> valueType = expertise.getValueType();
        if (valueType.equals(Double.class)) {
            expertise.setValue(ExpertiseValue.ofDouble(expertise.getId(), Double.parseDouble(value.toString())));
        } else if (valueType.equals(Integer.class)) {
            expertise.setValue(ExpertiseValue.ofInt(expertise.getId(), Integer.parseInt(value.toString())));
        } else if (valueType.equals(Boolean.class)) {
            expertise.setValue(ExpertiseValue.ofBool(expertise.getId(), true));
        } else if (valueType.equals(String.class)) {
            expertise.setValue(ExpertiseValue.ofString(expertise.getId(), value.toString()));
        } else {
            Logger.warn("Unsupported value type for expertise '" + expertise.getId() + "': " + valueType.getName());
        }

        this.data.setExpertise(companyUniqueId, expertise.getString());

    }

}
