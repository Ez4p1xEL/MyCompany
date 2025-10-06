package p1xel.minecraft.bukkit.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
                    Player player = (Player) event.getWhoClicked();
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
                    Player player = (Player) event.getWhoClicked();
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
                    Player player = (Player) event.getWhoClicked();
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
                    Player player = (Player) event.getWhoClicked();
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
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                }
            }
            event.setCancelled(true);
            return;
        }

//        InventoryView view = event.getView();
//        if (view instanceof AnvilView) {
//            if (holder instanceof GUITextInput) {
//
//                if (event.getSlot() == 2) {
//                    GUITextInput gui = (GUITextInput) holder;
//                    gui.check((AnvilView) view);
//                    Player player = (Player) event.getWhoClicked();
//                    player.closeInventory();
//                }
//                event.setCancelled(true);
//            }
//        }
    }

}
