package p1xel.minecraft.bukkit.manager.gui.area;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUIMain;
import p1xel.minecraft.bukkit.manager.gui.GUITextInput;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.Map;
import java.util.UUID;

public class GUICompanyArea extends CompanyMenu {

    private final Menu menu;

    public GUICompanyArea(UUID playerUniqueId) {
        super(playerUniqueId);
        this.menu = MenuUtil.getMenu("areas");
        if (hasCompany) {
            init();
        }
    }

    @Override
    public void init() {

        Inventory inventory = Bukkit.createInventory(this, menu.getSize(), menu.getTitle().replace("%company%", companyName));

        Map<String, String> placeholders = Map.ofEntries(
                Map.entry("%player%", playerName),
                Map.entry("%company%", companyName),
                Map.entry("%position%", position),
                Map.entry("%label%", positionLabel)
        );

        inventory = setItems(inventory, menu, placeholders);

        // for each slot in the inventory
        inventory = fillEmptySlot(inventory, menu);

        this.inventory = inventory;
    }

    @Override
    public boolean check(String name) {
        Player player = Bukkit.getPlayer(playerUniqueId);

        switch (name) {
            case "back_to_main": {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 1);
                player.openInventory(new GUIMain(playerUniqueId).getInventory());
                return true;
            }

            case "create": {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                new GUITextInput(playerUniqueId, "area-create");
                return true;
            }

            case "list", "delete", "rent", "sell": {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.openInventory(new GUIAreaList(playerUniqueId, name, 1).getInventory());
                return true;
            }

            case "market": {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.openInventory(new GUIAreaTradeMarket(playerUniqueId).getInventory());
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean check(String name, ClickType clickType) {
        return false;
    }
}
