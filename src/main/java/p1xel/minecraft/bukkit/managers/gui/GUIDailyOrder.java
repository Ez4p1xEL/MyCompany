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
import p1xel.minecraft.bukkit.EmployeeOrder;
import p1xel.minecraft.bukkit.utils.storage.EmployeeOrders;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GUIDailyOrder extends GUIAbstract implements InventoryHolder {

    private UUID playerUniqueId;
    private String action;
    private int page;
    private boolean hasCompany;
    private UUID companyUniqueId;
    private Inventory inventory;

    public GUIDailyOrder(UUID playerUniqueId, String action, int page) {
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
        Inventory inventory = Bukkit.createInventory(this, 45, Locale.getMessage("menu.orders.title").replaceAll("%company%", companyName));
        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (page > 1) {
            hasPreviousPage = true;
        }

        List<String> orders = userManager.getDailyOrders(playerUniqueId);

        if (orders.size() >= page*14) {
            hasNextPage = true;
        }
        orders.subList(14 * (page-1), Math.min((page) * 14, orders.size()));

        ItemStack orders_info = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.orders.items.orders_info.material")), 1);
        ItemMeta orders_info_meta = orders_info.getItemMeta();
        orders_info_meta.setDisplayName(Locale.getMessage("menu.orders.items.orders_info.display_name").replaceAll("%company%", companyName));
        List<String> orders_info_lore = Locale.yaml.getStringList("menu.orders.items.orders_info.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
        orders_info_meta.setLore(orders_info_lore);
        PersistentDataContainer company_info_container = orders_info_meta.getPersistentDataContainer();
        company_info_container.set(menu_id_key, PersistentDataType.STRING, "orders_info");
        orders_info.setItemMeta(orders_info_meta);
        inventory.setItem(4, orders_info);

        if (hasNextPage) {

            ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.orders.items.next_page.material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Locale.getMessage("menu.orders.items.next_page.display_name"));
            List<String> lore = Locale.yaml.getStringList("menu.orders.items.next_page.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "next_page");
            item.setItemMeta(meta);
            inventory.setItem(5, item);
        }

        if (hasPreviousPage) {

            ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.orders.items.previous_page.material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Locale.getMessage("menu.orders.items.previous_page.display_name"));
            List<String> lore = Locale.yaml.getStringList("menu.orders.items.previous_page.lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "previous_page");
            item.setItemMeta(meta);
            inventory.setItem(3, item);
        }

        ItemStack item = new ItemStack(Material.matchMaterial(Locale.getMessage("menu.orders.items.back_to_main.material")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Locale.getMessage("menu.orders.items.back_to_main.display_name"));
        List<String> lore = Locale.yaml.getStringList("menu.orders.items.back_to_main.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(menu_id_key, PersistentDataType.STRING, "back_to_main");
        item.setItemMeta(meta);
        inventory.setItem(36, item);

        this.inventory = inventory;
        update(orders);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean check(String name) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        if (name.equalsIgnoreCase("orders_info")) {
            return true;
        }
        if (name.equalsIgnoreCase("next_page")) {
            player.openInventory(new GUIDailyOrder(playerUniqueId, action, 1).getInventory());
            return true;
        }
        if (name.equalsIgnoreCase("previous_page")) {
            player.openInventory(new GUIDailyOrder(playerUniqueId, action, Math.min(1,page-1)).getInventory());
            return true;
        }

        if (name.equalsIgnoreCase("back_to_main")) {
            player.openInventory(new GUIMain(playerUniqueId).getInventory());
            return true;
        }
        return true;
    }

    public void update(List<String> orders) {
        int slot = 20;
        for (String order : orders) {
            if (slot == 25) {
                slot = 29;
            }

            String material = Locale.getMessage("menu.orders.items.order.material");
            String[] args = material.split(";");
            material = args[0];

            ItemStack item = new ItemStack(Material.matchMaterial(material));
            ItemMeta meta = item.getItemMeta();
            String employeeName = Bukkit.getOfflinePlayer(playerUniqueId).getName();
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("player_head")) {
                    SkullMeta skullMeta = (SkullMeta) meta;
                    skullMeta.setOwner(employeeName);
                }
            }
            //String position = userManager.getPosition(employeeUniqueId);
            meta.setDisplayName(Locale.getMessage("menu.orders.items.order.display_name").replaceAll("%label%", EmployeeOrders.getLabel(order)).replaceAll("%order%", order));

            List<String> rewards = EmployeeOrders.getRewards(order);
            List<String> rewardList = new ArrayList<>();
            for (String reward : rewards) {
                String[] split = reward.split(";");
                String type = split[0];
                if (type.equalsIgnoreCase("item")) {
                    String r = Locale.getMessage("rewards.item").replaceAll("%item%", split[1]).replaceAll("%amount%", split[2]);
                    rewardList.add(r);
                    continue;
                }
                if (type.equalsIgnoreCase("company_income")) {
                    String r = Locale.getMessage("rewards.company_income").replaceAll("%income%", split[1]);
                    rewardList.add(r);
                    continue;
                }
                if (type.equalsIgnoreCase("money")) {
                    String r = Locale.getMessage("rewards.money").replaceAll("%money%", split[1]);
                    rewardList.add(r);
                    continue;
                }if (type.equalsIgnoreCase("experience")) {
                    String r = Locale.getMessage("rewards.experience").replaceAll("%exp%", split[1]);
                    rewardList.add(r);

                }

            }

            Set<String> quests = EmployeeOrders.getQuests(order);
            List<String> questList = new ArrayList<>();

            HashMap<String, EmployeeOrder> hashmap = EmployeeOrders.getPlayerOrders(playerUniqueId);
            if (hashmap == null) {
                questList.add(Locale.getMessage("order-finished"));
            } else {

                EmployeeOrder employeeOrder = hashmap.get(order);
                if (employeeOrder == null) {
                    questList.add(Locale.getMessage("order-finished"));
                } else {
                    for (String quest : quests) {
                        questList.add(EmployeeOrders.getProgressMessage(playerUniqueId, order, quest));
                    }
                }
            }


            List<String> lore_list = Locale.yaml.getStringList("menu.orders.items.order.lore").stream()
                    .flatMap(line -> {
                        line = ChatColor.translateAlternateColorCodes('&', line);
                        line = line.replaceAll("%label%", EmployeeOrders.getLabel(order));
                        line = line.replaceAll("%order%", order);
                        if (line.contains("%order_reward%")) {
                            return rewardList.stream(); // 替換為多行
                        }
                        if (line.contains("%quest_progress%")) {
                            return questList.stream();
                        }

                        if (line.contains("%description%")) {
                            return EmployeeOrders.getDescription(order).stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).collect(Collectors.toList()).stream();
                        }
                        return Stream.of(line); // 保留原本的行
                        }).collect(Collectors.toList());
            meta.setLore(lore_list);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, "order:" + order);
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
