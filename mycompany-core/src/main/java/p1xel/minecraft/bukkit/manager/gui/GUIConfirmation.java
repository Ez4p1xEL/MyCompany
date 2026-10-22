package p1xel.minecraft.bukkit.manager.gui;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.Map;
import java.util.UUID;

public class GUIConfirmation extends CompanyMenu {

    private final Runnable onConfirm;
    private final Runnable onCancel;

    public GUIConfirmation(UUID playerUniqueId, String action, Runnable onConfirm, Runnable onCancel) {
        super(playerUniqueId);
        this.menu = MenuUtil.getMenu("confirmation");
        this.action = action;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        init();
    }

    @Override
    public void init() {

        String actionLabel = menu.getActions().get(action).get("label");
        String actionDescription = menu.getActions().get(action).get("description");

        Inventory inventory = Bukkit.createInventory(this, menu.getSize(), menu.getTitle().replace("%action_label%", actionLabel));

        Map<String, String> placeholders = Map.ofEntries(
                Map.entry("%player%", playerName),
                Map.entry("%action_label%", actionLabel)
        );

        Map<String, String> descriptionPlaceholder = Map.ofEntries(
                Map.entry("%action_description%", actionDescription)
        );

        MenuItem confirm = menu.getItems().get("confirm");
        confirm = replacePlaceholdersInMenuItems(confirm, descriptionPlaceholder);

        MenuItem cancel = menu.getItems().get("cancel");
        cancel = replacePlaceholdersInMenuItems(cancel, descriptionPlaceholder);

        inventory = setItems(inventory, menu, placeholders);


        // for each slot in the inventory
        inventory = fillEmptySlot(inventory, menu);

        this.inventory = inventory;
    }

    @Override
    public boolean check(String name, ClickType clickType) {

        switch (name) {
            case "back_to_main", "cancel": {
                player.playSound(player, Sound.BLOCK_CHEST_CLOSE, 1, 1);
                onCancel.run();
                return true;
            }

            case "confirm": {
                player.playSound(player, Sound.ENTITY_VILLAGER_YES, 1, 1);
                onConfirm.run();
                return true;
            }

            default: {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            }
        }

        return true;
    }
}
