package p1xel.minecraft.bukkit.object.focus.tree.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.manager.CacheManager;
import p1xel.minecraft.bukkit.manager.CompanyManager;
import p1xel.minecraft.bukkit.manager.UserManager;
import p1xel.minecraft.bukkit.object.focus.CompanyFocus;
import p1xel.minecraft.bukkit.object.focus.Focus;
import p1xel.minecraft.bukkit.object.focus.FocusManager;
import p1xel.minecraft.bukkit.object.focus.Mob;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseListener;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MobDropListener extends ExpertiseListener {

    public MobDropListener(String name, String... expertiseIds) {
        super(name, expertiseIds);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        Entity entity = event.getDamageSource().getCausingEntity();

        if (!(entity instanceof Player player)) {
            return;
        }

        UUID playerUniqueId = player.getUniqueId();
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (companyUniqueId == null) {
            return;
        }

        CompanyFocus companyFocus = focusManager.getCompanyFocus(companyUniqueId);
        if (companyFocus == null) {
            return;
        }

        if (!(companyFocus.getFocus() instanceof Mob mob)) {
            return;
        }

        if (mob.getExpertise("more_drops_level1").getUnlocked()) {
            List<ItemStack> drops = event.getDrops();
            for (ItemStack drop : drops) {
                int randomNum = ThreadLocalRandom.current().nextInt(100) + 1;
                if (randomNum <= 25) {
                    drop.setAmount(drop.getAmount()+1);
                }
            }
        }
    }

}
