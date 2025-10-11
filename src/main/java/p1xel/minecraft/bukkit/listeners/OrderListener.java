package p1xel.minecraft.bukkit.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import p1xel.minecraft.bukkit.EmployeeOrder;
import p1xel.minecraft.bukkit.utils.Logger;
import p1xel.minecraft.bukkit.utils.storage.EmployeeOrders;

import java.util.UUID;
import java.util.logging.Level;

public class OrderListener implements Listener {

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

}
