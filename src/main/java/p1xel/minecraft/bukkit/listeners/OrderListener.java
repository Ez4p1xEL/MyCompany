package p1xel.minecraft.bukkit.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.EmployeeOrder;
import p1xel.minecraft.bukkit.utils.Logger;
import p1xel.minecraft.bukkit.utils.storage.EmployeeOrders;

import java.util.*;
import java.util.logging.Level;

public class OrderListener implements Listener {

    /*
    Step of adding action:
    1. Add action event listener here.
    2. Add sample order in orders.yml.
    3. Add translation text in language files
    4. Configure Employees.getProgressMessage().

    新增action:
    1. 加事件监听
    2. 在orders.yml新增订单例子
    3. 在语言文件新增翻译文本
    4. 前往 Employees.getProgressMessage() 配置
     */

    // 列出所有Minecraft里的动物和它们对应的食物，方便后续添加喂食任务
    // 使用 EntityType -> List<Material> 支持每种动物对应多个可用食物，且避免字符串拼写问题
    private static final Map<EntityType, List<Material>> ANIMAL_FEEDING_FOODS;
    static {
        Map<EntityType, List<Material>> map = new EnumMap<>(EntityType.class);
        map.put(EntityType.COW, List.of(Material.WHEAT));
        map.put(EntityType.SHEEP, List.of(Material.WHEAT));
        map.put(EntityType.PIG, List.of(Material.CARROT));
        map.put(EntityType.CHICKEN, List.of(Material.WHEAT_SEEDS));
        map.put(EntityType.HORSE, List.of(Material.WHEAT));
        map.put(EntityType.LLAMA, List.of(Material.WHEAT));
        map.put(EntityType.RABBIT, List.of(Material.CARROT));
        map.put(EntityType.FOX, List.of(Material.SWEET_BERRIES));
        map.put(EntityType.TURTLE, List.of(Material.SEAGRASS));
        map.put(EntityType.WOLF, List.of(Material.BONE));
        map.put(EntityType.CAT, List.of(Material.COD, Material.SALMON));
        map.put(EntityType.PARROT, List.of(Material.WHEAT_SEEDS));
        ANIMAL_FEEDING_FOODS = Collections.unmodifiableMap(map);
    }

