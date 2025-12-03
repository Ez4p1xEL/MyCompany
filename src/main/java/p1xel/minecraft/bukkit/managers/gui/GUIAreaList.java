package p1xel.minecraft.bukkit.managers.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.managers.areas.CompanyArea;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.Logger;
import p1xel.minecraft.bukkit.utils.permissions.Permission;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class GUIAreaList extends GUIAbstract implements InventoryHolder {

    private UUID playerUniqueId;
    private String action;
    private int page;
    private boolean hasCompany;
    private UUID companyUniqueId;
    private Inventory inventory;
    private HashMap<UUID, List<String>> rented;

    public GUIAreaList(UUID playerUniqueId, String action, int page) {
        this.playerUniqueId = playerUniqueId;
        this.action = action;
        this.page = page;
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (companyUniqueId != null) {
            this.companyUniqueId = companyUniqueId;
            hasCompany = true;
            rented = areaManager.getAreasRented(companyUniqueId);
            init();
        }
    }

    @Override
    public void init() {
        String companyName = companyManager.getName(companyUniqueId);
        Inventory inventory = Bukkit.createInventory(this, 45, Locale.getMessage("menu.area-list.title").replaceAll("%company%", companyName));
        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (page > 1) {
            hasPreviousPage = true;
        }

        List<String> areas = new ArrayList<>(areaManager.getAreas(companyUniqueId));

        boolean addRentAreas = action.equalsIgnoreCase("list");

        if (addRentAreas) {
            if (!rented.isEmpty()) {
                for (UUID renter : rented.keySet()) {
                    areas.addAll(List.copyOf(rented.get(renter)));
                }
            }
        }

        if (areas.size() >= page*14) {
            hasNextPage = true;
        }
        areas.subList(14 * (page-1), Math.min((page) * 14, areas.size()));

        ItemStack area_info = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.area-list.items.area_info.material")), 1);
        ItemMeta area_info_meta = area_info.getItemMeta();
        area_info_meta.setDisplayName(Locale.getMessage("menu.area-list.items.area_info.display_name").replaceAll("%company%", companyName));
        List<String> area_info_lore = Locale.yaml.getStringList("menu.area-list.items.area_info.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .map(line -> line.replaceAll("%company%", companyName))
                .collect(Collectors.toList());
        area_info_meta.setLore(area_info_lore);
        PersistentDataContainer company_info_container = area_info_meta.getPersistentDataContainer();
        company_info_container.set(menu_id_key, PersistentDataType.STRING, "area_info");
        area_info.setItemMeta(area_info_meta);
        inventory.setItem(4, area_info);

        if (hasNextPage) {

            ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.area-list.items.next_page.material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Locale.getMessage("menu.area-list.items.next_page.display_name"));
            List<String> lore = Locale.yaml.getStringList("menu.area-list.items.next_page.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "next_page");
            item.setItemMeta(meta);
            inventory.setItem(5, item);
        }

        if (hasPreviousPage) {

            ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.area-list.items.previous_page.material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Locale.getMessage("menu.area-list.items.previous_page.display_name"));
            List<String> lore = Locale.yaml.getStringList("menu.area-list.items.previous_page.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "previous_page");
            item.setItemMeta(meta);
            inventory.setItem(3, item);
        }

        ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.area-list.items.back_to_main.material")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Locale.getMessage("menu.area-list.items.back_to_main.display_name"));
        List<String> lore = Locale.yaml.getStringList("menu.area-list.items.back_to_main.lore").stream()
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
            player.openInventory(new GUIAreaList(playerUniqueId, action, page+1).getInventory());
            return true;
        }
        if (name.equalsIgnoreCase("previous_page")) {
            player.openInventory(new GUIAreaList(playerUniqueId, action, Math.min(1,page-1)).getInventory());
            return true;
        }

        if (name.equalsIgnoreCase("back_to_main")) {
            player.openInventory(new GUICompanyArea(playerUniqueId).getInventory());
            return true;
        }
        if (name.startsWith("company:")) {
            String[] args = name.split(";");
            UUID areaCompanyUniqueId = UUID.fromString(args[0].split(":")[1]);
            String areaName = args[1].split(":")[1];
            if (action.equalsIgnoreCase("list")) {
                if (type == ClickType.LEFT) {
                    new PersonalAPI(playerUniqueId).teleportToLocation(areaCompanyUniqueId, areaName);
                } else if (type == ClickType.RIGHT) {
                    new PersonalAPI(playerUniqueId).setAreaLocation(areaName, player.getLocation());
                }
                return false;
            }

            if (action.equalsIgnoreCase("delete")) {
                new PersonalAPI(playerUniqueId).deleteArea(areaCompanyUniqueId, areaName);
                player.closeInventory();
            }

            if (action.equalsIgnoreCase("rent")) {
                if (!userManager.hasPermission(playerUniqueId, Permission.AREA_MARKET_RENT)) {
                    return false;
                }
                new GUITextInput(playerUniqueId, "area-rent-set-price:" + areaName);
            }

            if (action.equalsIgnoreCase("sell")) {
                if (!userManager.hasPermission(playerUniqueId, Permission.AREA_MARKET_SELL)) {
                    return false;
                }
                new GUITextInput(playerUniqueId, "area-sell-set-price:" + areaName);
            }

//            if (action.equalsIgnoreCase("rent-market")) {
//                new GUITextInput(playerUniqueId, "area-rent" + areaName);
//            }
        }
        return true;
    }

    public void update(List<String> areas) {
        int slot = 20;
        for (String area : areas) {
            if (slot == 25) {
                slot = 29;
            }

            String menu_item = "area";
            UUID ownerCompanyUniqueId = null;
            long endTime;
            String left_click = "";
            String right_click = "";

            left_click = Locale.getMessage("menu.area-list.actions." + action + ".left");
            right_click = Locale.getMessage("menu.area-list.actions." + action + ".right");

            if (area.startsWith("rent#")) {
                menu_item = "area-rent";
                for (UUID uuid : rented.keySet()) {
                    if (rented.get(uuid).contains(area)) {
                        ownerCompanyUniqueId = uuid;
                        break;
                    }
                }
                area = area.substring(5);
                endTime = areaManager.getRentEndTime(companyUniqueId, ownerCompanyUniqueId, area);
            } else {
                endTime = 0L;
                ownerCompanyUniqueId = companyUniqueId;
            }

            // Format the end time
            LocalDateTime dateTime = Instant.ofEpochMilli(endTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Config.getString("company-settings.time-format"));
            String formatted_endtime = dateTime.format(formatter);

            String companyName = companyManager.getName(ownerCompanyUniqueId);
            String material = Locale.getMessage("menu.area-list.items." + menu_item + ".material");
            String[] args = material.split(";");
            material = args[0];

            ItemStack item = new ItemStack(Material.matchMaterial(material));
            ItemMeta meta = item.getItemMeta();
            //String position = userManager.getPosition(employeeUniqueId);
            meta.setDisplayName(Locale.getMessage("menu.area-list.items." + menu_item + ".display_name").replaceAll("%company%", companyName).replaceAll("%area%", area));
            String finalArea = area;
            CompanyArea companyArea = new CompanyArea(ownerCompanyUniqueId, area);
            Location firstBlock = companyArea.getFirst();
            Location secondBlock = companyArea.getSecond();
            String finalLeft_click = left_click;
            String finalRight_click = right_click;
            List<String> lore_list = Locale.yaml.getStringList("menu.area-list.items." + menu_item + ".lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .map(line -> line.replaceAll("%company%", companyName)
                            .replaceAll("%world%",firstBlock.getWorld().getName())
                            .replaceAll("%x1%",String.valueOf(firstBlock.getX()))
                            .replaceAll("%y1%",String.valueOf(firstBlock.getY()))
                            .replaceAll("%z1%",String.valueOf(firstBlock.getZ()))
                            .replaceAll("%x2%",String.valueOf(secondBlock.getX()))
                            .replaceAll("%y2%",String.valueOf(secondBlock.getY()))
                            .replaceAll("%z2%",String.valueOf(secondBlock.getZ()))
                            .replaceAll("%area%", finalArea)
                            .replaceAll("%end_time%", formatted_endtime)
                            .replaceAll("%left_click%", finalLeft_click)
                            .replaceAll("%right_click%", finalRight_click))
                    .filter(line -> !line.contains("#remove#"))
                    .collect(Collectors.toList());
            meta.setLore(lore_list);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "company:" + ownerCompanyUniqueId + ";area:"+area);
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
