package p1xel.minecraft.bukkit.manager.gui.area;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.manager.area.RentableCompanyArea;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.*;
import java.util.stream.Collectors;

public class GUIAreaRentMarket extends CompanyMenu {

    public GUIAreaRentMarket(UUID playerUniqueId, String action, int page) {
        super(playerUniqueId);
        this.action = action;
        this.page = page;
        this.menu = new Menu(MenuUtil.getMenu("area_rent_market"));
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

        List<RentableCompanyArea> areas = new ArrayList<>(areaManager.getMarketManager().getRentMarketAreas());
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
        menu.getItems().remove("area");

        for (int i = 0; i < area_per_page; i++) {

            // 计算当前行数
            int current_line = (slot / 9)+1;
            // 如果当前 slot 超过了当前行的最后一个 available slot, 则跳到下一行
            if (slot >= (current_line*9)-distance) {
                slot += distance+distance;
            }

            // 复制 area
            MenuItem cloneAreaItem = areaItem.clone();
            cloneAreaItem.setSlots(Collections.singletonList(slot));
            cloneAreaItem.setId(areaItem.getId() + "_" + slot);

            // replace placeholders
            RentableCompanyArea area = areas.get(i);
            String areaName = area.getName();
            String price = String.valueOf(area.getRentPrice());
            UUID areaCompanyUniqueId = area.getCompanyUUID();
            Company company = cache.getCompany(areaCompanyUniqueId);
            String areaCompanyName = company.getName();

            Map<String, String> areaPlaceholders = Map.ofEntries(
                    Map.entry("%company%", areaCompanyName),
                    Map.entry("%price%", price),
                    Map.entry("%area%", areaName)
            );

            cloneAreaItem = replacePlaceholdersInMenuItems(cloneAreaItem, areaPlaceholders);
            cloneAreaItem.setCustomKey("company:"+areaCompanyUniqueId+";area:"+areaName);
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
        if (name.equalsIgnoreCase("area_info")) {
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
            if (action.equalsIgnoreCase("rent")) {
                if (type == ClickType.RIGHT) {
                    new PersonalAPI(playerUniqueId).teleportToLocation(areaCompanyUniqueId, areaName);
                } else if (type == ClickType.LEFT) {
                    if (!userManager.hasPermission(playerUniqueId, Permission.AREA_RENT)) {
                        return false;
                    }
                    if (areaCompanyUniqueId.equals(companyUniqueId)) {
                        new PersonalAPI(playerUniqueId).removeAreaFromMarket(areaCompanyUniqueId, areaName);
                        return false;
                    }
                    new PersonalAPI(playerUniqueId).rentArea(areaCompanyUniqueId, areaName);
                }
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public void update(List<RentableCompanyArea> areas) {
        int slot = 20;
        for (RentableCompanyArea area : areas) {
            if (slot == 25) {
                slot = 29;
            }

            UUID areaCompanyUniqueId = area.getCompanyUUID();
            String companyName = companyManager.getName(areaCompanyUniqueId);
            String areaName = area.getName();

            String material = Locale.getMessage("menu.area-rent-market.items.area.material");
            String[] args = material.split(";");
            material = args[0];

            ItemStack item = new ItemStack(Material.matchMaterial(material));
            ItemMeta meta = item.getItemMeta();
            //String position = userManager.getPosition(employeeUniqueId);
            meta.setDisplayName(Locale.getMessage("menu.area-rent-market.items.area.display_name").replaceAll("%company%", companyName).replaceAll("%area%", areaName));
            List<String> lore_list = Locale.yaml.getStringList("menu.area-rent-market.items.area.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .map(line -> line.replaceAll("%company%", companyName)
                    .replaceAll("%area%", areaName)
                    .replaceAll("%price%", String.valueOf(area.getRentPrice())))
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
