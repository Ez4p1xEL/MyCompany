package p1xel.minecraft.bukkit.utils.permissions;

import java.util.HashMap;

public enum Permission {

    ALL("ALL"),
    EMPLOY("EMPLOY"),
    FIRE("FIRE"),
    CHESTSHOP_CREATE("CHESTSHOP_CREATE"),
    CHESTSHOP_DELETE("CHESTSHOP_DELETE");

    private String name;
    private final static HashMap<String, Permission> namemap = new HashMap<>();
    private Permission(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Permission matchPermission(String name) {
        return namemap.get(name.toUpperCase());
    }

    static {
        for (Permission perm : values()) {

            namemap.put(perm.getName(), perm);
        }
    }
}
