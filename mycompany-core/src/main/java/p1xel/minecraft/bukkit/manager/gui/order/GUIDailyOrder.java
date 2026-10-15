package p1xel.minecraft.bukkit.manager.gui.order;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUIMain;
import p1xel.minecraft.bukkit.object.EmployeeOrder;
import p1xel.minecraft.bukkit.util.storage.EmployeeOrders;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GUIDailyOrder extends CompanyMenu {

    private String action;
    private int page;

    private String companyName;
    private String position;
    private String positionLabel;

    public GUIDailyOrder(UUID playerUniqueId, String action, int page) {
        super(playerUniqueId);
        this.action = action;
        this.page = page;
        this.menu = new Menu(MenuUtil.getMenu("orders"));
        if (hasCompany) {
            init();
        }
    }

    @Override
    public void init() {
        this.position = user.getPosition();
        this.companyName = company.getName();
        this.positionLabel = company.getPositionLabel(position);
        Inventory inventory = Bukkit.createInventory(this, menu.getSize(), menu.getTitle().replace("%company%", companyName));

        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (page > 1) {
            hasPreviousPage = true;
        }

        List<String> orders = userManager.getDailyOrders(playerUniqueId);
        int order_per_page = menu.getInt("order-per-page", 5);

        if (orders.size() > page*order_per_page) {
            hasNextPage = true;
        }
        orders = orders.subList(order_per_page * (page-1), Math.min((page) * order_per_page, orders.size()));

        // 显示previous_page 和 next_page按钮
        if (!hasPreviousPage) {
            menu.getItems().remove("previous_page");
        }
        if (!hasNextPage) {
            menu.getItems().remove("next_page");
        }

        Map<String, String> placeholders = Map.ofEntries(
                Map.entry("%player%", playerName),
                Map.entry("%company%", companyName),
                Map.entry("%position%", position),
                Map.entry("%label%", positionLabel)
        );

        // 复制 order 的 item
        if (orders.size() < order_per_page) {
            order_per_page = orders.size();
        }
        // 当前 order 的位置, 再获取它与当前行首个slot的位置的距离
        int current_slot = menu.getItems().get("order").getSlots().getFirst();
        int line = (current_slot / 9)+1;
        int distance = current_slot - ((line-1)*9);

        int slot = current_slot;

        MenuItem orderItem = menu.getItems().get("order").clone();
        menu.getItems().remove("order");

        for (int i = 0; i < order_per_page; i++) {

            // 计算当前行数
            int current_line = (slot / 9)+1;
            // 如果当前 slot 超过了当前行的最后一个 available slot, 则跳到下一行
            if (slot >= (current_line*9)-distance) {
                slot += distance+distance;
            }

            // 复制 orderItem
            MenuItem cloneOrderItem = orderItem.clone();
            cloneOrderItem.setSlots(Collections.singletonList(slot));
            cloneOrderItem.setId(orderItem.getId() + "_" + slot);

            // replace placeholders
            String orderName = orders.get(i);
            String orderLabel = user.getEmployeeOrder(orderName).getLabel();
            List<String> orderDescription = user.getEmployeeOrder(orderName).getDescription();

            // reward list
            List<String> rewards = EmployeeOrders.getRewards(orderName);
            List<String> rewardList = new ArrayList<>();
            for (String reward : rewards) {
                String[] split = reward.split(";");
                String type = split[0];
                if (type.equalsIgnoreCase("item")) {
                    String itemName = split[1];
                    Material material = Material.matchMaterial(itemName);
                    itemName = Locale.deserialize(Component.translatable(material.translationKey()));
                    String r = Locale.yaml.getString("rewards.item").replace("%item%", itemName).replace("%amount%", split[2]);
                    rewardList.add(r);
                    continue;
                }
                if (type.equalsIgnoreCase("company_income")) {
                    String r = Locale.yaml.getString("rewards.company_income").replace("%income%", split[1]);
                    rewardList.add(r);
                    continue;
                }
                if (type.equalsIgnoreCase("money")) {
                    String r = Locale.yaml.getString("rewards.money").replace("%money%", split[1]);
                    rewardList.add(r);
                    continue;
                }if (type.equalsIgnoreCase("experience")) {
                    String r = Locale.yaml.getString("rewards.experience").replace("%exp%", split[1]);
                    rewardList.add(r);

                }

            }

            // quest list
            Set<String> quests = EmployeeOrders.getQuests(orderName);
            List<String> questList = new ArrayList<>();

            HashMap<String, EmployeeOrder> hashmap = EmployeeOrders.getPlayerOrders(playerUniqueId);
            if (hashmap == null) {
                questList.add(Locale.yaml.getString("order-finished"));
            } else {

                EmployeeOrder employeeOrder = hashmap.get(orderName);
                if (employeeOrder == null) {
                    questList.add(Locale.yaml.getString("order-finished"));
                } else {
                    for (String quest : quests) {
                        questList.add(EmployeeOrders.getProgressMessage(playerUniqueId, orderName, quest));
                    }
                }
            }

            Map<String, String> orderPlaceholders = Map.ofEntries(
                    Map.entry("%order%", orderName),
                    Map.entry("%label%", orderLabel),
                    Map.entry("%description%", String.join("\n", orderDescription)),
                    Map.entry("%order_reward%", String.join("\n", rewardList)),
                    Map.entry("%quest_progress%", String.join("\n", questList))
            );

            cloneOrderItem = replacePlaceholdersInMenuItems(cloneOrderItem, orderPlaceholders);
            cloneOrderItem.setCustomKey("order:" + orderName);
            menu.getItems().put(cloneOrderItem.getId(), cloneOrderItem);
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
        if (name.equalsIgnoreCase("orders_info")) {
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
            return true;
        }
        if (name.equalsIgnoreCase("next_page")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.openInventory(new GUIDailyOrder(playerUniqueId, action, page+1).getInventory());
            return true;
        }
        if (name.equalsIgnoreCase("previous_page")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.openInventory(new GUIDailyOrder(playerUniqueId, action, Math.min(1,page-1)).getInventory());
            return true;
        }

        if (name.equalsIgnoreCase("back_to_main")) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
            player.openInventory(new GUIMain(playerUniqueId).getInventory());
            return true;
        }
        return true;
    }

    @Override
    public boolean check(String name, ClickType clickType) {
        return false;
    }

    @Deprecated
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
