package p1xel.minecraft.bukkit.object.focus.tree;

import p1xel.minecraft.bukkit.util.Logger;

import java.util.logging.Level;

public enum Expertise {

    empty("empty", "normal", false, Boolean.class, ExpertiseValue.ofBool("empty", false)),
    more_drops_level1("more_drops_level1", "mob", false, Boolean.class ,ExpertiseValue.ofBool("more_drops_level1", false)),;

    /**
     * @description: 专精树中每个专长
     * @param id 专长的唯一标识符
     * @param valueType 专长的值类型
     * @param value 专长的默认值 (每次初始化时需要从配置文件中读取实际值)
     **/
    private final String id;
    private final String focusId;
    private boolean unlocked;
    private final Class<?> valueType;
    private ExpertiseValue<?> value;
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

    public boolean isUnlocked() {
        return unlocked;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public ExpertiseValue<?> getValue() {
        return value;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean setValue(ExpertiseValue<?> newValue) {
        if (!newValue.getId().equals(this.id)) {
            Logger.log(Level.WARNING, "Attempted to set value for expertise '" + this.id + "' with a value that has a different id: '" + newValue.getId() + "'");
            return false;
        }
        if (!newValue.getType().equals(this.valueType)) {
            Logger.log(Level.WARNING, "Attempted to set value for expertise '" + this.id + "' with a value that has a different type: '" + newValue.getType().getName() + "'");
            return false;
        }

        this.value = newValue;
        return true;
    }

}
