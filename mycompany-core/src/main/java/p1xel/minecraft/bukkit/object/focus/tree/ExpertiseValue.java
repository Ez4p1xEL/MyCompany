package p1xel.minecraft.bukkit.object.focus.tree;

public final class ExpertiseValue<T> {

    private final String id;
    private final Class<T> type;
    private final T value;

    private ExpertiseValue(String id, Class<T> type, T value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public static ExpertiseValue<Boolean> ofBool(String id, boolean def) {
        return new ExpertiseValue<>(id, Boolean.class, def);
    }

    public static ExpertiseValue<Double> ofDouble(String id, double def) {
        return new ExpertiseValue<>(id, Double.class, def);
    }

    public static ExpertiseValue<Integer> ofInt(String id, int def) {
        return new ExpertiseValue<>(id, Integer.class, def);
    }

    public static ExpertiseValue<String> ofString(String id, String def) {
        return new ExpertiseValue<>(id, String.class, def);
    }

    public String getId() { return id; }
    public Class<T> getType() { return type; }
    public T getValue() { return value; }

}
