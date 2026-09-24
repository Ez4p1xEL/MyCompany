package p1xel.minecraft.bukkit;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import p1xel.minecraft.bukkit.command.CommandListener;
import p1xel.minecraft.bukkit.command.TabList;
import p1xel.minecraft.bukkit.listener.*;
import p1xel.minecraft.bukkit.manager.*;
import p1xel.minecraft.bukkit.manager.area.AreaSelectionMode;
import p1xel.minecraft.bukkit.manager.building.DefaultBuildingArea;
import p1xel.minecraft.bukkit.manager.building.DisabledBuildingArea;
import p1xel.minecraft.bukkit.manager.building.DominionBuildingArea;
import p1xel.minecraft.bukkit.manager.building.ResidenceBuildingArea;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseListenerRegistry;
import p1xel.minecraft.bukkit.tool.bstats.Metrics;
import p1xel.minecraft.bukkit.tool.spigotmc.UpdateChecker;
import p1xel.minecraft.bukkit.util.*;
import p1xel.minecraft.bukkit.util.extension.Placeholders;
import p1xel.minecraft.bukkit.util.storage.*;
import p1xel.minecraft.bukkit.util.storage.backup.BackupCreator;
import p1xel.minecraft.bukkit.util.storage.cidstorage.CIdData;
import p1xel.minecraft.bukkit.util.storage.driver.CompanyYamlDriver;
import p1xel.minecraft.bukkit.util.storage.driver.UserYamlDriver;
import p1xel.minecraft.bukkit.util.storage.menu.MenuConfig;

import java.util.UUID;
import java.util.logging.Level;

public class MyCompany extends JavaPlugin {

    private static MyCompany instance;
    private static CacheManager cache;
    private static HireRequestManager request;
    private static Economy econ = null;
    private static TaxCollector tax;
    private AreaProtector areaProtector;
    private static int[] version;

    public static MyCompany getInstance() { return instance;}
    public static CacheManager getCacheManager() { return cache;}
    public static HireRequestManager getHireRequestManager() { return request;}
    public static Economy getEconomy() { return econ;}
    public static TaxCollector getTaxCollector() { return tax;}
    private static ExpertiseListenerRegistry expertiseListenerRegistry;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        String[] stringVersion = getServer().getBukkitVersion().split("-")[0].split("\\.");

        // --- 修复版本号解析越界问题 ---
        int major = Integer.parseInt(stringVersion[0]);
        int minor = Integer.parseInt(stringVersion[1]);
        // 兼容新版 Paper 的 26.2.build.84-stable 格式，非数字补丁版本默认为 0
        int patch = stringVersion.length > 2 && stringVersion[2].matches("\\d+") ? Integer.parseInt(stringVersion[2]) : 0;
        version = new int[]{major, minor, patch};
        // ------------------------------

        // 加载 ColorUtil (Hex color support for 1.16.1+)
        //if (((version[0] == 1 && version[1] > 16) || (version[0] == 1 && version[1] == 16 && version[2] >= 1)) || version[0] >= 26) {
        ColorUtil.hexColorEnabled = true;
        //}

        MenuConfig.initialization();
        Locale.createLocaleFile();
        CIdData.init();

        request = new HireRequestManager();
        // Check Storage
        boolean setStorage = false;
        String store = getConfig().getString("storage.type");
        if (store.equalsIgnoreCase("YAML")) {
            cache = new CacheManager(new CompanyManager(new CompanyData(new CompanyYamlDriver(this))), new UserManager(new UserData(new UserYamlDriver(this))));
            setStorage = true;
        }

//        if (store.equalsIgnoreCase("MYSQL")) {
//            cache = new CacheManager(new CompanyManager(new MysqlCompanyData()), new UserManager(new MysqlUserData()));
//            setStorage = true;
//        }

        if (!setStorage) {
            getLogger().info("No data storage method was found, please check the config.yml to see if there are any typing errors.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize existed companies
        cache.init();
        // Initialize selection tool item
        AreaSelectionMode.initTool();

        getServer().getPluginCommand("MyCompany").setExecutor(new CommandListener());
        getServer().getPluginCommand("MyCompany").setTabCompleter(new TabList());
        getServer().getPluginManager().registerEvents(new UserCreation(), this);
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new OrderListener(), this);
        getServer().getPluginManager().registerEvents(new AreaSelection(), this);

        if (Config.getBool("company-area.protection.enable")) {
            areaProtector = new AreaProtector();
            getServer().getPluginManager().registerEvents(areaProtector, this);
            areaProtector.init();
        }

        if (!setupEconomy()) {
            getLogger().warning("Vault is not found! Disabling MyCompany...");
            getServer().getPluginManager().disablePlugin(this);
        }

        tax = new TaxCollector();
        tax.startTask();

        // Focus 专精
        // 专长事件监听注册器
        expertiseListenerRegistry = new ExpertiseListenerRegistry(this);
        expertiseListenerRegistry.init();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(cache).register();
        }

        if (getConfig().getBoolean("company-settings.dedicated-company-building.enable")) {
            boolean useDefault = true;
            for (String plugin : getConfig().getConfigurationSection("company-settings.dedicated-company-building.supported-plugins").getKeys(false)) {

                if (plugin.equalsIgnoreCase("Residence")) {
                    boolean enable = getConfig().getBoolean("company-settings.dedicated-company-building.supported-plugins." + plugin);
                    if (enable) {
                        if (getServer().getPluginManager().getPlugin("Residence") != null) {
                            cache.getBuildingManager().setModule(new ResidenceBuildingArea());
                            useDefault = false;
                            break;
                        }
                    }
                }

                if (plugin.equalsIgnoreCase("Dominion")) {
                    boolean enable = getConfig().getBoolean("company-settings.dedicated-company-building.supported-plugins." + plugin);
                    if (enable) {
                        if (getServer().getPluginManager().getPlugin("Dominion") != null) {
                            cache.getBuildingManager().setModule(new DominionBuildingArea());
                            useDefault = false;
                            break;
                        }
                    }
                }

            }

            if (useDefault) {
                cache.getBuildingManager().setModule(new DefaultBuildingArea());
            }
        } else {
            cache.getBuildingManager().setModule(new DisabledBuildingArea());
        }

        // After all managers are ready
        EmployeeOrders.init();
        // Saving interval
        new BukkitRunnable() {
            @Override
            public void run() {
                Logger.debug(Level.INFO, "Start to save cached progress of orders...");
                EmployeeOrders.saveCacheToLocal();
                Logger.debug(Level.INFO, "Saved successfully!");
            }
        }.runTaskTimer(this, 0L, 20L * 30);

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

    public AreaProtector getAreaProtector() { return areaProtector; }
    public void setAreaProtector(AreaProtector areaProtector) { this.areaProtector = areaProtector; }


}