    // Helper getter to query allowed feeding materials for a given entity type
    public static List<Material> getFeedingMaterials(EntityType type) {
        return ANIMAL_FEEDING_FOODS.getOrDefault(type, Collections.emptyList());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        UUID playerUniqueId = player.getUniqueId();

        if (EmployeeOrders.getAllOrders().get(playerUniqueId).isEmpty()) {
            return;
        }

        Block block = event.getBlock();
        Material material = block.getType();
        final Object[] orderNames = EmployeeOrders.getAllOrders().get(playerUniqueId).keySet().toArray().clone();
        for (Object obj : orderNames) {
            String orderName = (String) obj;
            EmployeeOrder employeeOrder = EmployeeOrders.getPlayerOrders(playerUniqueId).get(orderName);
            for (String string : employeeOrder.getValues().keySet()) {
                String[] split = string.split(":");
                // 0 = questName, 1 = actionName
                String action = split[1];
                String quest = split[0];
                if (employeeOrder.isFinished(quest)) {
                    continue;
                }
                if (action.equalsIgnoreCase("break_block")) {
                    Logger.debug(Level.INFO, "Quest break_block detected");
                    if (((String) EmployeeOrders.getValue(employeeOrder.getName(), quest, "item")).equalsIgnoreCase(material.toString())) {
                        //employeeOrder.addProgressValue(string, 1);
                        EmployeeOrders.addProgressValue(playerUniqueId, orderName, string, 1);
                        Logger.debug(Level.INFO, "Added progress value");
                    }
                }
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        UUID playerUniqueId = player.getUniqueId();

        if (EmployeeOrders.getAllOrders().get(playerUniqueId).isEmpty()) {
            return;
        }

        Block block = event.getBlock();
        Material material = block.getType();
        final Object[] orderNames = EmployeeOrders.getAllOrders().get(playerUniqueId).keySet().toArray().clone();
        for (Object obj : orderNames) {
            String orderName = (String) obj;
            EmployeeOrder employeeOrder = EmployeeOrders.getPlayerOrders(playerUniqueId).get(orderName);
            for (String string : employeeOrder.getValues().keySet()) {
                String[] split = string.split(":");
                // 0 = questName, 1 = actionName
                String action = split[1];
                String quest = split[0];
                if (employeeOrder.isFinished(quest)) {
                    continue;
                }
                if (action.equalsIgnoreCase("place_block")) {
                    Logger.debug(Level.INFO, "Quest place_block detected");
                    if (((String) EmployeeOrders.getValue(employeeOrder.getName(), quest, "item")).equalsIgnoreCase(material.toString())) {
                        EmployeeOrders.addProgressValue(playerUniqueId, orderName, string, 1);
                        Logger.debug(Level.INFO, "Added progress value");
                    }
                }
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamageMob(EntityDeathEvent event) {

        Entity entity = event.getEntity();
        if (entity instanceof Player) { return; }
        Entity damager = event.getDamageSource().getCausingEntity();
        if (!(damager instanceof Player player)) { return; }

        UUID playerUniqueId = player.getUniqueId();

        if (EmployeeOrders.getAllOrders().get(playerUniqueId).isEmpty()) {
            return;
        }

        EntityType type = entity.getType();
        final Object[] orderNames = EmployeeOrders.getAllOrders().get(playerUniqueId).keySet().toArray().clone();
        for (Object obj : orderNames) {
            String orderName = (String) obj;
            EmployeeOrder employeeOrder = EmployeeOrders.getPlayerOrders(playerUniqueId).get(orderName);
            for (String string : employeeOrder.getValues().keySet()) {
                String[] split = string.split(":");
                // 0 = questName, 1 = actionName
                String action = split[1];
                String quest = split[0];
                if (employeeOrder.isFinished(quest)) {
                    continue;
                }
                if (action.equalsIgnoreCase("mob_kill")) {
                    Logger.debug(Level.INFO, "Quest mob_kill detected");
                    if (((String) EmployeeOrders.getValue(employeeOrder.getName(), quest, "mob")).equalsIgnoreCase(type.toString())) {
                        EmployeeOrders.addProgressValue(playerUniqueId, orderName, string, 1);
                        Logger.debug(Level.INFO, "Added progress value");
                    }
                }
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFish(PlayerFishEvent event) {

        Entity caught = event.getCaught();
        if (caught == null) { return; }
        Player player = event.getPlayer();
        UUID playerUniqueId = player.getUniqueId();

        if (EmployeeOrders.getAllOrders().get(playerUniqueId).isEmpty()) {
            return;
        }

        Item item = (Item) caught;
        ItemStack itemStack = item.getItemStack();
        Material material = itemStack.getType();
        final Object[] orderNames = EmployeeOrders.getAllOrders().get(playerUniqueId).keySet().toArray().clone();
        for (Object obj : orderNames) {
            String orderName = (String) obj;
            EmployeeOrder employeeOrder = EmployeeOrders.getPlayerOrders(playerUniqueId).get(orderName);
            for (String string : employeeOrder.getValues().keySet()) {
                String[] split = string.split(":");
                // 0 = questName, 1 = actionName
                String action = split[1];
                String quest = split[0];
                if (employeeOrder.isFinished(quest)) {
                    continue;
                }
                if (action.equalsIgnoreCase("fish")) {
                    Logger.debug(Level.INFO, "Quest fish detected");
                    if (((String) EmployeeOrders.getValue(employeeOrder.getName(), quest, "item")).equalsIgnoreCase(material.toString())) {
                        EmployeeOrders.addProgressValue(playerUniqueId, orderName, string, 1);
                        Logger.debug(Level.INFO, "Added progress value");
                    }
                }
            }
        }

    }



    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFeed(PlayerInteractAtEntityEvent event) {

        Player player = event.getPlayer();
        UUID playerUniqueId = player.getUniqueId();

        if (EmployeeOrders.getAllOrders().get(playerUniqueId).isEmpty()) {
            return;
        }

        Entity entity = event.getRightClicked();
        EntityType type = entity.getType();

        // Check the item in player's main hand and see if it's allowed for this entity type
        ItemStack held = player.getInventory().getItemInMainHand();
        Material heldMat = held.getType();
        if (heldMat == Material.AIR) {
            held = player.getInventory().getItemInOffHand();
            heldMat = held.getType();
            if (heldMat == Material.AIR) {
                return;
            }
        }
        List<Material> allowed = getFeedingMaterials(type);
        final Object[] orderNames = EmployeeOrders.getAllOrders().get(playerUniqueId).keySet().toArray().clone();
        for (Object obj : orderNames) {
            String orderName = (String) obj;
            EmployeeOrder employeeOrder = EmployeeOrders.getPlayerOrders(playerUniqueId).get(orderName);
            for (String string : employeeOrder.getValues().keySet()) {
                String[] split = string.split(":");
                // 0 = questName, 1 = actionName
                String action = split[1];
                String quest = split[0];
                if (employeeOrder.isFinished(quest)) {
                    continue;
                }
                if (action.equalsIgnoreCase("feed")) {
                    Logger.debug(Level.INFO, "Quest feed detected");
                    Object cfg = EmployeeOrders.getValue(employeeOrder.getName(), quest, "mob");
                    String expected = cfg.toString();
                    // Progress only if the held item is allowed for this entity AND matches the configured item (if any)
                    if (allowed.contains(heldMat) && expected.equalsIgnoreCase(type.toString())) {
                        EmployeeOrders.addProgressValue(playerUniqueId, orderName, string, 1);
                        Logger.debug(Level.INFO, "Added progress value");
                    }
                }
            }
        }

    }

}
