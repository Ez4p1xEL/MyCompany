package p1xel.minecraft.bukkit.manager.gui.position;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUIMain;
import p1xel.minecraft.bukkit.manager.gui.GUITextInput;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.*;
import java.util.stream.Collectors;

public class GUIPositionList extends CompanyMenu {

    private boolean exceptEmployer;

    public GUIPositionList(UUID playerUniqueId, String action, boolean exceptEmployer ,int page) {
        super(playerUniqueId);
        this.page = page;
        this.action = action;
        this.exceptEmployer = exceptEmployer;
        this.menu = new Menu(MenuUtil.getMenu("position_list"));
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

        List<String> positions = new ArrayList<>(company.getPositions());
        if (exceptEmployer) { positions.remove("employer"); }


        int position_per_page = menu.getInt("position-per-page", 10);

        if (positions.size() > page*position_per_page) {
            hasNextPage = true;
        }
        positions = positions.subList(position_per_page * (page-1), Math.min((page) * position_per_page, positions.size()));

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
                Map.entry("%label%", positionLabel),
                Map.entry("%amount%", String.valueOf(company.getPersonnelAmount())),
                Map.entry("%max%", String.valueOf(Config.getInt("company-settings.maximum-players")))
        );


        // 复制 personnel 的 item
        if (positions.size() < position_per_page) {
            position_per_page = positions.size();
        }
        // 当前 personnel 的位置, 再获取它与当前行首个slot的位置的距离
        int current_slot = menu.getItems().get("position").getSlots().getFirst();
        int line = (current_slot / 9)+1;
        int distance = current_slot - ((line-1)*9);

        int slot = current_slot;

        MenuItem personnelItem = menu.getItems().get("position").clone();
        menu.getItems().remove("position");

        for (int i = 0; i < position_per_page; i++) {

            // 计算当前行数
            int current_line = (slot / 9)+1;
            // 如果当前 slot 超过了当前行的最后一个 available slot, 则跳到下一行
            if (slot >= (current_line*9)-distance) {
                slot += distance+distance;
            }

            // get employee info
            String position = positions.get(i);
            String positionLabel = company.getPositionLabel(position);

            // replace placeholders

            // Get left and right click messages

            String act = action.split(":")[0];
            String left_click = menu.getActions().get(act).get("left");
            String right_click = menu.getActions().get(act).get("right");

            Map<String, String> employeePlaceholders = Map.ofEntries(
                    Map.entry("%position_name%", position),
                    Map.entry("%position_label%", positionLabel),
                    Map.entry("%position_permission%", company.getPositionPermission(position).toString()),
                    Map.entry("%left_click%", left_click),
                    Map.entry("%right_click%", right_click)
            );

            MenuItem cloneItem = personnelItem.clone();
            cloneItem = replacePlaceholdersInMenuItems(cloneItem, employeePlaceholders);
            cloneItem.setCustomKey("position:"+position);
            cloneItem.setSlots(Collections.singletonList(slot));
            cloneItem.setId(cloneItem.getId() + "_" + slot);

            menu.getItems().put(cloneItem.getId(), cloneItem);
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
        return true;
    }

    @Override
    public boolean check(String name, ClickType clickType) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        if (name.equalsIgnoreCase("company_info")) {
            return true;
        }
        if (name.equalsIgnoreCase("next_page")) {
            player.openInventory(new GUIPositionList(playerUniqueId, action, exceptEmployer, +1).getInventory());
            return true;
        }
        if (name.equalsIgnoreCase("previous_page")) {
            player.openInventory(new GUIPositionList(playerUniqueId, action, exceptEmployer, Math.min(1,page-1)).getInventory());
            return true;
        }

        if (name.equalsIgnoreCase("back_to_main")) {
            Inventory inv;
            if (previousMenu != null) {
                inv = previousMenu.getInventory();
            } else {
                inv = new GUIMain(playerUniqueId).getInventory();
            }
            player.openInventory(inv);
            return true;
        }

        if (name.startsWith("position:")) {
            String position = name.split(":")[1];
            if (action.equals("position_setlabel")) {
                new GUITextInput(playerUniqueId, action + ":" + position);
                return true;
            }

            if (action.equals("position_remove")) {
                new PersonalAPI(playerUniqueId).removePosition(position);
                player.closeInventory();
                return true;
            }

            if (action.startsWith("set_position:")) {
                UUID targetPlayerUniqueId = UUID.fromString(action.split(":")[1]);
                new PersonalAPI(playerUniqueId).setEmployeePosition(targetPlayerUniqueId, Bukkit.getOfflinePlayer(targetPlayerUniqueId).getName(), position);
                GUIPositionList guiPositionList = new GUIPositionList(playerUniqueId, action, exceptEmployer, page);
                player.openInventory(guiPositionList.getInventory());
                return true;
            }
        }

        return true;
    }

    @Deprecated
    public void update(List<String> positions) {
        String companyName = companyManager.getName(companyUniqueId);
        int slot = 20;
        for (String position : positions) {
            if (slot == 25) {
                slot = 29;
            }

            String material = Locale.getMessage("menu.position-list.items.position.material");

            ItemStack item = new ItemStack(Material.matchMaterial(material));
            ItemMeta meta = item.getItemMeta();
            String label = companyManager.getPositionLabel(companyUniqueId, position);
            meta.setDisplayName(Locale.getMessage("menu.position-list.items.position.display_name").replaceAll("%company%", companyName).replaceAll("%position%", position).replaceAll("%label%", label));
            List<String> lore_list = Locale.yaml.getStringList("menu.position-list.items.position.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line).replaceAll("%position%", position).replaceAll("%label%", label).replaceAll("%permissions%", companyManager.getPositionPermission(companyUniqueId, position).toString()))
                    .collect(Collectors.toList());
            meta.setLore(lore_list);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "position:" + position);
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
                meta.setDisplayName(" ");
                item.setItemMeta(meta);
                inventory.setItem(i, item);
            }
        }
    }
}
