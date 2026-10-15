package p1xel.minecraft.bukkit.manager.gui.area;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.Map;
import java.util.UUID;

public class GUIAreaTradeMarket extends CompanyMenu {

    public GUIAreaTradeMarket(UUID playerUniqueId) {
        super(playerUniqueId);
        this.menu = MenuUtil.getMenu("area_trade_market");
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
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean check(String name) {
        Player player = Bukkit.getPlayer(playerUniqueId);

        if (name.equalsIgnoreCase("back_to_main")) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 1);
            player.openInventory(new GUICompanyArea(playerUniqueId).getInventory());
            return true;
        }

        if (name.equalsIgnoreCase("rent")) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
            player.openInventory(new GUIAreaRentMarket(playerUniqueId, "rent", 1).getInventory());
        }

        if (name.equalsIgnoreCase("sell")) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
            player.openInventory(new GUIAreaSaleMarket(playerUniqueId, "sell", 1).getInventory());
        }
        return true;
    }

    @Override
    public boolean check(String name, ClickType clickType) {
        return false;
    }
}
