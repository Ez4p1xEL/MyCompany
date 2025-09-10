package p1xel.minecraft.bukkit.managers.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.permissions.Permission;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GUIMain extends GUIAbstract implements InventoryHolder {

    private UUID playerUniqueId;
    private boolean hasCompany;
    private UUID companyUniqueId;
    private String position;
    private Inventory inventory;

    public GUIMain(UUID playerUniqueId) {
        this.playerUniqueId = playerUniqueId;
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (companyUniqueId != null) {
            this.companyUniqueId = companyUniqueId;
            hasCompany = true;
            init();
        }
    }

    @Override
    public void init() {
        this.position = userManager.getPosition(playerUniqueId);
        String companyName = companyManager.getName(companyUniqueId);
        String positionLabel = companyManager.getPositionLabel(companyUniqueId, position);
        int companyId = companyManager.getId(companyUniqueId);
        double salary = companyManager.getSalary(companyUniqueId, position);
        Inventory inventory = Bukkit.createInventory(this, 45, Locale.getMessage("menu.main.title").replaceAll("%company%", companyName));

        for (String item_name : Locale.yaml.getConfigurationSection("menu.main.items").getKeys(false)) {

            String display_name = Locale.getMessage("menu.main.items." + item_name + ".display_name");
            display_name = display_name.replaceAll("%company%", companyName);
            List<String> loreList = Locale.yaml.getStringList("menu.main.items." + item_name + ".lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .map(line -> line.replaceAll("%company%", companyName).replaceAll("%position%", position).replaceAll("%label%", positionLabel)
                            .replaceAll("%cid%", String.valueOf(companyId)).replaceAll("%salary%", String.valueOf(salary))
                            .replaceAll("%cash%", String.valueOf(companyManager.getCash(companyUniqueId)))
                            .replaceAll("%daily%", String.valueOf(companyManager.getDailyIncome(companyUniqueId)))
                            .replaceAll("%total%", String.valueOf(companyManager.getTotalIncome(companyUniqueId)))
                            .replaceAll("%tax_management%", String.valueOf(Config.getDouble("company-funds.cost-per-day.management-fee")))
                            .replaceAll("%tax_property%", String.valueOf(Config.getTaxRate("property", MyCompany.getTaxCollector().getPhase("property-tax", companyManager.getCash(companyUniqueId)))))
                            .replaceAll("%tax_income%", String.valueOf(Config.getTaxRate("income", MyCompany.getTaxCollector().getPhase("income-tax", companyManager.getDailyIncome(companyUniqueId)))))
                            .replaceAll("%permit_"+ item_name +"%", Locale.getYesOrNo(userManager.hasPermission(playerUniqueId, Permission.matchPermission(item_name))))
//                            .replaceAll("%permit_employ%", Locale.getYesOrNo(userManager.hasPermission(playerUniqueId, Permission.EMPLOY)))
//                            .replaceAll("%permit_fire%", Locale.getYesOrNo(userManager.hasPermission(playerUniqueId, Permission.FIRE)))
//                            .replaceAll("%permit_chestshop_create%", Locale.getYesOrNo(userManager.hasPermission(playerUniqueId, Permission.CHESTSHOP_CREATE)))
//                            .replaceAll("%permit_chestshop_delete%", Locale.getYesOrNo(userManager.hasPermission(playerUniqueId, Permission.CHESTSHOP_DELETE)))
                            .replaceAll("%chestshop_identifier%", Config.getString("chest-shop.sign-create.identifier")))
                    .collect(Collectors.toList());

            String material = Locale.getMessage("menu.main.items." + item_name + ".material");
            String[] args = material.split(";");
            material = args[0];

            ItemStack item = new ItemStack(Material.matchMaterial(material));
            ItemMeta meta = item.getItemMeta();
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("player_head")) {
                    SkullMeta skullMeta = (SkullMeta) meta;
                    skullMeta.setOwner(Bukkit.getOfflinePlayer(playerUniqueId).getName());
                }
            }
            meta.setDisplayName(display_name);
            meta.setLore(loreList);

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, item_name);
            item.setItemMeta(meta);

            int slot = 0;
            switch (item_name) {
                case "user_info":
                    slot = 3;
                    break;
                case "company_info":
                    slot = 4;
                    break;
                case "balance":
                    slot = 5;
                    break;
                case "employ":
                    slot = 20;
                    break;
                case "fire":
                    slot = 29;
                    break;
                case "chestshop_create":
                    slot = 21;
                    break;
                case "chestshop_delete":
                    slot = 30;
                    break;
                case "set_position":
                    slot = 22;
                    break;
                case "position_setlabel":
                    slot = 31;
                    break;
                case "position_add":
                    slot = 23;
                    break;
                case "position_remove":
                    slot = 32;
                    break;
                case "tp":
                    slot = 17;
                    break;
            }

            inventory.setItem(slot, item);

        }

        for (int i = 0; i < 9*5; i++) {
            if (inventory.getItem(i) == null) {
                ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("");
                item.setItemMeta(meta);
                inventory.setItem(i, item);
            }
        }

        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean check(String name) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        if (name.equalsIgnoreCase("company_info")) {
            Bukkit.getServer().dispatchCommand(player, "mycompany info");
            return true;
        }
        if (name.equalsIgnoreCase("balance")) {
            Bukkit.getServer().dispatchCommand(player, "mycompany balance");
            return true;
        }
        if (name.equalsIgnoreCase("employ")) {
            if (!userManager.hasPermission(playerUniqueId, Permission.EMPLOY)) {
                return false;
            }
            // Open employing menu...
            Inventory newInv = new GUIPlayerList(playerUniqueId, "employ", 1).getInventory();
            player.openInventory(newInv);
            return true;
        }
        if (name.equalsIgnoreCase("fire")) {
            if (!userManager.hasPermission(playerUniqueId, Permission.FIRE)) {
                return false;
            }
            // Open firing menu...
            Inventory newInv = new GUIEmployeeList(playerUniqueId, "fire", true, true, 1).getInventory();
            player.openInventory(newInv);
            return true;
        }

        if (name.equalsIgnoreCase("position_setlabel")) {
            if (!userManager.hasPermission(playerUniqueId, Permission.POSITION_SETLABEL)) {
                return false;
            }
            Inventory newInv = new GUIPositionList(playerUniqueId, "position_setlabel", false,1).getInventory();
            player.openInventory(newInv);
            return true;
        }

        if (name.equalsIgnoreCase("set_position")) {
            if (!userManager.hasPermission(playerUniqueId, Permission.SET_POSITION)) {
                return false;
            }
            Inventory newInv = new GUIEmployeeList(playerUniqueId, "set_position", true, true, 1).getInventory();
            player.openInventory(newInv);
            return true;
        }

        if (name.equalsIgnoreCase("position_add")) {
            if (!userManager.hasPermission(playerUniqueId, Permission.POSITION_ADD)) {
                return false;
            }
            new GUITextInput(playerUniqueId, "position_add");
            return true;
        }

        if (name.equalsIgnoreCase("position_remove")) {
            if (!userManager.hasPermission(playerUniqueId, Permission.POSITION_REMOVE)) {
                return false;
            }
            Inventory newInv = new GUIPositionList(playerUniqueId, "position_remove", false,1).getInventory();
            player.openInventory(newInv);
            return true;
        }

        if (name.equalsIgnoreCase("tp")) {
            new PersonalAPI(playerUniqueId).teleportToCompany();
            return true;
        }
        return true;
    }
}
