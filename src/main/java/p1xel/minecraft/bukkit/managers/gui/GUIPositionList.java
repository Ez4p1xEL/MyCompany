package p1xel.minecraft.bukkit.managers.gui;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.permissions.Permission;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GUIPositionList extends GUIAbstract implements InventoryHolder {

    private UUID playerUniqueId;
    private UUID companyUniqueId;
    private boolean hasCompany;
    private Inventory inventory;
    private int page;
    private String action;
    private boolean exceptEmployer;

    public GUIPositionList(UUID playerUniqueId, String action, boolean exceptEmployer ,int page) {
        this.playerUniqueId = playerUniqueId;
        this.page = page;
        this.action = action;
        this.exceptEmployer = exceptEmployer;
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (companyUniqueId != null) {
            this.companyUniqueId = companyUniqueId;
            hasCompany = true;
            init();
        }
    }

    @Override
    public void init() {

        List<String> positions = companyManager.getPositions(companyUniqueId);
        if (exceptEmployer) {
            positions.remove("employer");
        }

        String companyName = companyManager.getName(companyUniqueId);
        Inventory inventory = Bukkit.createInventory(this, 45, Locale.getMessage("menu.position-list.title").replaceAll("%company%", companyName));
        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (page > 1) {
            hasPreviousPage = true;
        }

        if (positions.size() >= page*14) {
            hasNextPage = true;
        }
        positions.subList(14 * (page-1), Math.min((page) * 14, positions.size()));

        ItemStack company_info = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.position-list.items.company_info.material")), 1);
        ItemMeta company_info_meta = company_info.getItemMeta();
        company_info_meta.setDisplayName(Locale.getMessage("menu.position-list.items.company_info.display_name").replaceAll("%company%", companyName));
        List<String> company_info_lore = Locale.yaml.getStringList("menu.position-list.items.company_info.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .map(line -> line.replaceAll("%company%", companyName))
                .collect(Collectors.toList());
        company_info_meta.setLore(company_info_lore);
        PersistentDataContainer company_info_container = company_info_meta.getPersistentDataContainer();
        company_info_container.set(menu_id_key, PersistentDataType.STRING, "company_info");
        company_info.setItemMeta(company_info_meta);
        inventory.setItem(4, company_info);

        if (hasNextPage) {

            ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.position-list.items.next_page.material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Locale.getMessage("menu.position-list.items.next_page.display_name"));
            List<String> lore = Locale.yaml.getStringList("menu.position-list.items.next_page.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "next_page");
            item.setItemMeta(meta);
            inventory.setItem(5, item);
        }

        if (hasPreviousPage) {

            ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.position-list.items.previous_page.material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Locale.getMessage("menu.position-list.items.previous_page.display_name"));
            List<String> lore = Locale.yaml.getStringList("menu.position-list.items.previous_page.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "previous_page");
            item.setItemMeta(meta);
            inventory.setItem(3, item);
        }

        ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.position-list.items.back_to_main.material")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Locale.getMessage("menu.position-list.items.back_to_main.display_name"));
        List<String> lore = Locale.yaml.getStringList("menu.position-list.items.back_to_main.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(menu_id_key, PersistentDataType.STRING, "back_to_main");
        item.setItemMeta(meta);
        inventory.setItem(36, item);

        this.inventory = inventory;
        update(positions);

    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean check(String name) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        if (name.equalsIgnoreCase("company_info")) {
            return true;
        }
        if (name.equalsIgnoreCase("next_page")) {
            player.openInventory(new GUIPositionList(playerUniqueId, action, exceptEmployer, +1).getInventory());
            return true;
        }
        if (name.equalsIgnoreCase("previous_page")) {
            player.openInventory(new GUIPositionList(playerUniqueId, action, exceptEmployer, Math.min(1,page-1)).getInventory());
            return true;
        }

        if (name.equalsIgnoreCase("back_to_main")) {
            player.openInventory(new GUIMain(playerUniqueId).getInventory());
            return true;
        }

        if (name.startsWith("position:")) {
            String position = name.split(":")[1];
            if (action.equalsIgnoreCase("position_setlabel")) {
                new GUITextInput(playerUniqueId, action + ":" + position);
                return true;
            }
        }
        return true;
    }

    public void update(List<String> positions) {
        String companyName = companyManager.getName(companyUniqueId);
        int slot = 20;
        for (String position : positions) {
            if (slot == 25) {
                slot = 29;
            }

            String material = Locale.getMessage("menu.position-list.items.position.material");

            ItemStack item = new ItemStack(Material.matchMaterial(material));
            ItemMeta meta = item.getItemMeta();
            String label = companyManager.getPositionLabel(companyUniqueId, position);
            meta.setDisplayName(Locale.getMessage("menu.position-list.items.position.display_name").replaceAll("%company%", companyName).replaceAll("%position%", position).replaceAll("%label%", label));
            List<String> lore_list = Locale.yaml.getStringList("menu.position-list.items.position.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line).replaceAll("%position%", position).replaceAll("%label%", label).replaceAll("%permissions%", companyManager.getPositionPermission(companyUniqueId, position).toString()))
                    .collect(Collectors.toList());
            meta.setLore(lore_list);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "position:" + position);
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
            slot++;
            if (slot >= 36) {
                break;
            }

        }

        for (int i = 0; i < 9*5; i++) {
            if (inventory.getItem(i) == null) {
                ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(" ");
                item.setItemMeta(meta);
                inventory.setItem(i, item);
            }
        }
    }
}
