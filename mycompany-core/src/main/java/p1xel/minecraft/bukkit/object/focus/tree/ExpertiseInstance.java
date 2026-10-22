package p1xel.minecraft.bukkit.object.focus.tree;

import p1xel.minecraft.bukkit.util.Logger;

import java.util.logging.Level;

public class ExpertiseInstance {

    private String expertiseId;
    private String focusId;
    private boolean enabled;
    private boolean unlocked;
    private ExpertiseValue<?> value;
    private Class<?> valueType;

    public ExpertiseInstance(String expertiseId, String focusId, boolean enabled, boolean unlocked, ExpertiseValue<?> value) {
        this.expertiseId = expertiseId;
        this.focusId = focusId;
        this.enabled = enabled;
        this.unlocked = unlocked;
        this.valueType = value.getType();
        this.value = value;
    }

    public ExpertiseInstance(DefaultExpertiseInstance defaultExpertiseInstance) {
        this.expertiseId = defaultExpertiseInstance.getId();
        this.focusId = defaultExpertiseInstance.getFocusId();
        this.enabled = defaultExpertiseInstance.getEnabled();
        this.unlocked = defaultExpertiseInstance.getUnlocked();
        this.valueType = defaultExpertiseInstance.getValueType();
        this.value = defaultExpertiseInstance.getValue();
    }

    public String getId() {
        return expertiseId;
    }

    public String getFocusId() {
        return focusId;
    }

    public boolean getEnabled() { return enabled; }

    public boolean getUnlocked() {
        return unlocked;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public ExpertiseValue<?> getValue() {
        return value;
    }

    public String getString() {
        return this.expertiseId + ";" + this.value.getValue().toString() + ";" + this.unlocked+";" + this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean setValue(ExpertiseValue<?> newValue) {
        if (!newValue.getId().equals(this.expertiseId)) {
            Logger.log(Level.WARNING, "Attempted to set value for expertise '" + this.expertiseId + "' with a value that has a different id: '" + newValue.getId() + "'");
            return false;
        }
        if (!newValue.getType().equals(this.valueType)) {
            Logger.log(Level.WARNING, "Attempted to set value for expertise '" + this.expertiseId + "' with a value that has a different type: '" + newValue.getType().getName() + "'");
            return false;
        }

        this.value = newValue;
        return true;
    }

    public void setByString(String string) {
        String[] split = string.split(";");
        String id = split[0];
        String stringValue = split[1];
        String unlockedString = split[2];
        String enabledString = split[3];

        if (!id.equals(this.expertiseId)) {
            Logger.log(Level.WARNING, "Attempted to set value for expertise '" + this.expertiseId + "' with a value that has a different id: '" + id + "'");
            return;
        }

        if (this.valueType == Boolean.class) {
            boolean boolValue = Boolean.parseBoolean(stringValue);
            this.setValue(ExpertiseValue.ofBool(id, boolValue));
        } else if (this.valueType == Integer.class) {
            int intValue = Integer.parseInt(stringValue);
            this.setValue(ExpertiseValue.ofInt(id, intValue));
        } else if (this.valueType == Double.class) {
            double doubleValue = Double.parseDouble(stringValue);
            this.setValue(ExpertiseValue.ofDouble(id, doubleValue));
        } else if (this.valueType == String.class) {
            this.setValue(ExpertiseValue.ofString(id, stringValue));
        }

        this.setUnlocked(Boolean.parseBoolean(unlockedString));
        this.setEnabled(Boolean.parseBoolean(enabledString));
    }


}
