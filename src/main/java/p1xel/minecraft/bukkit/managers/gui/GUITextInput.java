package p1xel.minecraft.bukkit.managers.gui;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

        AnvilGUI.Builder anvil = new AnvilGUI.Builder();
        anvil.itemLeft(firstItem);
        anvil.plugin(MyCompany.getInstance());
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

        //AnvilInventory inventory = view.getTopInventory();
        switch (action) {
            case "found":
                new PersonalAPI(playerUniqueId).foundCompany(text);
                break;
        }

        return true;
    }

}
