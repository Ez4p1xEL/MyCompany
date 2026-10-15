package p1xel.minecraft.bukkit.manager.gui;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.manager.gui.area.GUICompanyArea;
import p1xel.minecraft.bukkit.manager.gui.order.GUIDailyOrder;
import p1xel.minecraft.bukkit.manager.gui.player.GUIPersonnel;
import p1xel.minecraft.bukkit.manager.gui.position.GUIPosition;
import p1xel.minecraft.bukkit.manager.gui.shop.GUIChestShop;
import p1xel.minecraft.bukkit.manager.gui.store.GUIInternalStore;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.Map;
import java.util.UUID;

public class GUIMain extends CompanyMenu {

    private int companyId;
    private double salary;
    private double cash;
    private double dailyIncome;
    private double totalIncome;

    public GUIMain(UUID playerUniqueId) {
        super(playerUniqueId);
        this.menu = MenuUtil.getMenu("main");
        if (hasCompany) {
            init();
        } else {
            // Back to GUIFound
            GUIFound gui = new GUIFound(playerUniqueId);
            player.openInventory(gui.getInventory());
        }
    }

    @Override
    public void init() {
        this.companyId = company.getId();
        this.salary = company.getPositionSalary(position);
        this.cash = company.getCash();
        this.dailyIncome = company.getDailyIncome();
        this.totalIncome = company.getTotalIncome();
        Inventory inventory = Bukkit.createInventory(this, menu.getSize(), menu.getTitle().replace("%company%", companyName));

        Map<String, String> placeholders = Map.ofEntries(
                Map.entry("%player%", playerName),
                Map.entry("%company%", companyName),
                Map.entry("%position%", position),
                Map.entry("%label%", positionLabel),
                Map.entry("%cid%", String.valueOf(companyId)),
                Map.entry("%salary%", String.valueOf(salary)),
                Map.entry("%cash%", String.valueOf(cash)),
                Map.entry("%daily%", String.valueOf(dailyIncome)),
                Map.entry("%total%", String.valueOf(totalIncome)),

                Map.entry("%tax_management%", String.valueOf(Config.getDouble("company-funds.cost-per-day.management-fee"))),
                Map.entry("%tax_property%", String.valueOf(Config.getTaxRate("property", MyCompany.getTaxCollector().getPhase("property-tax", companyManager.getCash(companyUniqueId))))),
                Map.entry("%tax_income%", String.valueOf(Config.getTaxRate("income", MyCompany.getTaxCollector().getPhase("income-tax", companyManager.getDailyIncome(companyUniqueId))))),
                Map.entry("%chestshop_identifier%", Config.getString("chest-shop.sign-create.identifier")),
                Map.entry("%salary_time%", Config.getString("company-funds.cost-per-day.tax-time"))
        );

        inventory = setItems(inventory, menu, placeholders);

        // for each slot in the inventory
        inventory = fillEmptySlot(inventory, menu);

        this.inventory = inventory;
    }

    @Override
    public boolean check(String name, ClickType clickType) {

        switch (name) {
            case "company_info": {
                Bukkit.getServer().dispatchCommand(player, "mycompany info");
                return true;
            }

            case "balance": {
                Bukkit.getServer().dispatchCommand(player, "mycompany balance");
                return true;
            }

            case "tp": {
                new PersonalAPI(playerUniqueId).teleportToCompany();
                return true;
            }

            case "resign": {
                if (userManager.getPosition(playerUniqueId).equalsIgnoreCase("employer")) {
                    player.sendMessage(Locale.getMessage("disband-instead"));
                    player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return true;
                }
                GUIConfirmation gui = new GUIConfirmation(playerUniqueId, "resign", "resign from your companmy",
                        () -> {
                    new PersonalAPI(playerUniqueId).resignFromCompany();
                    player.closeInventory();
                    },
                        () -> player.openInventory(this.getInventory()));
                player.openInventory(gui.getInventory());
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                return true;
            }

            case "order": {
                CompanyMenu gui = new GUIDailyOrder(playerUniqueId, "daily_order",1);
                gui.setPreviousMenu(this);
                player.openInventory(gui.getInventory());
                player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                return true;
            }

            case "area": {
                CompanyMenu gui = new GUICompanyArea(playerUniqueId);
                gui.setPreviousMenu(this);
                player.openInventory(gui.getInventory());
                player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                return true;
            }

            case "personnel": {
                CompanyMenu gui = new GUIPersonnel(playerUniqueId,  1);
                gui.setPreviousMenu(this);
                player.openInventory(gui.getInventory());
                player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                return true;
            }

            case "position": {
                CompanyMenu gui = new GUIPosition(playerUniqueId, 1);
                gui.setPreviousMenu(this);
                player.openInventory(gui.getInventory());
                player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                return true;
            }

            case "chestshop": {
                CompanyMenu gui = new GUIChestShop(playerUniqueId);
                gui.setPreviousMenu(this);
                player.openInventory(gui.getInventory());
                player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                return true;
            }

            case "internal_store": {
                CompanyMenu gui = new GUIInternalStore(playerUniqueId);
                gui.setPreviousMenu(this);
                player.openInventory(gui.getInventory());
                player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                return true;
            }

            case "focus": {
                // TODO: Open focus GUI
                player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                return true;
            }
        }
        return true;
    }
}
