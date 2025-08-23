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
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GUIPlayerList extends GUIAbstract implements InventoryHolder {

    private UUID playerUniqueId;
    private String action;
    private int page;
    private boolean hasCompany;
    private UUID companyUniqueId;
    private Inventory inventory;

    public GUIPlayerList(UUID playerUniqueId, String action, int page) {
        this.playerUniqueId = playerUniqueId;
        this.action = action;
        this.page = page;
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (companyUniqueId != null) {
            this.companyUniqueId = companyUniqueId;
            hasCompany = true;
            init();
        }
    }

    @Override
    public void init() {
        String companyName = companyManager.getName(companyUniqueId);
        Inventory inventory = Bukkit.createInventory(this, 45, Locale.getMessage("menu.player-list.title").replaceAll("%company%", companyName));
        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (page > 1) {
            hasPreviousPage = true;
        }

        List<UUID> uuids = Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toList());

        List<UUID> employeeUniqueIds = new ArrayList<>();

        for (String position : companyManager.getPositions(companyUniqueId)) {
            employeeUniqueIds.addAll(companyManager.getEmployeeList(companyUniqueId, position));
        }

        employeeUniqueIds.add(playerUniqueId);

        uuids.removeAll(employeeUniqueIds);

        if (uuids.size() >= page*14) {
            hasNextPage = true;
        }
        uuids.subList(14 * (page-1), Math.min((page) * 14, uuids.size()));

        ItemStack company_info = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.player-list.items.company_info.material")), 1);
        ItemMeta company_info_meta = company_info.getItemMeta();
        company_info_meta.setDisplayName(Locale.getMessage("menu.player-list.items.company_info.display_name").replaceAll("%company%", companyName));
        List<String> company_info_lore = Locale.yaml.getStringList("menu.player-list.items.company_info.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .map(line -> line.replaceAll("%company%", companyName))
                .collect(Collectors.toList());
        company_info_meta.setLore(company_info_lore);
        PersistentDataContainer company_info_container = company_info_meta.getPersistentDataContainer();
        company_info_container.set(menu_id_key, PersistentDataType.STRING, "company_info");
        company_info.setItemMeta(company_info_meta);
        inventory.setItem(4, company_info);

        if (hasNextPage) {

            ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.player-list.items.next_page.material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Locale.getMessage("menu.player-list.items.next_page.display_name"));
            List<String> lore = Locale.yaml.getStringList("menu.player-list.items.next_page.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "next_page");
            item.setItemMeta(meta);
            inventory.setItem(5, item);
        }

        if (hasPreviousPage) {

            ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.player-list.items.previous_page.material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Locale.getMessage("menu.player-list.items.previous_page.display_name"));
            List<String> lore = Locale.yaml.getStringList("menu.player-list.items.previous_page.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "previous_page");
            item.setItemMeta(meta);
            inventory.setItem(3, item);
        }

        ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.player-list.items.back_to_main.material")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Locale.getMessage("menu.player-list.items.back_to_main.display_name"));
        List<String> lore = Locale.yaml.getStringList("menu.player-list.items.back_to_main.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(menu_id_key, PersistentDataType.STRING, "back_to_main");
        item.setItemMeta(meta);
        inventory.setItem(36, item);

        this.inventory = inventory;
        update(uuids);
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
            player.openInventory(new GUIPlayerList(playerUniqueId, action, page+1).getInventory());
            return true;
        }
        if (name.equalsIgnoreCase("previous_page")) {
            player.openInventory(new GUIPlayerList(playerUniqueId, action, Math.min(1,page-1)).getInventory());
            return true;
        }

        if (name.equalsIgnoreCase("back_to_main")) {
            player.openInventory(new GUIMain(playerUniqueId).getInventory());
            return true;
        }

        if (name.startsWith("player:")) {
            String targetName = name.split(":")[1];
            OfflinePlayer off_target = Bukkit.getOfflinePlayer(targetName);
            UUID targetUniqueId = off_target.getUniqueId();
            if (action.equalsIgnoreCase("employ")) {
                boolean sendRequest = MyCompany.getHireRequestManager().sendRequest(playerUniqueId, targetUniqueId);
                if (!sendRequest) {
                    player.sendMessage(Locale.getMessage("contract-already-sent"));
                    return true;
                }

                Player target = (Player) off_target;
                int respondTime = Config.getInt("company-settings.hire-request-time");
                String companyName = companyManager.getName(companyUniqueId);

                player.sendMessage(Locale.getMessage("contract-send").replaceAll("%player%", targetName).replaceAll("%second%", String.valueOf(respondTime)));
                player.playSound(player, Sound.BLOCK_ANVIL_PLACE, 3f, 3f);
                target.sendMessage(Locale.getMessage("contract-received").replaceAll("%player%", player.getName()).replaceAll("%company%", companyName).replaceAll("%second%", String.valueOf(respondTime)));
                target.playSound(target, Sound.BLOCK_ANVIL_PLACE, 3f, 3f);
                return false;
            }
        }
        return true;
    }

    public void update(List<UUID> uuids) {
        String companyName = companyManager.getName(companyUniqueId);
        int slot = 20;
        for (UUID playerUniqueId : uuids) {
            if (slot == 25) {
                slot = 29;
            }

            String material = Locale.getMessage("menu.player-list.items.player.material");
            String[] args = material.split(";");
            material = args[0];

            ItemStack item = new ItemStack(Material.matchMaterial(material));
            ItemMeta meta = item.getItemMeta();
            String targetName = Bukkit.getOfflinePlayer(playerUniqueId).getName();
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("player_head")) {
                    SkullMeta skullMeta = (SkullMeta) meta;
                    skullMeta.setOwner(targetName);
                }
            }
            meta.setDisplayName(Locale.getMessage("menu.player-list.items.player.display_name").replaceAll("%company%", companyName).replaceAll("%player%", targetName));
            List<String> lore_list = Locale.yaml.getStringList("menu.player-list.items.player.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            meta.setLore(lore_list);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "player:" + targetName);
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
                meta.setDisplayName("");
                item.setItemMeta(meta);
                inventory.setItem(i, item);
            }
        }
    }

    public String getAction() {
        return action;
    }
}
