package p1xel.minecraft.bukkit.managers.areas;

import org.bukkit.*;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.utils.Config;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AreaSelectionMode {

    private static HashMap<UUID, Location> firstBlocks = new HashMap<>();
    private static HashMap<UUID, Location> secondBlocks = new HashMap<>();
    private static ItemStack tool;
    private static NamespacedKey tool_key = new NamespacedKey("mycompany", "selection_tool");

    public static boolean isSelectionFinished(UUID playerUniqueId) {
        return firstBlocks.get(playerUniqueId) != null && secondBlocks.get(playerUniqueId) != null;
    }

    public static ItemStack getToolItem() {
        return tool;
    }

    public static void initTool() {
        tool = new ItemStack(Material.matchMaterial(Config.getString("company-area.selection-tool.material")));
        ItemMeta meta = tool.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Config.getString("company-area.selection-tool.display_name")));
        List<String> lore_list = Config.getStringList("company-area.selection-tool.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
        meta.setLore(lore_list);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(tool_key, PersistentDataType.STRING, "tool_item");
        tool.setItemMeta(meta);
    }

    public static boolean setBlock(UUID playerUniqueId, Action action, Location location) {
        if (action == Action.LEFT_CLICK_BLOCK) {
            if (firstBlocks.get(playerUniqueId) == null) {
                firstBlocks.put(playerUniqueId, location);
            } else {
                firstBlocks.replace(playerUniqueId, location);
            }
            // If the second block is already selected
            // Check the world location of the second block
            // If they don't have the same world location
            // Remove the second block
            if (secondBlocks.get(playerUniqueId) != null) {
                World secondWorld = secondBlocks.get(playerUniqueId).getWorld();
                if (location.getWorld() != secondWorld) {
                    secondBlocks.remove(playerUniqueId);
                }
            }
            return true;
        }

        if (secondBlocks.get(playerUniqueId) == null) {
            secondBlocks.put(playerUniqueId, location);
        } else {
            secondBlocks.replace(playerUniqueId, location);
        }
        // If the second block is already selected
        // Check the world location of the second block
        // If they don't have the same world location
        // Remove the second block
        if (firstBlocks.get(playerUniqueId) != null) {
            World firstWorld = firstBlocks.get(playerUniqueId).getWorld();
            if (location.getWorld() != firstWorld) {
                firstBlocks.remove(playerUniqueId);
            }
        }
        return false;
    }

    public static NamespacedKey getKey() {
        return tool_key;
    }

    public static HashMap<UUID, Location> getFBMap() { return firstBlocks; }
    public static HashMap<UUID, Location> getSBMap() { return secondBlocks; }


}
