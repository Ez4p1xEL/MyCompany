package p1xel.minecraft.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import p1xel.minecraft.bukkit.MyCompany;

import java.util.UUID;

public class UserCreation implements Listener {

    @EventHandler
    public void onUserLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        MyCompany.getCacheManager().getUserManager().createUser(uniqueId);


    }



}
