package p1xel.minecraft.bukkit.object.focus.tree.listener;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.object.effect.Effect;
import p1xel.minecraft.bukkit.object.effect.EffectType;
import p1xel.minecraft.bukkit.object.effect.MobDropEffect;
import p1xel.minecraft.bukkit.object.focus.CompanyFocus;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseInstance;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseListener;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseConfig;
import p1xel.minecraft.bukkit.object.math.Expression;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.ProbabilityUtil;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class MobDropListener extends ExpertiseListener {

    public MobDropListener(String name, EffectType effectType) {
        super(name, effectType);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLootGenerate(EntityDeathEvent event) {

        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }

        Entity killer = event.getDamageSource().getCausingEntity();
        if (!(killer instanceof Player player)) {
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

        // 如果没有和该 Effect 相关的 Expertise 被解锁，则不处理
        for (ExpertiseInstance expertise : focusManager.getExpertisesWithEffectType(companyFocus, this.effectType)) {
            // 如果该 Expertise 没有被解锁，则跳过
            if (!expertise.getUnlocked()) {
                continue;
            }

            List<ItemStack> loot = event.getDrops();

            // 处理
            ExpertiseConfig config = focusManager.getExpertiseConfig(expertise.getId());

            // 获得所有与该 Expertise 相关的 Effect
            for (Effect effectInstance : config.getEffect().getEffects().get(this.effectType)) {

                // 将 Effect 转换为 MobDropEffect
                MobDropEffect effect = (MobDropEffect) effectInstance;
                EntityType entityType = effect.getEntityType();

                if (entityType == null || entityType == entity.getType()) {

                    int amount = effect.getAmount();
                    double percentage = effect.getChance();
                    Material material = effect.getMaterial();
                    boolean affectAllDrops = material == null;

                    for (ItemStack drop : loot) {

                        if (!affectAllDrops && drop.getType() != material) {
                            continue;
                        }

                        boolean triggered = ProbabilityUtil.isTriggered(percentage);
                        Logger.debug(Level.INFO,  "MobDropListener: Expertise " + expertise.getId() + " triggered: " + triggered + " for entity: " + entityType + ", item: " + drop.getType() + ", amount: " + amount);

                        if (triggered) {

                            int newAmount = (int) Expression.evaluate(drop.getAmount(), amount, effect.getNumberType());
                            drop.setAmount(newAmount);
                        }
                    }
                }
            }

        }

    }

}
