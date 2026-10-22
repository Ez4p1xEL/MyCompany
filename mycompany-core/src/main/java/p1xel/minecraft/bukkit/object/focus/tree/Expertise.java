package p1xel.minecraft.bukkit.object.focus.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Deprecated
public enum Expertise {

    empty("empty", "normal", false, Boolean.class, ExpertiseValue.ofBool("empty", false)),
    more_drops_level1("more_drops_zombie_level1", "mob", false, Boolean.class ,ExpertiseValue.ofBool("more_drops_zombie_level1", false)),
    more_drops_level2("more_drops_zombie_level2", "mob", false, Boolean.class ,ExpertiseValue.ofBool("more_drops_zombie_level2", false)),;

    /**
     * @description: 专精树中每个专长
     * @param id 专长的唯一标识符
     * @param valueType 专长的值类型
     * @param value 专长的默认值
     **/
    private final String id;
    private final String focusId;
    private boolean unlocked;
    private final Class<?> valueType;
    private ExpertiseValue<?> value;
    private static final HashMap<String, Expertise> nameMap = new HashMap<>();
    private static final HashMap<String, List<Expertise>> focusMap = new HashMap<>();
    Expertise(String id, String focusId, boolean unlocked, Class<?> valueType, ExpertiseValue<?> defaultValue) {
        this.id = id;
        this.focusId = focusId;
        this.unlocked = unlocked;
        this.valueType = valueType;
        this.value = defaultValue;
    }

    public String getId() {
        return id;
    }

    public String getFocusId() {
        return focusId;
    }

    public boolean getUnlocked() {
        return unlocked;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public ExpertiseValue<?> getValue() {
        return value;
    }

    public static List<Expertise> getExpertisesByFocusId(String focusId) {
        return focusMap.getOrDefault(focusId, List.of());
    }

    public static Expertise getById(String id) {
        return nameMap.get(id);
    }

    public String getString() {
        return this.id + ";" + this.value.getValue().toString() + ";" + this.unlocked;
    }

    static {
        for (Expertise expertise : Expertise.values()) {
            Expertise.nameMap.put(expertise.getId(), expertise);
            List<Expertise> focusList = Expertise.focusMap.getOrDefault(expertise.getFocusId(), new ArrayList<>());
            focusList.add(expertise);
            Expertise.focusMap.put(expertise.getFocusId(), focusList);
        }
    }


}
