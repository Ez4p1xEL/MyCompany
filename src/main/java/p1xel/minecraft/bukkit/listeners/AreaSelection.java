package p1xel.minecraft.bukkit.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.AreaManager;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.managers.areas.AreaSelectionMode;
import p1xel.minecraft.bukkit.managers.areas.CompanyArea;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AreaSelection implements Listener {

    private final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();
    private final AreaManager areaManager = MyCompany.getCacheManager().getAreaManager();

    @EventHandler
    public void onBlockSelect(PlayerInteractEvent event) {

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) { return; }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) { return; }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = AreaSelectionMode.getKey();
        if (!container.has(key)) { return; }
        if (!container.get(key, PersistentDataType.STRING).equalsIgnoreCase("tool_item")) { return; }

        Player player = event.getPlayer();
        UUID playerUniqueId = player.getUniqueId();
        Block clicked = event.getClickedBlock();
        Location location = clicked.getLocation();

        if (AreaSelectionMode.setBlock(playerUniqueId, action, location)) {
            player.sendMessage(Locale.getMessage("first-block-selected")
                    .replaceAll("%x%", String.valueOf(clicked.getX()))
                    .replaceAll("%y%", String.valueOf(clicked.getY()))
                    .replaceAll("%z%", String.valueOf(clicked.getZ())));
        } else {
            player.sendMessage(Locale.getMessage("second-block-selected")
                    .replaceAll("%x%", String.valueOf(clicked.getX()))
                    .replaceAll("%y%", String.valueOf(clicked.getY()))
                    .replaceAll("%z%", String.valueOf(clicked.getZ())));
        }

        event.setCancelled(true);

    }

    private Map<UUID, CompanyArea> playerAreas = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // Prevent checking without moving
        if (from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        CompanyArea oldArea = playerAreas.get(player.getUniqueId());
        CompanyArea newArea = areaManager.getAreaByLoc(to);

        if (oldArea != newArea) {
            if (oldArea != null) {
                String areaName = oldArea.getName();
                String companyName = companyManager.getName(oldArea.getCompanyUUID());
                String message = Locale.getMessage("area-left").replaceAll("%area%", areaName).replaceAll("%company%", companyName);
                player.sendActionBar(Component.text(message));
            }
            if (newArea != null) {
                String areaName = newArea.getName();
                String companyName = companyManager.getName(newArea.getCompanyUUID());
                String message = Locale.getMessage("area-enter").replaceAll("%area%", areaName).replaceAll("%company%", companyName);
                player.sendActionBar(Component.text(message));
            }
            playerAreas.put(player.getUniqueId(), newArea);
        }
    }

}
