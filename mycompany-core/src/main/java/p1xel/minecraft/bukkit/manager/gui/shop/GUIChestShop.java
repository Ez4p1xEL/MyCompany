package p1xel.minecraft.bukkit.manager.gui.shop;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUIMain;
import p1xel.minecraft.bukkit.manager.gui.GUITextInput;
import p1xel.minecraft.bukkit.manager.gui.area.GUIAreaList;
import p1xel.minecraft.bukkit.manager.gui.area.GUIAreaTradeMarket;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.permission.PermissionUtil;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.Map;
import java.util.UUID;

public class GUIChestShop extends CompanyMenu {

    public GUIChestShop(UUID playerUniqueId) {
        super(playerUniqueId);
        this.menu = MenuUtil.getMenu("chestshop");
        if (hasCompany) {
            init();
        }
    }

    @Override
    public void init() {

        Inventory inventory = Bukkit.createInventory(this, menu.getSize(), menu.getTitle().replace("%company%", companyName));

        Permission shop_create = Permission.CHESTSHOP_CREATE;
        Permission shop_delete = Permission.CHESTSHOP_DELETE;
        boolean hasCreatePermission = userManager.hasPermission(playerUniqueId, shop_create);
        boolean hasDeletePermission = userManager.hasPermission(playerUniqueId, shop_delete);
        String create_status = PermissionUtil.getStatus(hasCreatePermission);
        String delete_status = PermissionUtil.getStatus(hasDeletePermission);

        Map<String, String> placeholders = Map.ofEntries(
                Map.entry("%player%", playerName),
                Map.entry("%company%", companyName),
                Map.entry("%position%", position),
                Map.entry("%label%", positionLabel),
                Map.entry("%chestshop_identifier%", Config.getString("chest-shop.sign-create.identifier")),
                Map.entry("%permission_create_id%", shop_create.getName()),
                Map.entry("%permission_delete_id%", shop_delete.getName()),
                Map.entry("%permission_create_status%", create_status),
                Map.entry("%permission_delete_status%", delete_status)
        );

        inventory = setItems(inventory, menu, placeholders);

        // for each slot in the inventory
        inventory = fillEmptySlot(inventory, menu);

        this.inventory = inventory;
    }

    @Override
    public boolean check(String name, ClickType clickType) {
        switch (name) {
            case "back_to_main": {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 1);
                player.openInventory(new GUIMain(playerUniqueId).getInventory());
                return true;
            }

            default: {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            }
        }

        return true;
    }
}
