package p1xel.minecraft.bukkit.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.manager.UserManager;
import p1xel.minecraft.bukkit.util.storage.EmployeeOrders;

import java.util.UUID;

public class UserCreation implements Listener {

    private final UserManager userManager = MyCompany.getCacheManager().getUserManager();

    @EventHandler
    public void onUserLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        if (userManager.getDailyOrders(uniqueId).isEmpty()) {
            EmployeeOrders.createEmptyMap(uniqueId);
            userManager.randomizeDailyOrder(uniqueId);
        } else {
            // Create cache of employee orders
            EmployeeOrders.saveToCache(uniqueId);
        }

        // Create cache or user file
        userManager.getUser(uniqueId, true);

    }

    @EventHandler
    public void onUserLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        userManager.removeUserFromCache(uniqueId);
    }



}
