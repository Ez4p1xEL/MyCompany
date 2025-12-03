package p1xel.minecraft.bukkit.managers.gui;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.managers.areas.RentableCompanyArea;
import p1xel.minecraft.bukkit.managers.areas.TradableCompanyArea;
import p1xel.minecraft.bukkit.utils.permissions.Permission;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GUIAreaSaleMarket extends GUIAbstract implements InventoryHolder {

    private UUID playerUniqueId;
    private String action;
    private int page;
    private boolean hasCompany;
    private UUID companyUniqueId;
    private Inventory inventory;

    public GUIAreaSaleMarket(UUID playerUniqueId, String action, int page) {
        this.playerUniqueId = playerUniqueId;
        this.action = action;
        this.page = page;
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (companyUniqueId != null) {
            this.companyUniqueId = companyUniqueId;
            hasCompany = true;
            init();
        }
    }

    // Shit Mountain Code
    @Override
    public void init() {
        String companyName = companyManager.getName(companyUniqueId);
        Inventory inventory = Bukkit.createInventory(this, 45, Locale.getMessage("menu.area-sale-market.title").replaceAll("%company%", companyName));
        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (page > 1) {
            hasPreviousPage = true;
        }

        List<TradableCompanyArea> areas = new ArrayList<>(areaManager.getMarketManager().getTradeMarketAreas());

        if (areas.size() >= page*14) {
            hasNextPage = true;
        }
        areas.subList(14 * (page-1), Math.min((page) * 14, areas.size()));

        ItemStack area_info = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.area-sale-market.items.area_info.material")), 1);
        ItemMeta area_info_meta = area_info.getItemMeta();
        area_info_meta.setDisplayName(Locale.getMessage("menu.area-sale-market.items.area_info.display_name").replaceAll("%company%", companyName));
        List<String> area_info_lore = Locale.yaml.getStringList("menu.area-sale-market.items.area_info.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .map(line -> line.replaceAll("%company%", companyName))
                .collect(Collectors.toList());
        area_info_meta.setLore(area_info_lore);
        PersistentDataContainer company_info_container = area_info_meta.getPersistentDataContainer();
        company_info_container.set(menu_id_key, PersistentDataType.STRING, "area_info");
        area_info.setItemMeta(area_info_meta);
        inventory.setItem(4, area_info);

        if (hasNextPage) {

            ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.area-sale-market.items.next_page.material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Locale.getMessage("menu.area-sale-market.items.next_page.display_name"));
            List<String> lore = Locale.yaml.getStringList("menu.area-sale-market.items.next_page.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "next_page");
            item.setItemMeta(meta);
            inventory.setItem(5, item);
        }

        if (hasPreviousPage) {

            ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.area-sale-market.items.previous_page.material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Locale.getMessage("menu.area-sale-market.items.previous_page.display_name"));
            List<String> lore = Locale.yaml.getStringList("menu.area-sale-market.items.previous_page.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "previous_page");
            item.setItemMeta(meta);
            inventory.setItem(3, item);
        }

        ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.area-sale-market.items.back_to_main.material")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Locale.getMessage("menu.area-sale-market.items.back_to_main.display_name"));
        List<String> lore = Locale.yaml.getStringList("menu.area-sale-market.items.back_to_main.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(menu_id_key, PersistentDataType.STRING, "back_to_main");
        item.setItemMeta(meta);
        inventory.setItem(36, item);

        this.inventory = inventory;
        update(areas);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean check(String name) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        return true;
    }

    public boolean check(String name, ClickType type) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        if (name.equalsIgnoreCase("company_info")) {
            return true;
        }
        if (name.equalsIgnoreCase("next_page")) {
            player.openInventory(new GUIAreaRentMarket(playerUniqueId, action, page+1).getInventory());
            return true;
        }
        if (name.equalsIgnoreCase("previous_page")) {
            player.openInventory(new GUIAreaRentMarket(playerUniqueId, action, Math.min(1,page-1)).getInventory());
            return true;
        }

        if (name.equalsIgnoreCase("back_to_main")) {
            player.openInventory(new GUIAreaTradeMarket(playerUniqueId).getInventory());
            return true;
        }

        if (name.startsWith("company:")) {
            String[] args = name.split(";");
            UUID areaCompanyUniqueId = UUID.fromString(args[0].split(":")[1]);
            String areaName = args[1].split(":")[1];
            if (action.equalsIgnoreCase("sell")) {
                if (type == ClickType.RIGHT) {
                    new PersonalAPI(playerUniqueId).teleportToLocation(areaCompanyUniqueId, areaName);
                } else if (type == ClickType.LEFT) {
                    if (!userManager.hasPermission(playerUniqueId, Permission.AREA_BUY)) {
                        return false;
                    }
                    if (areaCompanyUniqueId.equals(companyUniqueId)) {
                        new PersonalAPI(playerUniqueId).removeAreaFromMarket(areaCompanyUniqueId, areaName);
                        return false;
                    }
                    new PersonalAPI(playerUniqueId).buyArea(areaCompanyUniqueId, areaName);
                }
                return false;
            }
        }
        return true;
    }

    public void update(List<TradableCompanyArea> areas) {
        int slot = 20;
        for (TradableCompanyArea area : areas) {
            if (slot == 25) {
                slot = 29;
            }

            UUID areaCompanyUniqueId = area.getCompanyUUID();
            String companyName = companyManager.getName(areaCompanyUniqueId);
            String areaName = area.getName();

            String material = Locale.getMessage("menu.area-sale-market.items.area.material");
            String[] args = material.split(";");
            material = args[0];

            ItemStack item = new ItemStack(Material.matchMaterial(material));
            ItemMeta meta = item.getItemMeta();
            //String position = userManager.getPosition(employeeUniqueId);
            meta.setDisplayName(Locale.getMessage("menu.area-sale-market.items.area.display_name").replaceAll("%company%", companyName).replaceAll("%area%", areaName));
            List<String> lore_list = Locale.yaml.getStringList("menu.area-sale-market.items.area.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .map(line -> line.replaceAll("%company%", companyName)
                            .replaceAll("%area%", areaName)
                            .replaceAll("%price%", String.valueOf(area.getSellPrice())))
                    .collect(Collectors.toList());
            meta.setLore(lore_list);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "company:" + areaCompanyUniqueId + ";area:"+areaName);
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
            slot++;
            if (slot >= 36) {
                break;
            }

        }

        for (int i = 0; i < 9*5; i++) {
            if (inventory.getItem(i) == null) {
                ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("");
                item.setItemMeta(meta);
                inventory.setItem(i, item);
            }
        }
    }

    public String getAction() {
        return action;
    }
}
