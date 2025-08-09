package p1xel.minecraft.bukkit.utils;

import p1xel.minecraft.bukkit.MyCompany;

import java.util.logging.Level;

public class Logger {

    // Debug Tool, Default for true
    private static boolean enabled = true;

    public static void setEnabled(boolean bool) {
        enabled = bool;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void log(Level level, String message) {
        MyCompany.getInstance().getServer().getLogger().log(level, "[MyCompany] " + message);
    }

    public static void debug(Level level, String message) {
        if (enabled) {
            MyCompany.getInstance().getServer().getLogger().log(level, "[MyCompany_DEBUG] " + message);
        }
    }

}
