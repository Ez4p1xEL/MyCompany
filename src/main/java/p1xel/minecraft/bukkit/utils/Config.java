package p1xel.minecraft.bukkit.utils;


import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.listeners.AreaProtector;
import p1xel.minecraft.bukkit.managers.areas.AreaSelectionMode;
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

        MyCompany plugin = MyCompany.getInstance();

        plugin.reloadConfig();
        //config = NoBuildPlus.getInstance().getConfig();
        MyCompany.getCacheManager().init();
        Locale.createLocaleFile();
        AreaSelectionMode.initTool();
        MyCompany.getTaxCollector().cancelTask();
        MyCompany.getTaxCollector().startTask();
        Logger.setEnabled(Config.getBool("debug"));

        AreaProtector existed = plugin.getAreaProtector();
        if (Config.getBool("company-area.protection.enable")) {
            if (existed == null) {
                AreaProtector areaProtector = new AreaProtector();
                plugin.getServer().getPluginManager().registerEvents(areaProtector, plugin);
                plugin.setAreaProtector(areaProtector);
                areaProtector.init();
                return;
            }
            return;
        } else {
            if (existed != null) {
                HandlerList.unregisterAll(existed);
                plugin.setAreaProtector(null);
                return;
            }
        }

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

    public static double getTaxRate(String tax, String phase) {
        Configuration config = MyCompany.getInstance().getConfig();
        return config.getDouble("company-funds.cost-per-day." + tax +".phases." + phase + ".tax-rate", config.getDouble("company-funds.cost-per-day." + tax + ".default-tax-rate"));
    }

    public static ConfigurationSection getConfigurationSection(String path) { return MyCompany.getInstance().getConfig().getConfigurationSection(path);}

}
