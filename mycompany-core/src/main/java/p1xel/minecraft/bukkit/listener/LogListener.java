package p1xel.minecraft.bukkit.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.ManagerSupport;
import p1xel.minecraft.bukkit.object.User;

import java.util.UUID;

public class LogListener extends ManagerSupport implements Listener {

    public void checkAndAdd(Player player, String logId, double value) {
        UUID playerUniqueId = player.getUniqueId();
        User user = userManager.getUser(playerUniqueId);
        if (user == null) {
            return;
        }

        UUID companyUniqueId = user.getCompanyUUID();
        if (companyUniqueId == null) {
            return;
        }

        Company company = companyManager.getCompany(companyUniqueId);
        if (company == null) {
            return;
        }

        companyManager.addLogValue(companyUniqueId, logId, value);
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {

        Entity killer = event.getDamageSource().getCausingEntity();
        if (killer == null) {
            return;
        }

        if (!(killer instanceof Player player)) {
            return;
        }

        checkAndAdd(player, "mob_kill_count_" + event.getEntityType().toString(), 1.0);

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String materialName = block.getType().toString();

        checkAndAdd(player, "block_break_count_" + materialName, 1.0);
    }

}
