package p1xel.minecraft.bukkit.object.focus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.CacheManager;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.action.Action;
import p1xel.minecraft.bukkit.object.condition.CompanyMoneyCondition;
import p1xel.minecraft.bukkit.object.condition.Condition;
import p1xel.minecraft.bukkit.object.condition.ExpertiseUnlockedCondition;
import p1xel.minecraft.bukkit.object.effect.EffectType;
import p1xel.minecraft.bukkit.object.focus.tree.DefaultExpertiseInstance;
import p1xel.minecraft.bukkit.object.focus.tree.Expertise;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseInstance;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseValue;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseAction;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseCondition;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseConfig;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseEffect;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.focus.FocusUtil;
import p1xel.minecraft.bukkit.util.storage.AbstractCompanyData;

import java.io.File;
import java.util.*;

public class FocusManager {

    private final AbstractCompanyData data;
    private final HashMap<UUID, CompanyFocus> companyFocusMap = new HashMap<>();
    private final HashMap<String, ExpertiseConfig> expertiseConfigMap = new HashMap<>();

    public FocusManager(AbstractCompanyData data) {
        this.data = data;
    }

    public void init() {
        companyFocusMap.clear();
        expertiseConfigMap.clear();
        initFile();
        CacheManager cache = MyCompany.getCacheManager();
        for (UUID uniqueId : data.getAllCompanies()) {

            String focusName = data.getFocusInName(uniqueId);

            List<ExpertiseInstance> companyExpertiseList = new ArrayList<>();

            // 更新公司的Expertise列表，确保与配置文件中的专精树保持一致
            updateExpertisesFromEnum(uniqueId, focusName);

            // get expertises list
            // TODO: 取代原 Expertise 类的静态方法，改为从配置文件中读取专精树
            for (String expertiseId : FocusUtil.ENABLED_EXPERTISE_LIST.get(focusName)) {
                String string = data.getExpertise(uniqueId, expertiseId);
                if (string.isEmpty()) {
                    continue;
                }
                DefaultExpertiseInstance defaultExpertiseInstance = FocusUtil.getDefaultExpertiseInstance(focusName, expertiseId);
                if (defaultExpertiseInstance == null) {
                    Logger.warn("DefaultExpertiseInstance not found for focus: " + focusName + ", expertise: " + expertiseId);
                    continue;
                }
                ExpertiseInstance expertiseInstance = new ExpertiseInstance(defaultExpertiseInstance);
                expertiseInstance.setByString(string);
                companyExpertiseList.add(expertiseInstance);

            }

            Company company = cache.getCompany(uniqueId);

            // get Focus by focusName
            // save CompanyFocus to companyFocusMap
            if (focusName.equals("mob")) {
                Mob mob = new Mob("mob", companyExpertiseList);
                CompanyFocus companyFocus = new CompanyFocus(mob, company);
                companyFocusMap.put(uniqueId, companyFocus);
                continue;
            }

            if (focusName.equals("none")) {
                NoneFocus noneFocus = new NoneFocus("none", companyExpertiseList);
                CompanyFocus companyFocus = new CompanyFocus(noneFocus, company);
                companyFocusMap.put(uniqueId, companyFocus);
                continue;
            }

            if (focusName.equals("ore")) {
                NoneFocus noneFocus = new NoneFocus("ore", companyExpertiseList);
                CompanyFocus companyFocus = new CompanyFocus(noneFocus, company);
                companyFocusMap.put(uniqueId, companyFocus);
                continue;
            }

            CompanyFocus companyFocus = new CompanyFocus(new Focus(focusName, companyExpertiseList), company);
            companyFocusMap.put(uniqueId, companyFocus);


        }
    }

    public void initFile() {
        saveExpertiseConfigs();
    }

