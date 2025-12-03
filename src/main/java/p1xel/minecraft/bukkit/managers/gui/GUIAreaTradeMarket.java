package p1xel.minecraft.bukkit.managers.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GUIAreaTradeMarket extends GUIAbstract implements InventoryHolder {

    private UUID playerUniqueId;
    private UUID companyUniqueId;
    private boolean hasCompany;
    private Inventory inventory;

    public GUIAreaTradeMarket(UUID playerUniqueId) {
        this.playerUniqueId = playerUniqueId;
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (companyUniqueId != null) {
            this.companyUniqueId = companyUniqueId;
            hasCompany = true;
            init();
        }
    }

    @Override
    public void init() {
        Inventory inventory = Bukkit.createInventory(this, 36, Locale.getMessage("menu.area-trade-market.title"));

        for (String item_name : Locale.yaml.getConfigurationSection("menu.area-trade-market.items").getKeys(false)) {

            String display_name = Locale.getMessage("menu.area-trade-market.items." + item_name + ".display_name");
            List<String> loreList = Locale.yaml.getStringList("menu.area-trade-market.items." + item_name + ".lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());

            String material = Locale.getMessage("menu.area-trade-market.items." + item_name + ".material");
            String[] args = material.split(";");
            material = args[0];

            ItemStack item = new ItemStack(Material.matchMaterial(material));
            ItemMeta meta = item.getItemMeta();
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("player_head")) {
                    SkullMeta skullMeta = (SkullMeta) meta;
                    skullMeta.setOwner(Bukkit.getOfflinePlayer(playerUniqueId).getName());
                }
            }
            meta.setDisplayName(display_name);
            meta.setLore(loreList);

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, item_name);
            item.setItemMeta(meta);

            int slot = 0;
            switch (item_name) {
                case "rent":
                    slot = 12;
                    break;
                case "sell":
                    slot = 14;
                    break;
                case "back_to_main":
                    slot = 31;
                    break;
            }

            inventory.setItem(slot, item);

        }

        for (int i = 0; i < 9*4; i++) {
            if (inventory.getItem(i) == null) {
                ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("");
                item.setItemMeta(meta);
                inventory.setItem(i, item);
            }
        }

        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean check(String name) {
        Player player = Bukkit.getPlayer(playerUniqueId);

        if (name.equalsIgnoreCase("back_to_main")) {
            player.openInventory(new GUICompanyArea(playerUniqueId).getInventory());
            return true;
        }

        if (name.equalsIgnoreCase("rent")) {
            player.openInventory(new GUIAreaRentMarket(playerUniqueId, "rent", 1).getInventory());
        }

        if (name.equalsIgnoreCase("sell")) {
            player.openInventory(new GUIAreaSaleMarket(playerUniqueId, "sell", 1).getInventory());
        }
        return true;
    }
}
