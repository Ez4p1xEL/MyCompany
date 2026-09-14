package p1xel.minecraft.bukkit.utils.prices;

import java.util.HashMap;

public enum PriceGroup {

    NORMAL("normal"),
    ORE_COMPANY("ore-company");

    private final String name;
    private final HashMap<String, PriceGroup> namemap = new HashMap<>();
    PriceGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    static {
        for (PriceGroup group : values()) {
            group.namemap.put(group.getName().toLowerCase(), group);
        }
    }

    public static PriceGroup matchPriceGroup(String name) {
        return NORMAL.namemap.getOrDefault(name.toLowerCase(), NORMAL);
    }

}
