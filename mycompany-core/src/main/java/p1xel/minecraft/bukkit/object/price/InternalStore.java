package p1xel.minecraft.bukkit.object.price;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.util.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;

public class InternalStore {

    private File file;
    private FileConfiguration yaml;
    private final HashMap<PriceGroup, Double> multipliers = new HashMap<>();
    private final HashMap<String, InternalPrice> priceMap = new HashMap<>();
    private final HashMap<Material, InternalPrice> vanillaItemPriceMap = new HashMap<>();
    private double playerShare = 0.0;

    public InternalStore() {
        init();
    }

    public void init() {

        File file = new File(MyCompany.getInstance().getDataFolder(), "prices.yml");

        if (!file.exists()) {
            MyCompany.getInstance().saveResource("prices.yml",    false);
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        this.file = file;
        this.yaml = yaml;

        saveMultipliers(); // 保存PriceGroup的价格倍数到multipliers中
        savePrices(); // 保存商品价格到priceMap中
        this.playerShare = yaml.getDouble("player-share", 0.0); // 保存玩家分成比例

        System.out.println(vanillaItemPriceMap);
    }

    public void saveMultipliers() {
        for (PriceGroup group : PriceGroup.values()) {
            double multiplier = yaml.getDouble("multipliers.company."+ group.getName());
            multipliers.put(group, multiplier);
        }
    }

    public void savePrices() {
        // 保存VanillaItem
        ConfigurationSection vanillaSection = yaml.getConfigurationSection("prices.vanilla");
        if (vanillaSection != null && !vanillaSection.getKeys(false).isEmpty()) {
            for (String id : vanillaSection.getKeys(false)) {
                Material material = Material.matchMaterial(vanillaSection.getString(id + ".material", ""));
                if (material == null) {
                    Logger.log(Level.WARNING, "Invalid material for vanilla item in prices.yml: " + id);
                    continue;
                }

                double price = vanillaSection.getDouble(id + ".price", 0.0);
                if (price <= 0.0) {
                    Logger.log(Level.WARNING, "Invalid price for vanilla item in prices.yml: " + id);
                    continue;
                }

                String groupName = vanillaSection.getString(id + ".group", PriceGroup.NORMAL.getName()).toLowerCase();
                PriceGroup priceGroup = PriceGroup.matchPriceGroup(groupName);

                // 由于是VanillaItem原版物品，所以直接新建ItemStack即可
                ItemStack itemStack = ItemStack.of(material);

                InternalPrice internalPrice = new InternalPrice(id, true, itemStack, price, priceGroup);
                priceMap.put(id, internalPrice);
                vanillaItemPriceMap.put(material, internalPrice);
            }
        }
    }

    public HashMap<String, InternalPrice> getPriceMap() {
        return priceMap;
    }

    public HashMap<PriceGroup, Double> getMultipliers() {
        return multipliers;
    }

    public HashMap<Material, InternalPrice> getVanillaItemPriceMap() {
        return vanillaItemPriceMap;
    }

    public double getPlayerShare() {
        return playerShare;
    }

}
