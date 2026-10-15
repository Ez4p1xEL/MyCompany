package p1xel.minecraft.bukkit.util.permission;

import p1xel.minecraft.bukkit.util.storage.Locale;

import java.util.HashMap;
import java.util.List;

public class PermissionUtil {

    public static final HashMap<Permission, String> LABELS = new HashMap<>();
    public static final HashMap<Permission, List<String>> DESCRIPTIONS = new HashMap<>();
    public static String STATUS_YES = "YES";
    public static String STATUS_NO = "NO";

    // Should be called after initializing Locale
    public static void savePermissionLocale() {
        for (Permission permission : Permission.getAll()) {
            String label = "position-permission.permission." + permission.getName() + ".label";
            String description = "position-permission.permission." + permission.getName() + ".description";
            LABELS.put(permission, Locale.yaml.getString(label, "No Label"));
            DESCRIPTIONS.put(permission, Locale.yaml.getStringList(description));
        }

        String statusPath = "position-permission.status.";
        STATUS_YES = Locale.yaml.getString(statusPath + "enabled", "YES");
        STATUS_NO = Locale.yaml.getString(statusPath + "disabled", "NO");
    }

    public static String getStatus(boolean status) {
        return status ? STATUS_YES : STATUS_NO;
    }

}
