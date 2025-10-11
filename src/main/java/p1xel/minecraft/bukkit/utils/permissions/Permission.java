package p1xel.minecraft.bukkit.utils.permissions;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;

public enum Permission {

    ALL("ALL"),
    EMPLOY("EMPLOY"),
    FIRE("FIRE"),
    CHESTSHOP_CREATE("CHESTSHOP_CREATE"),
    CHESTSHOP_DELETE("CHESTSHOP_DELETE"),
    SET_POSITION("SET_POSITION"),
    POSITION_ADD("POSITION_ADD"),
    POSITION_REMOVE("POSITION_REMOVE"),
    POSITION_SETLABEL("POSITION_SETLABEL"),
    AREA_CREATE("AREA_CREATE"),
    AREA_DELETE("AREA_DELETE"),
    AREA_SETLOC("AREA_SETLOC");

    private String name;
    private final static HashMap<String, Permission> namemap = new HashMap<>();
    private Permission(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public static Permission matchPermission(String name) {
        return namemap.get(name.toUpperCase());
    }

    public static Collection<Permission> getAll() {
        return namemap.values();
    }

    public static Collection<String> getAllInString() {
        return namemap.keySet();
    }

    static {
        for (Permission perm : values()) {

            namemap.put(perm.getName(), perm);
        }
    }
}
