package p1xel.minecraft.bukkit.manager.gui.position;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUIMain;
import p1xel.minecraft.bukkit.manager.gui.GUITextInput;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.*;

public class GUIPosition extends CompanyMenu {

    public GUIPosition(UUID playerUniqueId, int page) {
        super(playerUniqueId);
        this.page = page;
        this.menu = new Menu(MenuUtil.getMenu("position"));
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

        List<String> positions = new ArrayList<>(companyManager.getPositions(companyUniqueId));

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

        MenuItem positionItem = menu.getItems().get("position").clone();
        menu.getItems().remove("position");

        for (int i = 0; i < position_per_page; i++) {

            // 计算当前行数
            int current_line = (slot / 9)+1;
            // 如果当前 slot 超过了当前行的最后一个 available slot, 则跳到下一行
            if (slot >= (current_line*9)-distance) {
                slot += distance+distance;
            }

            // get position info
            String positionName = positions.get(i);
            String positionLabel = company.getPositionLabel(positionName);
            double positionSalary = company.getPositionSalary(positionName);
            String positionPermission = company.getPositionPermission(positionName).toString().replace("[", "").replace("]", "").replace(", ", ",");

            // replace placeholders

            // Get left and right click messages
            String left_action = "no_permission";
            String shift_left_action = "no_permission";
            String shift_right_action = "no_permission";

            if (positionName.equals("employer")) {
                left_action = "employer";
                shift_left_action = "employer";
                shift_right_action = "employer";
            } else if (positionName.equals("employee")) {
                left_action = "employee";
                shift_left_action = "employee";
                shift_right_action = "employee";
            } else
            {
                if (userManager.hasPermission(playerUniqueId, Permission.POSITION_SETLABEL)) {
                    left_action = "default";
                }

                if (userManager.hasPermission(playerUniqueId, Permission.POSITION_SETPERMISSION)) {
                    shift_left_action = "default";
                }

                if (userManager.hasPermission(playerUniqueId, Permission.POSITION_REMOVE)) {
                    shift_right_action = "default";
                }

            }

            String left_click = menu.getActions().get(left_action).get("left");
            String shift_left_click = menu.getActions().get(shift_left_action).get("shift_left");
            String shift_right_click = menu.getActions().get(shift_right_action).get("shift_right");

            Map<String, String> employeePlaceholders = Map.ofEntries(
                    Map.entry("%position_name%", positionName),
                    Map.entry("%position_label%", positionLabel),
                    Map.entry("%position_salary%", String.valueOf(positionSalary)),
                    Map.entry("%position_permission%", positionPermission),
                    Map.entry("%left_click%", left_click),
                    Map.entry("%shift_left_click%", shift_left_click),
                    Map.entry("%shift_right_click%", shift_right_click)
            );

            MenuItem cloneItem = positionItem.clone();
            cloneItem = replacePlaceholdersInMenuItems(cloneItem, employeePlaceholders);
            cloneItem.setCustomKey("position:"+positionName);
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
        Player player = Bukkit.getPlayer(playerUniqueId);

        return true;
    }

    @Override
    public boolean check(String name, ClickType type) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        if (name.equals("position_info")) {
            return true;
        }
        if (name.equals("next_page")) {
            player.openInventory(new GUIPosition(playerUniqueId, page+1).getInventory());
            return true;
        }
        if (name.equals("previous_page")) {
            player.openInventory(new GUIPosition(playerUniqueId, Math.min(1,page-1)).getInventory());
            return true;
        }

        if (name.equals("back_to_main")) {
            CompanyMenu gui;
            gui = Objects.requireNonNullElseGet(this.previousMenu, () -> new GUIMain(playerUniqueId));
            player.openInventory(gui.getInventory());
            return true;
        }

        if (name.equals("add")) {
            if (!userManager.hasPermission(playerUniqueId, Permission.POSITION_ADD)) {
                return true;
            }
            GUITextInput gui = new GUITextInput(playerUniqueId, "position_add");
            gui.setPreviousMenu(this);
            return true;
        }

        if (name.startsWith("position:")) {
            String[] args = name.split(":");
            String positionName = args[1];
            boolean isEmployer = false;
            boolean isEmployee = false;
            if (positionName.equals("employer")) {
                isEmployer = true;
            } else if (positionName.equals("employee")) {
                isEmployee = true;
            }

            if (type == ClickType.LEFT) {
                if (userManager.hasPermission(playerUniqueId, Permission.POSITION_SETLABEL)) {
                    GUITextInput gui = new GUITextInput(playerUniqueId, "position_setlabel" + ":" + positionName);
                    gui.setPreviousMenu(this);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                }
            } else if (type == ClickType.SHIFT_LEFT) {
                if (isEmployer) { return true; }
                if (userManager.hasPermission(playerUniqueId, Permission.POSITION_SETPERMISSION)) {
                    GUIPermissionList gui = new GUIPermissionList(playerUniqueId, "position_setpermission:" + positionName, !playerUniqueId.equals(company.getEmployer()), 1);
                    gui.setPreviousMenu(this);
                    player.openInventory(gui.getInventory());
                    player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                }
            } else if (type == ClickType.SHIFT_RIGHT) {
                if (isEmployer || isEmployee) { return true; }
                if (userManager.hasPermission(playerUniqueId, Permission.POSITION_REMOVE)) {
                    new PersonalAPI(playerUniqueId).removePosition(positionName);
                    player.openInventory(new GUIPosition(playerUniqueId, 1).getInventory());
                    player.playSound(player, Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                }
            }

            return true;
        }
        return true;
    }
}