    public void saveExpertiseConfigs() {

        for (String focusId : FocusUtil.ENABLED_FOCUS_LIST) {
            List<ExpertiseConfig> expertiseConfigs = FocusUtil.initExpertiseConfig(focusId);
            for (ExpertiseConfig config : expertiseConfigs) {
                expertiseConfigMap.put(config.getExpertiseId(), config);
            }
        }

//        for (String focus : yaml.getConfigurationSection("focus-tree").getKeys(false)) {
//
//            for (String expertiseId : yaml.getConfigurationSection("focus-tree." + focus).getKeys(false)) {
//
//                // Expertise Condition
//                String condition_path = "focus-tree." + focus + "." + expertiseId + ".condition";
//
//                HashSet<Condition> conditionSet = new HashSet<>();
//                // 检查 Condition 的类型
//                if (yaml.isSet(condition_path +".company_money")) {
//                    double money = yaml.getDouble(condition_path + ".company_money", 0.0);
//                    conditionSet.add(new CompanyMoneyCondition(money, true));
//                }
//
//                if (yaml.isSet(condition_path +".expertise")) {
//                    String expertise_id_unlocked = yaml.getString(condition_path + ".expertise", "");
//                    conditionSet.add(new ExpertiseUnlockedCondition(expertise_id_unlocked, false));
//                } else if (yaml.isSet(condition_path + ".expertises")) {
//                    List<String> expertiseIds = yaml.getStringList(condition_path + ".expertises");
//                    for (String expertise_id_unlocked : expertiseIds) {
//                        conditionSet.add(new ExpertiseUnlockedCondition(expertise_id_unlocked, false));
//                    }
//                }
//
//                ExpertiseCondition condition = new ExpertiseCondition(conditionSet);
//
//                // Expertise Effect
//                String effect_path = "focus-tree." + focus + "." + expertiseId + ".effect";
//                HashMap<String, Double> effectMap = new HashMap<>();
//                ConfigurationSection effectSection = yaml.getConfigurationSection(effect_path);
//                if (effectSection != null) {
//                    for (String effectId : yaml.getConfigurationSection(effect_path).getKeys(false)) {
//                        double value = yaml.getDouble(effect_path + "." + effectId, 0.0);
//                        effectMap.put(effectId, value);
//                    }
//                }
//                ExpertiseEffect effect = new ExpertiseEffect(effectMap);
//
//                ExpertiseConfig config = new ExpertiseConfig(expertiseId, condition, effect);
//                expertiseConfigMap.put(expertiseId, config);
//            }
//
//        }
    }

    /*
     * generateFocus() 和 setFocus() 都是给 新增及修改Focus的时候使用的
     * generateFocus()会生成一个全新的Focus对象
     * 然后配合 setFocus() 使用，可以直接覆盖掉旧版的Focus对象
     */
    public Focus generateFocus(String focusId) {
        List<DefaultExpertiseInstance> defaultExpertiseList = new ArrayList<>();
        for (String expertiseId : FocusUtil.ENABLED_EXPERTISE_LIST.get(focusId)) {
            DefaultExpertiseInstance defaultExpertiseInstance = FocusUtil.getDefaultExpertiseInstance(focusId, expertiseId);
            if (defaultExpertiseInstance != null) {
                defaultExpertiseList.add(defaultExpertiseInstance);
            }
        }

        List<ExpertiseInstance> companyExpertiseInstanceList = new ArrayList<>();
        for (DefaultExpertiseInstance defaultExpertiseInstance : defaultExpertiseList) {
            companyExpertiseInstanceList.add(new ExpertiseInstance(defaultExpertiseInstance));
        }

        switch (focusId) {
            case "mob":
                return new Mob(focusId, companyExpertiseInstanceList);
            case "none":
                return new NoneFocus(focusId, companyExpertiseInstanceList);
            case "ore":
                return new Ore(focusId, companyExpertiseInstanceList);
            default:
                if (FocusUtil.ENABLED_FOCUS_LIST.contains(focusId)) {
                    return new Focus(focusId, companyExpertiseInstanceList);
                }
                Logger.warn("Focus with id '" + focusId + "' not found.");
                return null;
        }
    }

    public void setFocus(UUID companyUniqueId, Focus focus) {
        // 先移除旧的Focus和Expertises数据
        this.data.removeFocusAndExpertises(companyUniqueId);

        this.data.setFocus(companyUniqueId, focus.getId());
        for (ExpertiseInstance expertiseInstance : focus.expertiseMap.values()) {
            this.data.setExpertise(companyUniqueId, expertiseInstance.getString());
        }

        CompanyFocus companyFocus = getCompanyFocus(companyUniqueId);
        companyFocus.setFocus(focus);

        // 重置 PriceGroup
        MyCompany.getCacheManager().getCompanyManager().setPriceGroup(companyUniqueId, "normal");
    }

