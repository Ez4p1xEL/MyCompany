package p1xel.minecraft.bukkit.manager.gui.player;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUIMain;
import p1xel.minecraft.bukkit.manager.gui.position.GUIPositionList;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.*;

public class GUIPersonnel extends CompanyMenu {

    public GUIPersonnel(UUID playerUniqueId, int page) {
        super(playerUniqueId);
        this.page = page;
        this.menu = new Menu(MenuUtil.getMenu("personnel"));
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

        List<UUID> uuids = new ArrayList<>();
        uuids.add(company.getEmployer());
        for (String position : companyManager.getPositions(companyUniqueId)) {
            uuids.addAll(companyManager.getEmployeeList(companyUniqueId, position));
        }


        int personnel_per_page = menu.getInt("personnel-per-page", 10);

        if (uuids.size() > page*personnel_per_page) {
            hasNextPage = true;
        }
        uuids = uuids.subList(personnel_per_page * (page-1), Math.min((page) * personnel_per_page, uuids.size()));

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
        if (uuids.size() < personnel_per_page) {
            personnel_per_page = uuids.size();
        }
        // 当前 personnel 的位置, 再获取它与当前行首个slot的位置的距离
        int current_slot = menu.getItems().get("personnel").getSlots().getFirst();
        int line = (current_slot / 9)+1;
        int distance = current_slot - ((line-1)*9);

        int slot = current_slot;

        MenuItem personnelItem = menu.getItems().get("personnel").clone();
        menu.getItems().remove("personnel");

        for (int i = 0; i < personnel_per_page; i++) {

            // 计算当前行数
            int current_line = (slot / 9)+1;
            // 如果当前 slot 超过了当前行的最后一个 available slot, 则跳到下一行
            if (slot >= (current_line*9)-distance) {
                slot += distance+distance;
            }

            // get employee info
            UUID employeeUniqueId = uuids.get(i);
            User employee = userManager.getUser(employeeUniqueId);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(employeeUniqueId);
            String employeeName = offlinePlayer.getName();
            String employeePosition = employee.getPosition();
            String employeePositionLabel = company.getPositionLabel(employeePosition);
            double employeeSalary = company.getPositionSalary(employeePosition);

            // replace placeholders

            // Get left and right click messages
            String left_action = "no_permission";
            String right_action = "no_permission";

            if (employeeUniqueId.equals(playerUniqueId)) {
                left_action = "self";
                right_action = "self";
            } else
            {
                if (userManager.hasPermission(playerUniqueId, Permission.SET_POSITION)) {
                    left_action = "default";
                }

                if (userManager.hasPermission(playerUniqueId, Permission.FIRE)) {
                    right_action = "default";
                }

            }

            String left_click = menu.getActions().get(left_action).get("left");
            String shift_right_click = menu.getActions().get(right_action).get("shift_right");

            Map<String, String> employeePlaceholders = Map.ofEntries(
                    Map.entry("%personnel_player%", employeeName),
                    Map.entry("%personnel_label%", employeePositionLabel),
                    Map.entry("%personnel_position%", employeePosition),
                    Map.entry("%personnel_salary%", String.valueOf(employeeSalary)),
                    Map.entry("%left_click%", left_click),
                    Map.entry("%shift_right_click%", shift_right_click)
            );

            MenuItem cloneItem = personnelItem.clone();
            cloneItem = replacePlaceholdersInMenuItems(cloneItem, employeePlaceholders);
            cloneItem.setCustomKey("personnel:"+employeeUniqueId);
            cloneItem.setSlots(Collections.singletonList(slot));
            cloneItem.setId(cloneItem.getId() + "_" + slot);

            // 如果Material String使用的是对应玩家的头颅
            // 则在此进行修改头颅皮肤
            String materialString = cloneItem.getMaterialString();
            if (materialString.equalsIgnoreCase("PLAYER_HEAD;%PLAYER%")) {
                materialString = materialString.toUpperCase();
                materialString = materialString.replace("%PLAYER%", employeeName);
                cloneItem.setMaterialString(materialString);
            }

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
        if (name.equals("personnel_info")) {
            return true;
        }
        if (name.equals("next_page")) {
            player.openInventory(new GUIPersonnel(playerUniqueId, page+1).getInventory());
            return true;
        }
        if (name.equals("previous_page")) {
            player.openInventory(new GUIPersonnel(playerUniqueId, Math.min(1,page-1)).getInventory());
            return true;
        }

        if (name.equals("back_to_main")) {
            player.openInventory(new GUIMain(playerUniqueId).getInventory());
            return true;
        }

        if (name.equals("employ")) {
            if (!userManager.hasPermission(playerUniqueId, Permission.EMPLOY)) {
                return true;
            }
            player.openInventory(new GUIPlayerList(playerUniqueId, "employ", 1).getInventory());
            return true;
        }

        if (name.startsWith("personnel:")) {
            String[] args = name.split(":");
            UUID employeeUniqueId = UUID.fromString(args[1]);
            if (employeeUniqueId.equals(playerUniqueId)) {
                return true;
            }

            if (type == ClickType.LEFT) {
                if (userManager.hasPermission(playerUniqueId, Permission.SET_POSITION)) {
                    GUIPositionList positionList = new GUIPositionList(playerUniqueId, "set_position:" + employeeUniqueId, true, 1);
                    positionList.setPreviousMenu(this);
                    player.openInventory(positionList.getInventory());
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                } else {
                    return true;
                }
            } else if (type == ClickType.SHIFT_RIGHT) {
                if (userManager.hasPermission(playerUniqueId, Permission.FIRE)) {
                    companyManager.dismissEmployee(companyUniqueId, employeeUniqueId);
                    OfflinePlayer target = Bukkit.getOfflinePlayer(employeeUniqueId);
                    player.sendMessage(Locale.getMessage("fire-success").replace("%player%", target.getName()));
                    if (target.isOnline()) {
                        Player onlineTarget = (Player) target;
                        String companyName = MyCompany.getCacheManager().getCompanyManager().getName(companyUniqueId);
                        onlineTarget.sendMessage(Locale.getMessage("dismissed").replace("%player%", player.getName()).replace("%company%", companyName));
                        onlineTarget.playSound(onlineTarget, Sound.ENTITY_VILLAGER_NO, 3f, 3f);
                    }
                    player.openInventory(new GUIPersonnel(playerUniqueId, page).getInventory());
                    return true;
                }
                return true;
            }
        }
        return true;
    }
}
