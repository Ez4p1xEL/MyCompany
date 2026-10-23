package p1xel.minecraft.bukkit.manager.gui.store;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.event.CompanyIncomeEvent;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUIMain;
import p1xel.minecraft.bukkit.object.price.PriceGroup;
import p1xel.minecraft.bukkit.util.ColorUtil;
import p1xel.minecraft.bukkit.object.price.InternalPrice;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.*;
import java.util.stream.Collectors;

public class GUIInternalStore extends CompanyMenu {

    private double price;
    private double playerShare;
    private final List<Integer> sellableSlots = new ArrayList<>();
    private final Menu menu;

    private String companyName;
    private String position;
    private Map<String, String> placeholders;

    public GUIInternalStore(UUID playerUniqueId) {
        super(playerUniqueId);
        this.menu = MenuUtil.getMenu("internal_store");
        if (hasCompany) {
            price = 0.0;
            init();
        }
    }

    @Override
    public void init() {

        this.companyName = company.getName();
        this.position = user.getPosition();

        Inventory inventory = Bukkit.createInventory(this, menu.getSize(), menu.getTitle().replace("%company%", companyName));

        this.placeholders = Map.ofEntries(
                Map.entry("%player%", playerName),
                Map.entry("%company%", companyName),
                Map.entry("%position%", position),
                Map.entry("%money%", String.valueOf(price)),
                Map.entry("%price%", String.valueOf(playerShare))
        );

        inventory = setItems(inventory, menu, placeholders);

        // for each slot in the inventory
        inventory = fillEmptySlot(inventory, menu);

        this.inventory = inventory;
    }

    @Deprecated
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
                        .map(line -> line.replaceAll("%money%", String.valueOf(price)))
                        .map(line -> line.replaceAll("%price%", String.valueOf(playerShare)))
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
        sellableSlots.clear();
        ItemStack[] contents = inventory.getContents();
        HashMap<Material, InternalPrice> vanillaItemPriceMap = internalStore.getVanillaItemPriceMap();
        HashMap<String, PriceGroup> priceGroupMap = internalStore.getPriceGroupMap();
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
                    if (!companyManager.getPriceGroup(companyUniqueId).equals(priceGroup.getId())) {
                        priceGroup = priceGroupMap.get("normal");
                    }
                    double productPrice = internalPrice.getPrice() * priceGroup.getPriceMultiplier() * amount;
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

        // refresh the inventory to update the price display
        this.inventory = setItems(this.inventory, menu, placeholders);
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

    @Override
    public boolean check(String name, ClickType clickType) {
        return false;
    }

    public boolean check(Player player, String name) {
        switch (name) {
            case "submit": {
                sellItems();
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
                return true;
            }
            case "bar": {
                player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
                return true;
            }

            case "back_to_main": {
                Inventory newInv = new GUIMain(playerUniqueId).getInventory();
                player.openInventory(newInv);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
                return true;
            }
        }

        return false;
    }

    public void updateMenu() {
        this.placeholders = Map.ofEntries(
                Map.entry("%player%", playerName),
                Map.entry("%company%", companyName),
                Map.entry("%position%", position),
                Map.entry("%money%", String.valueOf(price)),
                Map.entry("%price%", String.valueOf(playerShare))
        );
        inventory = setItems(inventory, menu, placeholders);
    }
}