    public void unlockExpertise(UUID companyUniqueId, ExpertiseInstance expertiseInstance, Object value) {
        expertiseInstance.setUnlocked(true);

        Class<?> valueType = expertiseInstance.getValueType();
        if (valueType.equals(Double.class)) {
            expertiseInstance.setValue(ExpertiseValue.ofDouble(expertiseInstance.getId(), Double.parseDouble(value.toString())));
        } else if (valueType.equals(Integer.class)) {
            expertiseInstance.setValue(ExpertiseValue.ofInt(expertiseInstance.getId(), Integer.parseInt(value.toString())));
        } else if (valueType.equals(Boolean.class)) {
            expertiseInstance.setValue(ExpertiseValue.ofBool(expertiseInstance.getId(), true));
        } else if (valueType.equals(String.class)) {
            expertiseInstance.setValue(ExpertiseValue.ofString(expertiseInstance.getId(), value.toString()));
        } else {
            Logger.warn("Unsupported value type for expertise '" + expertiseInstance.getId() + "': " + valueType.getName());
        }

        this.data.setExpertise(companyUniqueId, expertiseInstance.getString());

        // execute Expertise Action
        ExpertiseAction expertiseAction = getExpertiseConfig(expertiseInstance.getId()).getAction();
        if (expertiseAction == null) {
            // 没有设置 Action，就不执行任何操作
            return;
        }

        executeExpertiseAction(companyUniqueId, expertiseAction);

    }

    public void executeExpertiseAction(UUID companyUniqueId, ExpertiseAction expertiseAction) {
        Company company = MyCompany.getCacheManager().getCompany(companyUniqueId);

        for (Action action : expertiseAction.getActions().values()) {
            boolean success = action.execute(company, null);
            if (!success) {
                Logger.warn("Failed to execute action: " + action.getActionType().getId() + " for company: " + companyUniqueId);
            }
        }
    }

    // Use this to set expertise value directly.
    // Make sure the expertise is existed. Otherwise, it will create a new expertise with the given value, which may not be intended.
    public void setExpertise(UUID companyUniqueId, String expertiseString) {
        this.data.setExpertise(companyUniqueId, expertiseString);
    }

    public void updateExpertisesFromEnum(UUID companyUniqueId, String focusId) {
//        for (Expertise expertise : Expertise.getExpertisesByFocusId(focusId)) {
//
//            String string = data.getExpertise(companyUniqueId, expertise.getId());
//
//            if (string.isEmpty()) {
//                // 如果公司没有这个专精，就添加它
//                // 默认值为ExpertiseValue的默认值
//                this.data.setExpertise(companyUniqueId, expertise.getString());
//            }
//
//        }

        for (String expertiseId : FocusUtil.ENABLED_EXPERTISE_LIST.get(focusId)) {
            String string = data.getExpertise(companyUniqueId, expertiseId);
            if (string.isEmpty()) {
                DefaultExpertiseInstance defaultExpertiseInstance = FocusUtil.getDefaultExpertiseInstance(focusId, expertiseId);
                if (defaultExpertiseInstance == null) {
                    Logger.warn("DefaultExpertiseInstance not found for focus: " + focusId + ", expertise: " + expertiseId);
                    continue;
                }
                this.data.setExpertise(companyUniqueId, defaultExpertiseInstance.getString());
            }
        }
    }

    public List<ExpertiseInstance> getExpertisesWithEffectType(CompanyFocus companyFocus, EffectType effectType) {
        if (companyFocus == null) {
            return Collections.emptyList();
        }

        List<ExpertiseInstance> expertiseInstancesWithEffect = new ArrayList<>();
        for (ExpertiseInstance expertiseInstance : companyFocus.getFocus().expertiseMap.values()) {
            ExpertiseConfig config = getExpertiseConfig(expertiseInstance.getId());
            if (config != null && config.getEffect().getEffects().containsKey(effectType)) {
                expertiseInstancesWithEffect.add(expertiseInstance);
            }
        }
        return expertiseInstancesWithEffect;
    }

    public CompanyFocus getCompanyFocus(UUID companyUniqueId) {
        return companyFocusMap.get(companyUniqueId);
    }

    public ExpertiseConfig getExpertiseConfig(String expertiseId) {
        return expertiseConfigMap.get(expertiseId);
    }

}
