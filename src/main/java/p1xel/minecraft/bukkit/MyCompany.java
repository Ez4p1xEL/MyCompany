package p1xel.minecraft.bukkit;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import p1xel.minecraft.bukkit.commands.CommandListener;
import p1xel.minecraft.bukkit.listeners.UserCreation;
import p1xel.minecraft.bukkit.managers.*;
import p1xel.minecraft.bukkit.utils.*;
import p1xel.minecraft.bukkit.utils.extensions.Placeholders;
import p1xel.minecraft.bukkit.utils.storage.*;
import p1xel.minecraft.bukkit.utils.storage.cidstorage.CIdData;

public class MyCompany extends JavaPlugin {

    private static MyCompany instance;
    private static CacheManager cache;
    private static HireRequestManager request;
    private static Economy econ = null;
    private static TaxCollector tax;

    public static MyCompany getInstance() { return instance;}
    public static CacheManager getCacheManager() { return cache;}
    public static HireRequestManager getHireRequestManager() { return request;}
    public static Economy getEconomy() { return econ;}
    public static TaxCollector getTaxCollector() { return tax;}

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Locale.createLocaleFile();
        CIdData.init();

        request = new HireRequestManager();
        // Check Storage
        boolean setStorage = false;
        String store = getConfig().getString("storage.type");
        if (store.equalsIgnoreCase("YAML")) {
            cache = new CacheManager(new CompanyManager(new YamlCompanyData()), new UserManager(new YamlUserData()));
            setStorage = true;
        }

        if (store.equalsIgnoreCase("MYSQL")) {
            cache = new CacheManager(new CompanyManager(new MysqlCompanyData()), new UserManager(new MysqlUserData()));
            setStorage = true;
        }

        if (!setStorage) {
            getLogger().info("No data storage method was found, please check the config.yml to see if there are any typing errors.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize existed companies
        cache.init();

        getServer().getPluginCommand("MyCompany").setExecutor(new CommandListener());
        getServer().getPluginManager().registerEvents(new UserCreation(), this);

        if (!setupEconomy()) {
            getLogger().warning("Vault is not found! Disabling MyCompany...");
            getServer().getPluginManager().disablePlugin(this);
        }

        tax = new TaxCollector();
        tax.startTask();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(cache).register();
        }

        Logger.setEnabled(Config.getBool("debug"));

        // Text from https://tools.miku.ac/taag/ (Font: Bloody)
        getLogger().info(" ███▄ ▄███▓▓██   ██▓ ▄████▄   ▒█████   ███▄ ▄███▓ ██▓███   ▄▄▄       ███▄    █▓██   ██▓");
        getLogger().info("▓██▒▀█▀ ██▒ ▒██  ██▒▒██▀ ▀█  ▒██▒  ██▒▓██▒▀█▀ ██▒▓██░  ██▒▒████▄     ██ ▀█   █ ▒██  ██▒");
        getLogger().info("▓██    ▓██░  ▒██ ██░▒▓█    ▄ ▒██░  ██▒▓██    ▓██░▓██░ ██▓▒▒██  ▀█▄  ▓██  ▀█ ██▒ ▒██ ██░");
        getLogger().info("▒██    ▒██   ░ ▐██▓░▒▓▓▄ ▄██▒▒██   ██░▒██    ▒██ ▒██▄█▓▒ ▒░██▄▄▄▄██ ▓██▒  ▐▌██▒ ░ ▐██▓░");
        getLogger().info("▒██▒   ░██▒  ░ ██▒▓░▒ ▓███▀ ░░ ████▓▒░▒██▒   ░██▒▒██▒ ░  ░ ▓█   ▓██▒▒██░   ▓██░ ░ ██▒▓░");
        getLogger().info("░ ▒░   ░  ░   ██▒▒▒ ░ ░▒ ▒  ░░ ▒░▒░▒░ ░ ▒░   ░  ░▒▓▒░ ░  ░ ▒▒   ▓▒█░░ ▒░   ▒ ▒   ██▒▒▒ ");
        getLogger().info("░  ░      ░ ▓██ ░▒░   ░  ▒     ░ ▒ ▒░ ░  ░      ░░▒ ░       ▒   ▒▒ ░░ ░░   ░ ▒░▓██ ░▒░ ");
        getLogger().info("░      ░    ▒ ▒ ░░  ░        ░ ░ ░ ▒  ░      ░   ░░         ░   ▒      ░   ░ ░ ▒ ▒ ░░  ");
        getLogger().info("       ░    ░ ░     ░ ░          ░ ░         ░                  ░  ░         ░ ░ ░     ");
        getLogger().info("            ░ ░     ░                                                          ░ ░     ");
        getLogger().info("Plugin is enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }


}
