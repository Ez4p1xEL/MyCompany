package p1xel.minecraft.bukkit.manager.gui.position;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUIMain;
import p1xel.minecraft.bukkit.manager.gui.GUITextInput;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.permission.PermissionUtil;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.*;
import java.util.stream.Collectors;

public class GUIPermissionList extends CompanyMenu {

    private boolean exceptALL;
    private String operatedPosition = "";
    private HashSet<Permission> enabledPermissions = new HashSet<>();

    public GUIPermissionList(UUID playerUniqueId, String action, boolean exceptALL , int page) {
        super(playerUniqueId);
        this.page = page;
        this.action = action;
        this.exceptALL = exceptALL;
        this.menu = new Menu(MenuUtil.getMenu("position_permission_list"));
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

        List<Permission> permissions = List.of(Permission.values());

        if (exceptALL) {
            permissions = permissions.stream().filter(permission -> !permission.equals(Permission.ALL)).collect(Collectors.toList());
        }
        int permission_per_page = menu.getInt("permission-per-page", 10);

        if (permissions.size() > page*permission_per_page) {
            hasNextPage = true;
        }
        permissions = permissions.subList(permission_per_page * (page-1), Math.min((page) * permission_per_page, permissions.size()));

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

        // 获取当前操作的职位及其权限
        String[] split = action.split(":");
        this.operatedPosition = split[1];
        this.enabledPermissions = company.getPositionPermission(this.operatedPosition);


        // 复制 personnel 的 item
        if (permissions.size() < permission_per_page) {
            permission_per_page = permissions.size();
        }
        // 当前 personnel 的位置, 再获取它与当前行首个slot的位置的距离
        int current_slot = menu.getItems().get("permission").getSlots().getFirst();
        int line = (current_slot / 9)+1;
        int distance = current_slot - ((line-1)*9);

        int slot = current_slot;

        MenuItem permissionItem = menu.getItems().get("permission").clone();
        menu.getItems().remove("permission");

        for (int i = 0; i < permission_per_page; i++) {

            // 计算当前行数
            int current_line = (slot / 9)+1;
            // 如果当前 slot 超过了当前行的最后一个 available slot, 则跳到下一行
            if (slot >= (current_line*9)-distance) {
                slot += distance+distance;
            }

            // get employee info
            Permission permission = permissions.get(i);
            String permissionId = permission.getName();
            String permissionLabel = PermissionUtil.LABELS.get(permission);
            List<String> permissionDescription = PermissionUtil.DESCRIPTIONS.get(permission);
            String permissionDescriptionString = String.join("\n", permissionDescription);
            boolean isPermissionEnabled = enabledPermissions.contains(permission);
            String permissionStatus = PermissionUtil.getStatus(isPermissionEnabled);

            // replace placeholders
            Map<String, String> employeePlaceholders = Map.ofEntries(
                    Map.entry("%position_permission_id%", permissionId),
                    Map.entry("%position_permission_label%", permissionLabel),
                    Map.entry("%position_permission_description%", permissionDescriptionString),
                    Map.entry("%position_permission_status%", permissionStatus)
            );

            MenuItem cloneItem = permissionItem.clone();

            // 如果有单独设置权限显示的物品Id
            MenuItem permissionItemFromMenu = menu.getItems().get("permission_"+permissionId.toLowerCase());
            if (permissionItemFromMenu != null) {
                cloneItem.setMaterial(permissionItemFromMenu.getMaterial());
                // 移除原有的permissionItemFromMenu, 避免重复显示
                menu.getItems().remove("permission_"+permissionId.toLowerCase());
            }

            // 如果该权限已启用, 则给物品添加附魔效果
            if (isPermissionEnabled) {
                cloneItem.getEnchantments().put(Enchantment.UNBREAKING, 1);
                cloneItem.getItemFlags().add(ItemFlag.HIDE_ENCHANTS);
            }

            cloneItem = replacePlaceholdersInMenuItems(cloneItem, employeePlaceholders);
            cloneItem.setCustomKey("permission:"+permissionId);
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
    public boolean check(String name) {
        return true;
    }

    @Override
    public boolean check(String name, ClickType clickType) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        if (name.equalsIgnoreCase("permission_info")) {
            return true;
        }
        if (name.equalsIgnoreCase("next_page")) {
            player.openInventory(new GUIPermissionList(playerUniqueId, action, exceptALL, page+1).getInventory());
            return true;
        }
        if (name.equalsIgnoreCase("previous_page")) {
            player.openInventory(new GUIPermissionList(playerUniqueId, action, exceptALL, Math.min(1,page-1)).getInventory());
            return true;
        }

        if (name.equalsIgnoreCase("back_to_main")) {
            Inventory inv;
            if (previousMenu != null) {
                inv = previousMenu.getInventory();
            } else {
                inv = new GUIPosition(playerUniqueId, 1).getInventory();
            }
            player.openInventory(inv);
            return true;
        }

        if (name.startsWith("permission:")) {
            String permissionId = name.split(":")[1];
            Permission permission = Permission.matchPermission(permissionId);
            if (action.startsWith("position_setpermission:")) {
                String position = action.split(":")[1];

                if (enabledPermissions.contains(permission)) {
                    companyManager.removePositionPermission(companyUniqueId, position, permission);
                } else {
                    companyManager.addPositionPermission(companyUniqueId, position, permission);
                }

                player.openInventory(new GUIPermissionList(playerUniqueId, action, exceptALL, page).getInventory());
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                return true;
            }
        }

        // TODO: Edit permission

        return true;
    }
}
