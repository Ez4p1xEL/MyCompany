package p1xel.minecraft.bukkit.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import p1xel.minecraft.bukkit.EmployeeOrder;
import p1xel.minecraft.bukkit.utils.Logger;
import p1xel.minecraft.bukkit.utils.storage.EmployeeOrders;

import java.util.UUID;
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

    @EventHandler
    public void onBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        UUID playerUniqueId = player.getUniqueId();

        if (EmployeeOrders.getAllOrders().get(playerUniqueId).isEmpty()) {
            return;
        }

        Block block = event.getBlock();
        Material material = block.getType();
        for (EmployeeOrder employeeOrder : EmployeeOrders.getAllOrders().get(playerUniqueId).values()) {
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
                        employeeOrder.addProgressValue(string, 1);
                        Logger.debug(Level.INFO, "Added progress value");
                    }
                }
            }
        }

    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        UUID playerUniqueId = player.getUniqueId();

        if (EmployeeOrders.getAllOrders().get(playerUniqueId).isEmpty()) {
            return;
        }

        Block block = event.getBlock();
        Material material = block.getType();
        for (EmployeeOrder employeeOrder : EmployeeOrders.getAllOrders().get(playerUniqueId).values()) {
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
                        employeeOrder.addProgressValue(string, 1);
                        Logger.debug(Level.INFO, "Added progress value");
                    }
                }
            }
        }

    }

    @EventHandler
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
        for (EmployeeOrder employeeOrder : EmployeeOrders.getAllOrders().get(playerUniqueId).values()) {
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
                        employeeOrder.addProgressValue(string, 1);
                        Logger.debug(Level.INFO, "Added progress value");
                    }
                }
            }
        }

    }

}
