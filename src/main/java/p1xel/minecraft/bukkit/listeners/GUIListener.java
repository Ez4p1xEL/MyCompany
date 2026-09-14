package p1xel.minecraft.bukkit.listeners;

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
import p1xel.minecraft.bukkit.managers.gui.*;
import p1xel.minecraft.bukkit.utils.storage.Locale;

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

        HumanEntity entity = event.getWhoClicked();

        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        NamespacedKey menu_id_key = new NamespacedKey("mycompany", "menu_id");

        if (holder instanceof GUIFound) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIFound gui = (GUIFound) holder;
                gui.check(container.get(menu_id_key, PersistentDataType.STRING));
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIMain) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIMain gui = (GUIMain) holder;
                if (!gui.check(container.get(menu_id_key, PersistentDataType.STRING))) {
                    player.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", container.get(menu_id_key, PersistentDataType.STRING)));
                }
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIEmployeeList) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIEmployeeList gui = (GUIEmployeeList) holder;
                if (!gui.check(container.get(menu_id_key, PersistentDataType.STRING))) {
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIPlayerList) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIPlayerList gui = (GUIPlayerList) holder;
                if (!gui.check(container.get(menu_id_key, PersistentDataType.STRING))) {
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIPositionList) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIPositionList gui = (GUIPositionList) holder;
                if (!gui.check(container.get(menu_id_key, PersistentDataType.STRING))) {
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIDailyOrder) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIDailyOrder gui = (GUIDailyOrder) holder;
                if (!gui.check(container.get(menu_id_key, PersistentDataType.STRING))) {
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUICompanyArea) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUICompanyArea gui = (GUICompanyArea) holder;
                if (!gui.check(container.get(menu_id_key, PersistentDataType.STRING))) {
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIAreaList) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIAreaList gui = (GUIAreaList) holder;
                if (!gui.check(container.get(menu_id_key, PersistentDataType.STRING), event.getClick())) {
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIAreaTradeMarket) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIAreaTradeMarket gui = (GUIAreaTradeMarket) holder;
                if (!gui.check(container.get(menu_id_key, PersistentDataType.STRING))) {
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIAreaRentMarket) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIAreaRentMarket gui = (GUIAreaRentMarket) holder;
                if (!gui.check(container.get(menu_id_key, PersistentDataType.STRING), event.getClick())) {
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIAreaSaleMarket) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIAreaSaleMarket gui = (GUIAreaSaleMarket) holder;
                if (!gui.check(container.get(menu_id_key, PersistentDataType.STRING), event.getClick())) {
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

        if (holder instanceof GUIInternalStore) {
            ItemStack item = inventory.getItem(event.getSlot());
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(menu_id_key, PersistentDataType.STRING)) {
                GUIInternalStore gui = (GUIInternalStore) holder;
                if (gui.check(player, container.get(menu_id_key, PersistentDataType.STRING))) {
                    event.setCancelled(true);
                }
            }
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
