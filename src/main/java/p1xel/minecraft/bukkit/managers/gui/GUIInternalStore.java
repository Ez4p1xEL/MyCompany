package p1xel.minecraft.bukkit.managers.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.events.CompanyIncomeEvent;
import p1xel.minecraft.bukkit.utils.ColorUtil;
import p1xel.minecraft.bukkit.utils.prices.InternalPrice;
import p1xel.minecraft.bukkit.utils.prices.PriceGroup;
import p1xel.minecraft.bukkit.utils.storage.Locale;
import p1xel.minecraft.bukkit.utils.storage.menu.MenuConfig;

import java.util.*;
import java.util.stream.Collectors;

public class GUIInternalStore extends GUIAbstract implements InventoryHolder {

    private UUID playerUniqueId;
    private boolean hasCompany;
    private UUID companyUniqueId;
    private String position;
    private Inventory inventory;
    private double price;
    private double playerShare;
    private List<Integer> sellableSlots = new ArrayList<>();

    public GUIInternalStore(UUID playerUniqueId) {
        this.playerUniqueId = playerUniqueId;
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (companyUniqueId != null) {
            this.companyUniqueId = companyUniqueId;
            hasCompany = true;
            price = 0.0;
            init();
        }
    }

    @Override
    public void init() {

        this.inventory = Bukkit.createInventory(this, 54, Locale.getMessage("menu.internal-store.title"));

        setItem(Material.PAPER, "submit", 49);

        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                Material empty_slot = Material.matchMaterial(MenuConfig.GLOBAL_EMPTY_SLOT);
                ItemStack item = new ItemStack(empty_slot != null ? empty_slot : Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(" ");
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(menu_id_key, PersistentDataType.STRING, "empty_slot");
                item.setItemMeta(meta);
                inventory.setItem(i, item);
            }
        }

    }

    private void setItem(Material material, String menu_id, int slot) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String display_name = null;
        List<String> lore = new ArrayList<>();

        switch (menu_id) {

            case "back_to_main": {

                display_name = Locale.getMessage("menu.internal-store.items." + menu_id + ".display_name");
                lore = Locale.yaml.getStringList("menu.internal-store.items." + menu_id + ".lore").stream()
                        .map(ColorUtil::translateHexColorCodes).collect(Collectors.toList());
                break;
            }

            case "submit": {
                display_name = Locale.getMessage("menu.internal-store.items." + menu_id + ".display_name");
                lore = Locale.yaml.getStringList("menu.internal-store.items." + menu_id + ".lore").stream()
                        .map(ColorUtil::translateHexColorCodes)
                        .map(line -> line.replaceAll("%price%", String.valueOf(price)))
                        .map(line -> line.replaceAll("%share%", String.valueOf(playerShare)))
                        .collect(Collectors.toList());
                break;
            }
        }

        meta.setDisplayName(display_name);
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(menu_id_key, PersistentDataType.STRING, menu_id);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    public void updatePrice() {
        ItemStack[] contents = inventory.getContents();
        HashMap<Material, InternalPrice> vanillaItemPriceMap = internalStore.getVanillaItemPriceMap();
        HashMap<PriceGroup, Double> priceGroupMap = internalStore.getMultipliers();
        double totalPrice = 0.0;
        double playerPrice = 0.0;
        int slot = -1;
        for (ItemStack item : contents) {
            slot++;
            if (item != null && item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                // Vanilla Item
                if (item.hasItemMeta()) {
                    continue;
                }

                if (vanillaItemPriceMap.containsKey(item.getType())) {
                    int amount = item.getAmount();
                    InternalPrice internalPrice = vanillaItemPriceMap.get(item.getType());
                    PriceGroup priceGroup = internalPrice.getPriceGroup();
                    if (!companyManager.getPriceGroup(companyUniqueId).equals(priceGroup.getName())) {
                        priceGroup = PriceGroup.NORMAL;
                    }
                    double productPrice = internalPrice.getPrice() * priceGroupMap.getOrDefault(priceGroup, 1.0) * amount;
                    double playerShare = productPrice * internalStore.getPlayerShare();
                    totalPrice += productPrice - playerShare;
                    playerPrice += playerShare;
                    sellableSlots.add(slot);
                    continue;
                }

            }
        }
        this.price = totalPrice;
        this.playerShare = playerPrice;
        setItem(Material.PAPER, "submit", 49);
    }

    public void sellItems() {

        // 在出售前再最后更新一次价格
        updatePrice();

        if (price <= 0.0) {
            return;
        }

        for (int slot : sellableSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(slot, null);
            }
        }

        // 触发公司收入事件
        CompanyIncomeEvent event = new CompanyIncomeEvent(companyUniqueId, price);
        Bukkit.getPluginManager().callEvent(event);
        final double finalPrice = event.getAmount();

        companyManager.giveMoney(companyUniqueId, finalPrice);

        // 个人分红
        if (playerShare > 0) {
            MyCompany.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(playerUniqueId), playerShare);
        }

        // 完成后清空
        updatePrice();

    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public boolean check(String name) {

        return false;
    }

    public boolean check(Player player, String name) {
        switch (name) {
            case "submit": {
                sellItems();
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
                return true;
            }
            case "empty_slot": {
                return true;
            }
        }

        return false;
    }
}
