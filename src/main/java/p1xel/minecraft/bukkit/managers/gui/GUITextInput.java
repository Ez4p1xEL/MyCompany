package p1xel.minecraft.bukkit.managers.gui;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class GUITextInput extends GUIAbstract {

    private UUID playerUniqueId;
    private String action;
    private UUID companyUniqueId;
    private AnvilGUI.Builder inventory;
    private boolean hasCompany;

    public GUITextInput(UUID playerUniqueId, String action) {
        this.playerUniqueId = playerUniqueId;
        this.action = action;
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (companyUniqueId != null) {
            this.companyUniqueId = companyUniqueId;
            hasCompany = true;
        }
        init();
    }

    @Override
    public void init() {
        //AnvilInventory inventory = (AnvilInventory) Bukkit.createInventory(this, InventoryType.ANVIL, Locale.getMessage("menu.text-input.title"));
        ItemStack firstItem = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.text-input.items.first_item.material")));
        ItemMeta firstItemMeta = firstItem.getItemMeta();
        firstItemMeta.setDisplayName(Locale.getMessage("menu.text-input.items.first_item.display_name"));
        firstItem.setItemMeta(firstItemMeta);
        //inventory.setFirstItem(firstItem);

        String title = "Text Input";
        if (action.startsWith("found")) {
            title = Locale.getMessage("menu.text-input.title.found");
        } else if (action.startsWith("position_setlabel")) {
            title = Locale.getMessage("menu.text-input.title.position_setlabel");
        } else if (action.startsWith("set_position")) {
            title = Locale.getMessage("menu.text-input.title.set_position");
        } else if (action.startsWith("position_add")) {
            title = Locale.getMessage("menu.text-input.title.position_add");
        } else if (action.startsWith("area-create")) {
            title = Locale.getMessage("menu.text-input.title.area-create");
        } else if (action.startsWith("area-rent-set-price")) {
            title = Locale.getMessage("menu.text-input.title.area-rent-set-price");
        }

        AnvilGUI.Builder anvil = new AnvilGUI.Builder();
        anvil.itemLeft(firstItem);
        anvil.plugin(MyCompany.getInstance());
        anvil.title(title);
        anvil.onClick((slot, stateSnapshot) -> {
            if (slot != AnvilGUI.Slot.OUTPUT) {
                return Collections.emptyList();
            }
            String text = stateSnapshot.getText();
                return Arrays.asList(
                        AnvilGUI.ResponseAction.close(),
                        AnvilGUI.ResponseAction.run(() -> check(text)));
        });
        anvil.open(Bukkit.getPlayer(playerUniqueId));
        this.inventory = anvil;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public AnvilGUI.Builder getAnvil() {
        return inventory;
    }

    @Override
    public boolean check(String text) {

        Player player = Bukkit.getPlayer(playerUniqueId);

        if (text.equalsIgnoreCase("cancel")) {
            return true;
        }

        //AnvilInventory inventory = view.getTopInventory();
        if (action.equalsIgnoreCase("found")) {
            new PersonalAPI(playerUniqueId).foundCompany(text);
            return true;
        }

        if (action.startsWith("position_setlabel:")) {
            String position = action.split(":")[1];
            new PersonalAPI(playerUniqueId).setPositionLabel(position, text);
            return true;
        }

        if (action.startsWith("set_position:")) {
            UUID targetUniqueId = UUID.fromString(action.split(":")[1]);
            new PersonalAPI(playerUniqueId).setEmployeePosition(targetUniqueId, Bukkit.getPlayer(targetUniqueId).getName(), text);
            return true;
        }

        if (action.equalsIgnoreCase("position_add")) {
            new PersonalAPI(playerUniqueId).addPosition(text, null);
            return true;
        }

        if (action.equalsIgnoreCase("area-create")) {
            new PersonalAPI(playerUniqueId).createArea(text);
            player.openInventory(new GUICompanyArea(playerUniqueId).getInventory());
            return true;
        }

        if (action.startsWith("area-rent-set-price")) {
            double price;
            try {
                price = Double.parseDouble(text);
            } catch (NumberFormatException e) {
                player.sendMessage(Locale.getMessage("invalid-amount"));
                return true;
            }
            String areaName = action.split(":")[1];
            new PersonalAPI(playerUniqueId).pushAreaToRentMarket(areaName, price);
            player.openInventory(new GUIAreaList(playerUniqueId, "rent",1).getInventory());
            return true;
        }

        if (action.startsWith("area-sell-set-price")) {
            double price;
            try {
                price = Double.parseDouble(text);
            } catch (NumberFormatException e) {
                player.sendMessage(Locale.getMessage("invalid-amount"));
                return true;
            }
            String areaName = action.split(":")[1];
            new PersonalAPI(playerUniqueId).pushAreaToSaleMarket(areaName, price);
            player.openInventory(new GUIAreaList(playerUniqueId, "sell",1).getInventory());
            return true;
        }

        return true;
    }

}
