package p1xel.minecraft.bukkit.utils.permissions;

import java.util.HashMap;

public enum Permission {

    EMPLOY("employ"),
    FIRE("fire"),
    CHESTSHOP_CREATE("chestshop_create"),
    CHESTSHOP_DELETE("chestshop_delete");

    private String name;
    private final static HashMap<String, Permission> namemap = new HashMap<>();
    private Permission(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Permission matchPermission(String name) {
        return namemap.get(name);
    }

    static {
        for (Permission perm : values()) {

            namemap.put(perm.getName(), perm);
        }
    }
}
