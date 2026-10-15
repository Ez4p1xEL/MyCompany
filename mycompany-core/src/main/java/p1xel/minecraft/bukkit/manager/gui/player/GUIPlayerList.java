package p1xel.minecraft.bukkit.manager.gui.player;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUIAbstract;
import p1xel.minecraft.bukkit.manager.gui.GUIMain;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.*;
import java.util.stream.Collectors;

public class GUIPlayerList extends CompanyMenu {

    public GUIPlayerList(UUID playerUniqueId, String action, int page) {
        super(playerUniqueId);
        this.action = action;
        this.page = page;
        this.menu = new Menu(MenuUtil.getMenu("player_list"));
        if (hasCompany) {
            init();
        }
    }

    @Override
    public void init() {
        Inventory inventory = Bukkit.createInventory(this, menu.getSize(), menu.getTitle().replace("%company%", companyName));

        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (page > 1) {
            hasPreviousPage = true;
        }

        List<UUID> uuids = new ArrayList<>();

        // 移除已有公司的玩家
        if (action.equals("employ")) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                User user = userManager.getUser(onlinePlayer.getUniqueId());
                if (user.getCompanyUUID() == null) {
                    uuids.add(onlinePlayer.getUniqueId());
                }
            }
        }


        int player_per_page = menu.getInt("player-per-page", 10);

        if (uuids.size() > page*player_per_page) {
            hasNextPage = true;
        }
        uuids = uuids.subList(player_per_page * (page-1), Math.min((page) * player_per_page, uuids.size()));

        // 显示previous_page 和 next_page按钮
        if (!hasPreviousPage) {
            menu.getItems().remove("previous_page");
        }
        if (!hasNextPage) {
            menu.getItems().remove("next_page");
        }

        Map<String, String> placeholders = Map.ofEntries(
                Map.entry("%player%", playerName),
                Map.entry("%position%", position),
                Map.entry("%label%", positionLabel),
                Map.entry("%amount%", String.valueOf(company.getPersonnelAmount())),
                Map.entry("%max%", String.valueOf(Config.getInt("company-settings.maximum-players")))
        );


        // 复制 player 的 item
        if (uuids.size() < player_per_page) {
            player_per_page = uuids.size();
        }
        // 当前 player 的位置, 再获取它与当前行首个slot的位置的距离
        int current_slot = menu.getItems().get("player").getSlots().getFirst();
        int line = (current_slot / 9)+1;
        int distance = current_slot - ((line-1)*9);

        int slot = current_slot;

        MenuItem playerItem = menu.getItems().get("player").clone();
        menu.getItems().remove("player");

        for (int i = 0; i < player_per_page; i++) {

            // 计算当前行数
            int current_line = (slot / 9)+1;
            // 如果当前 slot 超过了当前行的最后一个 available slot, 则跳到下一行
            if (slot >= (current_line*9)-distance) {
                slot += distance+distance;
            }

            // replace placeholders
            Player player = Bukkit.getPlayer(uuids.get(i));
            String playerName = player.getName();
            UUID playerUniqueId = player.getUniqueId();

            // Get left and right click messages
            String left_click = menu.getActions().get(action).get("left");
            String right_click = menu.getActions().get(action).get("right");

            Map<String, String> employeePlaceholders = Map.ofEntries(
                    Map.entry("%player%", playerName),
                    Map.entry("%left_click%", left_click),
                    Map.entry("%right_click%", right_click)
            );

            MenuItem cloneItem = playerItem.clone();
            cloneItem = replacePlaceholdersInMenuItems(cloneItem, employeePlaceholders);
            cloneItem.setCustomKey("player:"+playerUniqueId);
            cloneItem.setSlots(Collections.singletonList(slot));
            cloneItem.setId(cloneItem.getId() + "_" + slot);

            // 如果Material String使用的是对应玩家的头颅
            // 则在此进行修改头颅皮肤
            String materialString = cloneItem.getMaterialString();
            if (materialString.equalsIgnoreCase("PLAYER_HEAD;%PLAYER%")) {
                materialString = materialString.toUpperCase();
                materialString = materialString.replace("%PLAYER%", playerName);
                cloneItem.setMaterialString(materialString);
            }

            menu.getItems().put(cloneItem.getId(), cloneItem);
            slot++;

        }

        inventory = setItems(inventory, this.menu, placeholders);
        inventory = fillEmptySlot(inventory, this.menu);

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
            player.openInventory(new GUIPersonnel(playerUniqueId, 1).getInventory());
            return true;
        }

        if (name.startsWith("player:")) {
            UUID targetUniqueId = UUID.fromString(name.split(":")[1]);
            Player target = Bukkit.getPlayer(targetUniqueId); // target must be online
            String targetName = target.getName();
            if (action.equalsIgnoreCase("employ")) {
                boolean sendRequest = MyCompany.getHireRequestManager().sendRequest(playerUniqueId, targetUniqueId);
                if (!sendRequest) {
                    player.sendMessage(Locale.getMessage("contract-already-sent"));
                    return true;
                }

                int respondTime = Config.getInt("company-settings.hire-request-time");
                String companyName = companyManager.getName(companyUniqueId);

                player.sendMessage(Locale.getMessage("contract-send").replace("%player%", targetName).replace("%second%", String.valueOf(respondTime)));
                player.playSound(player, Sound.BLOCK_ANVIL_PLACE, 3f, 3f);
                target.sendMessage(Locale.getMessage("contract-received").replace("%player%", player.getName()).replace("%company%", companyName).replace("%second%", String.valueOf(respondTime)));
                target.playSound(target, Sound.BLOCK_ANVIL_PLACE, 3f, 3f);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean check(String name, ClickType clickType) {
        return false;
    }

    @Deprecated
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
