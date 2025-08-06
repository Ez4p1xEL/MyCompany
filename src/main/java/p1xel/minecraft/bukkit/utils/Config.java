package p1xel.minecraft.bukkit.utils;


import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.List;

public class Config {

    public static String getString(String path) {
        return MyCompany.getInstance().getConfig().getString(path);
    }

    public static boolean getBool(String path) {
        return MyCompany.getInstance().getConfig().getBoolean(path);
    }

    public static String getLanguage() {
        return MyCompany.getInstance().getConfig().getString("language");
    }

    public static void reloadConfig() {

        MyCompany.getInstance().reloadConfig();
        //config = NoBuildPlus.getInstance().getConfig();
        MyCompany.getCacheManager().init();
        Locale.createLocaleFile();
        MyCompany.getTaxCollector().cancelTask();
        MyCompany.getTaxCollector().startTask();
        Logger.setEnabled(Config.getBool("debug"));

    }

    public static String getVersion() {
        return MyCompany.getInstance().getDescription().getVersion();
    }

    public static int getConfigurationVersion() {return getInt("configuration");}

    public static int getInt(String path) {
        return MyCompany.getInstance().getConfig().getInt(path);
    }

    public static double getDouble(String path) { return MyCompany.getInstance().getConfig().getDouble(path);}

    public static List<String> getStringList(String path) { return MyCompany.getInstance().getConfig().getStringList(path);}

}
