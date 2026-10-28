package p1xel.minecraft.bukkit.object.focus.tree.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import p1xel.minecraft.bukkit.object.effect.Effect;
import p1xel.minecraft.bukkit.object.effect.EffectType;
import p1xel.minecraft.bukkit.object.effect.MiningHasteEffect;
import p1xel.minecraft.bukkit.object.focus.CompanyFocus;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseInstance;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseListener;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseConfig;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.ProbabilityUtil;

import java.util.UUID;
import java.util.logging.Level;

public class MiningHasteListener extends ExpertiseListener {

    public MiningHasteListener(String name, EffectType effectType) {
        super(name, effectType);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {

        Block block = event.getBlock();
        Material blockMaterial = block.getType();
        Player player = event.getPlayer();

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

            // 处理
            ExpertiseConfig config = focusManager.getExpertiseConfig(expertise.getId());

            // 获得所有与该 Expertise 相关的 Effect
            for (Effect effectInstance : config.getEffect().getEffects().get(this.effectType)) {

                // 将 Effect 转换为 BlockDropItemEffect
                MiningHasteEffect effect = (MiningHasteEffect) effectInstance;
                Material effectBlockMaterial = effect.getBlockMaterial();

                if (effectBlockMaterial == null || effectBlockMaterial == blockMaterial) {

                    double percentage = effect.getChance();
                    int amplifier = effect.getAmplifier();
                    int duration = effect.getDuration();

                    boolean triggered = ProbabilityUtil.isTriggered(percentage);
                    Logger.debug(Level.INFO,  "MiningHasteListener: Expertise " + expertise.getId() + " triggered: " + triggered + " for block: " + blockMaterial);

                    if (triggered) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, amplifier));
                    }
                }
            }

        }

    }

}
