package p1xel.minecraft.bukkit;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import p1xel.minecraft.bukkit.commands.CommandListener;
import p1xel.minecraft.bukkit.commands.TabList;
import p1xel.minecraft.bukkit.listeners.ShopListener;
import p1xel.minecraft.bukkit.listeners.UserCreation;
import p1xel.minecraft.bukkit.managers.*;
import p1xel.minecraft.bukkit.tools.bstats.Metrics;
import p1xel.minecraft.bukkit.tools.spigotmc.UpdateChecker;
import p1xel.minecraft.bukkit.utils.*;
import p1xel.minecraft.bukkit.utils.extensions.Placeholders;
import p1xel.minecraft.bukkit.utils.storage.*;
import p1xel.minecraft.bukkit.utils.storage.backups.BackupCreator;
import p1xel.minecraft.bukkit.utils.storage.cidstorage.CIdData;

import java.util.UUID;

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
        getServer().getPluginCommand("MyCompany").setTabCompleter(new TabList());
        getServer().getPluginManager().registerEvents(new UserCreation(), this);
        getServer().getPluginManager().registerEvents(new ShopListener(), this);

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

        // Backup creator
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID companyUniqueId : cache.getCompanyManager().getAllCompanies()) {
                    BackupCreator.createBackup(companyUniqueId);
                }
            }
        }.runTaskTimer(this, 0L, 20L * 60L * Config.getInt("backup-creator.timer-on-start"));
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

        new Metrics(MyCompany.getInstance(), 26813);

        if (Config.getBool("check-update")) {
            new UpdateChecker(this, 127007).getVersion(version -> {
                if (this.getDescription().getVersion().equals(version)) {
                    getLogger().info(Locale.getMessage("check-update.latest"));
                } else {
                    getLogger().info(Locale.getMessage("check-update.outdate"));
                }
            });
        }
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
