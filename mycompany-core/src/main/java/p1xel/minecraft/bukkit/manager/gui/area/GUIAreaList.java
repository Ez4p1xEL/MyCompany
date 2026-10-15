package p1xel.minecraft.bukkit.manager.gui.area;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.manager.area.CompanyArea;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUITextInput;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GUIAreaList extends CompanyMenu {

    private final HashMap<UUID, List<String>> rented = areaManager.getAreasRented(companyUniqueId);

    public GUIAreaList(UUID playerUniqueId, String action, int page) {
        super(playerUniqueId);
        this.action = action;
        this.page = page;
        this.menu = new Menu(MenuUtil.getMenu("area_list"));
        if (hasCompany) {
            init();
        }
    }

    @Override
    public void init() {
        Inventory inventory = Bukkit.createInventory(this, menu.getSize(), menu.getTitle().replace("%company%", companyName));

        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (page > 1) {
            hasPreviousPage = true;
        }

        List<String> areas = new ArrayList<>(areaManager.getAreas(companyUniqueId));

        boolean addRentAreas = action.equals("list");

        if (addRentAreas) {
            if (!rented.isEmpty()) {
                for (UUID renter : rented.keySet()) {
                    areas.addAll(List.copyOf(rented.get(renter)));
                }
            }
        }

        int area_per_page = menu.getInt("area-per-page", 10);

        if (areas.size() > page*area_per_page) {
            hasNextPage = true;
        }
        areas = areas.subList(area_per_page * (page-1), Math.min((page) * area_per_page, areas.size()));

        // 显示previous_page 和 next_page按钮
        if (!hasPreviousPage) {
            menu.getItems().remove("previous_page");
        }
        if (!hasNextPage) {
            menu.getItems().remove("next_page");
        }

        Map<String, String> placeholders = Map.ofEntries(
                Map.entry("%player%", playerName),
                Map.entry("%position%", position),
                Map.entry("%label%", positionLabel)
        );


        // 复制 area 的 item
        if (areas.size() < area_per_page) {
            area_per_page = areas.size();
        }
        // 当前 area 的位置, 再获取它与当前行首个slot的位置的距离
        int current_slot = menu.getItems().get("area").getSlots().getFirst();
        int line = (current_slot / 9)+1;
        int distance = current_slot - ((line-1)*9);

        int slot = current_slot;

        MenuItem areaItem = menu.getItems().get("area").clone();
        MenuItem rentAreaItem = menu.getItems().get("area_rent");
        menu.getItems().remove("area");
        menu.getItems().remove("area_rent");

        for (int i = 0; i < area_per_page; i++) {

            // 计算当前行数
            int current_line = (slot / 9)+1;
            // 如果当前 slot 超过了当前行的最后一个 available slot, 则跳到下一行
            if (slot >= (current_line*9)-distance) {
                slot += distance+distance;
            }

            // 复制 area
            MenuItem cloneAreaItem;
            boolean isRentArea;
            UUID ownerCompanyUniqueId = companyUniqueId;
            long endTime = 0L;

            String area = areas.get(i);
            if (area.startsWith("rent#")) {
                cloneAreaItem= areaItem.clone();
                isRentArea = true;
                // 获取领地原主人公司
                for (UUID uuid : rented.keySet()) {
                    if (rented.get(uuid).contains(area)) {
                        ownerCompanyUniqueId = uuid;
                        break;
                    }
                }
                area = area.substring(5);
                endTime = areaManager.getRentEndTime(companyUniqueId, ownerCompanyUniqueId, area);
            } else {
                cloneAreaItem = rentAreaItem.clone();
            }

            cloneAreaItem.setSlots(Collections.singletonList(slot));
            cloneAreaItem.setId(areaItem.getId() + "_" + slot);

            // replace placeholders

            CompanyArea companyArea = new CompanyArea(ownerCompanyUniqueId, area);
            Company areaCompany = cache.getCompany(ownerCompanyUniqueId);
            String areaCompanyName = areaCompany.getName();
            Location firstBlock = companyArea.getFirst();
            Location secondBlock = companyArea.getSecond();

            // Format the end time
            LocalDateTime dateTime = Instant.ofEpochMilli(endTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Config.getString("company-settings.time-format"));
            String formatted_endtime = dateTime.format(formatter);

            // Get left and right click messages
            String left_click = menu.getActions().get(action).get("left");
            String right_click = menu.getActions().get(action).get("right");

            Map<String, String> areaPlaceholders = Map.ofEntries(
                    Map.entry("%company%", areaCompanyName),
                    Map.entry("%area%", area),
                    Map.entry("%world%", firstBlock.getWorld().getName()),
                    Map.entry("%x1%", String.valueOf(firstBlock.getBlockX())),
                    Map.entry("%y1%", String.valueOf(firstBlock.getBlockY())),
                    Map.entry("%z1%", String.valueOf(firstBlock.getBlockZ())),
                    Map.entry("%x2%", String.valueOf(secondBlock.getBlockX())),
                    Map.entry("%y2%", String.valueOf(secondBlock.getBlockY())),
                    Map.entry("%z2%", String.valueOf(secondBlock.getBlockZ())),
                    Map.entry("%end_time%", formatted_endtime),
                    Map.entry("%left_click%", left_click),
                    Map.entry("%right_click%", right_click)
            );

            cloneAreaItem = replacePlaceholdersInMenuItems(cloneAreaItem, areaPlaceholders);
            cloneAreaItem.setCustomKey("company:"+ownerCompanyUniqueId+";area:"+area);
            menu.getItems().put(cloneAreaItem.getId(), cloneAreaItem);
            slot++;

        }

        inventory = setItems(inventory, this.menu, placeholders);
        inventory = fillEmptySlot(inventory, this.menu);

        this.inventory = inventory;

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

    @Override
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

    @Deprecated
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
