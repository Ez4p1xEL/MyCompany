package p1xel.minecraft.bukkit.util.focus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.object.action.*;
import p1xel.minecraft.bukkit.object.condition.*;
import p1xel.minecraft.bukkit.object.effect.Effect;
import p1xel.minecraft.bukkit.object.effect.EffectType;
import p1xel.minecraft.bukkit.object.effect.MobDamageEffect;
import p1xel.minecraft.bukkit.object.effect.MobDropEffect;
import p1xel.minecraft.bukkit.object.focus.tree.DefaultExpertiseInstance;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseValue;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseAction;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseCondition;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseConfig;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseEffect;
import p1xel.minecraft.bukkit.util.ConditionUtil;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.storage.Locale;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FocusUtil {

    public static FixedFocusList fixedFocusList = new FixedFocusList();

    private static final HashMap<String, File> files = new HashMap<>();
    private static final HashMap<String, FileConfiguration> yamls = new HashMap<>();

    public static final HashMap<String, List<Condition>> FOCUS_CONDITION_MAP = new HashMap<>();

    // 这两个都是储存 focus/下的所有文件名，focusId和expertiseId
    // 它们不应该被后期修改
    public static final List<String> ENABLED_FOCUS_LIST = new ArrayList<>();
    public static final HashMap<String, List<String>> ENABLED_EXPERTISE_LIST = new HashMap<>();

    private static final HashMap<String, HashMap<String, DefaultExpertiseInstance>> DEFAULT_EXPERTISE_INSTANCE_MAP = new HashMap<>();

    public static final HashMap<String, String> FOCUS_LABELS = new HashMap<>();
    public static final HashMap<String, List<String>> FOCUS_DESCRIPTIONS = new HashMap<>();
    public static final HashMap<String, String> EXPERTISE_LABELS = new HashMap<>();
    public static final HashMap<String, List<String>> EXPERTISE_DESCRIPTIONS = new HashMap<>();

    public static String STATUS_UNLOCKED = "LEARNED";
    public static String STATUS_LOCKED = "LOCKED";
    public static String STATUS_ENABLED = "ENABLED";
    public static String STATUS_DISABLED = "DISABLED";

    public static void createFiles() {

        File folder = new File(MyCompany.getInstance().getDataFolder(), "/focus");
        folder.mkdirs();

        String[] focusArray = fixedFocusList.getFocuses();
        for (String fileName : focusArray) {
            File file = new File(folder, fileName + ".yml");

            if (!file.exists()) {
                continue;
            }

            String focusId = file.getName().replace(".yml", "");
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            files.put(focusId, file);
            yamls.put(focusId, yaml);
            ENABLED_FOCUS_LIST.add(focusId);
            Set<String> expertiseList = yaml.getConfigurationSection("expertise").getKeys(false);
            ENABLED_EXPERTISE_LIST.put(focusId, new ArrayList<>(expertiseList));
        }

        // 第一次加载时用语言文件中的focus和expertise信息覆盖 focus/下所有文件
        loadLocaleForFirstTime();

        saveFocusLocale(); // 缓存

    }

    public static void saveFocusCondition(String focusId) {

        FileConfiguration yaml = yamls.get(focusId);

        ConfigurationSection configurationSection = yaml.getConfigurationSection("condition");
        if (configurationSection == null) {
            return;
        }

        for (String conditionId : configurationSection.getKeys(false)) {
            if (!yaml.isSet("condition." + conditionId)) {
                return;
            }

            Object value = yaml.get("condition." + conditionId);
            if (value == null) {
                continue;
            }
            Condition condition = ConditionUtil.getCondition(conditionId, value);

            if (condition != null) {
                FOCUS_CONDITION_MAP.computeIfAbsent(focusId, k -> new ArrayList<>()).add(condition);
            }
        }


    }

    private static void loadLocaleForFirstTime() {
        if (!Config.getBool("generate-default-config")) {
            return;
        }
        for (String focusId : files.keySet() ) {

            FileConfiguration yaml = yamls.get(focusId);

            // 第一次加载时用语言文件中的focus和expertise信息覆盖 focus/下所有文件
            yaml.set("label", Locale.yaml.getString("focus.focus." + focusId + ".label", focusId));
            yaml.set("description", Locale.yaml.getStringList("focus.focus." + focusId + ".description"));

            for (String expertiseId : yaml.getConfigurationSection("expertise").getKeys(false)) {
                yaml.set("expertise." + expertiseId + ".label", Locale.yaml.getString("focus.focus." + focusId + ".expertise." + expertiseId + ".label", "No Label"));
                yaml.set("expertise." + expertiseId + ".description", Locale.yaml.getStringList("focus.focus." + focusId + ".expertise." + expertiseId + ".description"));
            }

            try {
                yaml.save(files.get(focusId));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Should be called after initializing Locale
    public static void saveFocusLocale() {

        for (String focusId : files.keySet()) {

            FileConfiguration yaml = yamls.get(focusId);

            // focus label & description
            FOCUS_LABELS.put(focusId, yaml.getString("label", focusId));
            FOCUS_DESCRIPTIONS.put(focusId, yaml.getStringList("description"));

            // expertise label & description
            if (yaml.contains("expertise")) {

                for (String expertiseId : yaml.getConfigurationSection("expertise").getKeys(false)) {
                    EXPERTISE_LABELS.put(expertiseId, yaml.getString("expertise." + expertiseId + ".label", "No Label"));
                    EXPERTISE_DESCRIPTIONS.put(expertiseId, yaml.getStringList("expertise." + expertiseId + ".description"));
                }

            }
        }

        String statusPath = "focus.status.";
        STATUS_UNLOCKED = Locale.yaml.getString(statusPath + "unlocked", "LEARNED");
        STATUS_LOCKED = Locale.yaml.getString(statusPath + "locked", "LOCKED");
        STATUS_ENABLED = Locale.yaml.getString(statusPath + "enabled", "AVAILABLE");
        STATUS_DISABLED = Locale.yaml.getString(statusPath + "disabled", "UNAVAILABLE");
    }

    public static List<ExpertiseConfig> initExpertiseConfig(String focusId) {

        // 顺便缓存Focus的Condition
        saveFocusCondition(focusId);

        List<ExpertiseConfig> list = new ArrayList<>();
        FileConfiguration yaml = yamls.get(focusId);

        for (String expertiseId : yaml.getConfigurationSection("expertise").getKeys(false)) {

            // Expertise Condition
            ExpertiseCondition condition = getExpertiseCondition(focusId, expertiseId);

            // Expertise Effect
            ExpertiseEffect effect = getExpertiseEffect(focusId, expertiseId);

            // Expertise Action
            ExpertiseAction action = getExpertiseAction(focusId, expertiseId);

            ExpertiseConfig config = new ExpertiseConfig(expertiseId, condition, effect, action);
            list.add(config);

            // 创建 DefaultExpertiseInstance
            String valueType = yaml.getString("expertise." + expertiseId + ".value_type", "boolean");
            String defaultValue = yaml.getString("expertise." + expertiseId + ".default.value", "false");

            ExpertiseValue<?> value = switch (valueType) {
                case "boolean" -> ExpertiseValue.ofBool(expertiseId, Boolean.parseBoolean(defaultValue));
                case "integer" -> ExpertiseValue.ofInt(expertiseId, Integer.parseInt(defaultValue));
                case "double" -> ExpertiseValue.ofDouble(expertiseId, Double.parseDouble(defaultValue));
                case "string" -> ExpertiseValue.ofString(expertiseId, defaultValue);
                default ->
                    // 如果遇到未知的类型，默认使用字符串类型
                        ExpertiseValue.ofString(expertiseId, defaultValue);
            };

            DefaultExpertiseInstance defaultExpertiseInstance = new DefaultExpertiseInstance(expertiseId, focusId, true, false, value);
            DEFAULT_EXPERTISE_INSTANCE_MAP.computeIfAbsent(focusId, k -> new HashMap<>()).put(expertiseId, defaultExpertiseInstance);

        }
        return list;
    }

    private static ExpertiseCondition getExpertiseCondition(String focusId, String expertiseId) {
        FileConfiguration yaml = yamls.get(focusId);
        String condition_path = "expertise." + expertiseId + ".condition";

        HashSet<Condition> conditionSet = new HashSet<>();

        for (String conditionId : yaml.getConfigurationSection(condition_path).getKeys(false)) {

            if (!yaml.isSet(condition_path + "." + conditionId)) {
                continue;
            }

            Object value = yaml.get(condition_path + "." + conditionId);
            if (value == null) { continue; }
            Condition condition = ConditionUtil.getCondition(conditionId, value);

            if (condition != null) {
                conditionSet.add(condition);
            }
        }


        return new ExpertiseCondition(conditionSet);
    }

    private static ExpertiseEffect getExpertiseEffect(String focusId, String expertiseId) {
        FileConfiguration yaml = yamls.get(focusId);

        String effect_path = "expertise." + expertiseId + ".effect";
        HashMap<EffectType, List<Effect>> effectMap = new HashMap<>();
        ConfigurationSection effectSection = yaml.getConfigurationSection(effect_path);
        if (effectSection != null) {
            for (String effectId : yaml.getConfigurationSection(effect_path).getKeys(false)) {
                EffectType effectType = EffectType.getById(effectId);
                if (effectType == null) {
                    Logger.warn("Unknown EffectType: " + effectId + " in focus: " + focusId + ", expertise: " + expertiseId);
                    continue;
                }
                List<Effect> effects = new ArrayList<>();

                switch (effectType) {
                    case MOB_DROPS_ADD: {

                        for (String entityId : yaml.getConfigurationSection(effect_path + "." + effectId).getKeys(false)) {
                            List<String> stringList = yaml.getStringList(effect_path + "." + effectId + "." + entityId);
                            for (String args : stringList) {
                                MobDropEffect mobDropEffect = new MobDropEffect(entityId, args);
                                effects.add(mobDropEffect);
                            }
                        }
                        effectMap.put(effectType, effects);
                        break;

                    }

                    case MOB_DAMAGE_MULTIPLY: {
                        for (String entityId : yaml.getConfigurationSection(effect_path + "." + effectId).getKeys(false)) {
                            List<String> stringList = yaml.getStringList(effect_path + "." + effectId + "." + entityId);
                            for (String args : stringList) {
                                MobDamageEffect mobDamageEffect = new MobDamageEffect(entityId, args);
                                effects.add(mobDamageEffect);
                            }
                        }
                        effectMap.put(effectType, effects);
                        break;
                    }

                }
            }
        }

        return new ExpertiseEffect(effectMap);
    }

    public static ExpertiseAction getExpertiseAction(String focusId, String expertiseId) {
        FileConfiguration yaml = yamls.get(focusId);

        String action_path = "expertise." + expertiseId + ".action";
        HashMap<ActionType, Action> actionMap = new HashMap<>();
        ConfigurationSection actionSection = yaml.getConfigurationSection(action_path);
        if (actionSection != null) {
            for (String actionId : yaml.getConfigurationSection(action_path).getKeys(false)) {
                ActionType actionType = ActionType.getActionType(actionId);
                if (actionType == null) {
                    Logger.warn("Unknown ActionType: " + actionId + " in focus: " + focusId + ", expertise: " + expertiseId);
                    continue;
                }
                Action action = null;

                switch (actionType) {
                    case ENABLE_EXPERTISE: {
                        List<String> targetExpertiseIds = yaml.getStringList(action_path + "." + actionId);
                        action = new ExpertiseEnableAction(targetExpertiseIds);
                        break;
                    }
                    case DISABLE_EXPERTISE: {
                        List<String> targetExpertiseIds = yaml.getStringList(action_path + "." + actionId);
                        action = new ExpertiseDisableAction(targetExpertiseIds);
                        break;
                    }
                    case CONSOLE_COMMAND: {
                        List<String> commands = yaml.getStringList(action_path + "." + actionId);
                        action = new ConsoleCommandAction(commands, MyCompany.getInstance());
                        break;
                    }
                    case SET_PRICE_GROUP: {
                        String priceGroup = yaml.getString(action_path + "." + actionId, "normal");
                        action = new PriceGroupSetAction(priceGroup);
                        break;
                    }
                }

                if (action != null) {
                    actionMap.put(actionType, action);
                }
            }
        }

        return new ExpertiseAction(actionMap);
    }

    public static String getStatus(boolean status) { return status ? STATUS_UNLOCKED : STATUS_LOCKED; }

    public static String getEnabledLabel(boolean enabled) { return enabled ? STATUS_ENABLED : STATUS_DISABLED; }

    public static DefaultExpertiseInstance getDefaultExpertiseInstance(String focusId, String expertiseId) {
        if (DEFAULT_EXPERTISE_INSTANCE_MAP.containsKey(focusId)) {
            return DEFAULT_EXPERTISE_INSTANCE_MAP.get(focusId).get(expertiseId);
        }
        return null;
    }

}
