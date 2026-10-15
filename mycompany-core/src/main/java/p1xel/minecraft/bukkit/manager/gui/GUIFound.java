package p1xel.minecraft.bukkit.manager.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GUIFound extends CompanyMenu {

    /*
    Some of the variables should be null, as player has no company yet.
    只有没有公司的玩家才能打开这个界面，所以这些变量应该是null
     */

    public GUIFound(UUID playerUniqueId) {
        super(playerUniqueId);
        this.menu = MenuUtil.getMenu("found");
        if (!hasCompany) {
            init();
        }
    }

    @Override
    public void init() {
        Inventory inventory = Bukkit.createInventory(this, menu.getSize(), menu.getTitle());

        Map<String, String> placeholders = Map.ofEntries(
                Map.entry("%player%", playerName)
        );

        inventory = setItems(inventory, menu, placeholders);

        // for each slot in the inventory
        inventory = fillEmptySlot(inventory, menu);

        this.inventory = inventory;
    }

    @Override
    public boolean check(String name) {
        //Player player = Bukkit.getPlayer(playerUniqueId);
        if (name.equalsIgnoreCase("found")) {
            new GUITextInput(playerUniqueId, "found");
        }
        return true;
    }

    @Override
    public boolean check(String name, ClickType clickType) {
        return false;
    }
}
