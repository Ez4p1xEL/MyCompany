package p1xel.minecraft.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.gui.*;
import p1xel.minecraft.bukkit.manager.gui.area.*;
import p1xel.minecraft.bukkit.manager.gui.order.GUIDailyOrder;
import p1xel.minecraft.bukkit.manager.gui.player.GUIEmployeeList;
import p1xel.minecraft.bukkit.manager.gui.player.GUIPersonnel;
import p1xel.minecraft.bukkit.manager.gui.player.GUIPlayerList;
import p1xel.minecraft.bukkit.manager.gui.position.GUIPermissionList;
import p1xel.minecraft.bukkit.manager.gui.position.GUIPosition;
import p1xel.minecraft.bukkit.manager.gui.position.GUIPositionList;
import p1xel.minecraft.bukkit.manager.gui.shop.GUIChestShop;
import p1xel.minecraft.bukkit.manager.gui.GUIConfirmation;
import p1xel.minecraft.bukkit.manager.gui.store.GUIInternalStore;

public class GUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) {
            return;
        }
        InventoryHolder holder = inventory.getHolder();
        if (holder == null) {
            return;
        }

        if (!(holder instanceof CompanyMenu)) {
            return;
        }

        HumanEntity entity = event.getWhoClicked();

        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        NamespacedKey menu_id_key = new NamespacedKey("mycompany", "menu_id");

        ItemStack item = inventory.getItem(event.getSlot());
        if (!item.hasItemMeta()) {
            return;
        }
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (!container.has(menu_id_key, PersistentDataType.STRING)) {
            return;
        }
        String menuId = container.get(menu_id_key, PersistentDataType.STRING);
        if (menuId == null || menuId.isEmpty()) {
            return;
        }
        ClickType clickType = event.getClick();

        if (holder instanceof GUIFound gui) {
            if (!gui.check(menuId)) {
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIMain gui) {
            if (!gui.check(menuId, clickType)) {

            }
            event.setCancelled(true);
            return;
        }

        // TODO: remaster GUIEmployeeList
        if (holder instanceof GUIEmployeeList gui) {
            if (!gui.check(menuId)) {
                player.closeInventory();
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIPlayerList gui) {
            if (!gui.check(menuId)) {
                player.closeInventory();
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIPositionList gui) {
            if (!gui.check(menuId, clickType)) {
                player.closeInventory();
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIDailyOrder gui) {
            if (!gui.check(menuId)) {
                player.closeInventory();
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUICompanyArea gui) {
            if (!gui.check(menuId)) {
                player.closeInventory();
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIAreaList gui) {
            if (!gui.check(menuId, clickType)) {
                player.closeInventory();
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIAreaTradeMarket gui) {
            if (!gui.check(menuId)) {
                player.closeInventory();
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIAreaRentMarket gui) {
            if (!gui.check(menuId, clickType)) {
                player.closeInventory();
            }
            event.setCancelled(true);
            return;
        }


        if (holder instanceof GUIAreaSaleMarket gui) {
            if (!gui.check(menuId, clickType)) {
                player.closeInventory();
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIInternalStore gui) {
            if (gui.check(player, menuId)) {
                event.setCancelled(true);
            }
            return;
        }

        if (holder instanceof GUIPersonnel gui) {
            if (!gui.check(menuId, clickType)) {

            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIPosition gui) {
            if (!gui.check(menuId, clickType)) {

            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIPermissionList gui) {
            if (!gui.check(menuId, clickType)) {

            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIChestShop gui) {
            if (!gui.check(menuId, clickType)) {

            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIConfirmation gui) {
            if (!gui.check(menuId, clickType)) {

            }
            event.setCancelled(true);
            return;
        }

    }

    @EventHandler
    public void onItemMove(InventoryClickEvent event) {

        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof GUIInternalStore)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        GUIInternalStore gui = (GUIInternalStore) topInventory.getHolder();

        Inventory clickedInventory = event.getClickedInventory();
        InventoryAction action = event.getAction();

        if (clickedInventory != null && clickedInventory.equals(topInventory)) {
            // Run in next tick to ensure the inventory has been updated
            Bukkit.getScheduler().runTaskLater(MyCompany.getInstance(), task -> {
                gui.updatePrice();
            }, 1L);
            return;
        }

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            // 物品从玩家背包移动到目标容器
            Bukkit.getScheduler().runTaskLater(MyCompany.getInstance(), task -> {
                gui.updatePrice();
            }, 1L);
        }

    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {

        Inventory topInventory = event.getInventory();
        if (!(topInventory.getHolder() instanceof GUIInternalStore)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        GUIInternalStore gui = (GUIInternalStore) topInventory.getHolder();

        // Run in next tick to ensure the inventory has been updated
        Bukkit.getScheduler().runTaskLater(MyCompany.getInstance(), task -> {
            gui.updatePrice();
        }, 1L);

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        Inventory topInventory = event.getInventory();
        if (!(topInventory.getHolder() instanceof GUIInternalStore gui)) {
            return;
        }

        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        for (int slot = 0; slot <= 44; slot++) {
            ItemStack item = topInventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item);
            }
        }

    }

}
