package p1xel.minecraft.bukkit.object.focus.tree.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.object.effect.BlockDropEffect;
import p1xel.minecraft.bukkit.object.effect.Effect;
import p1xel.minecraft.bukkit.object.effect.EffectType;
import p1xel.minecraft.bukkit.object.focus.CompanyFocus;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseInstance;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseListener;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseConfig;
import p1xel.minecraft.bukkit.object.math.Expression;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.ProbabilityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class BlockDropListener extends ExpertiseListener {

    public BlockDropListener(String name, EffectType effectType) {
        super(name, effectType);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDrops(BlockDropItemEvent event) {

        BlockState block = event.getBlockState();
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

            List<Item> loot = new ArrayList<>(event.getItems());

            // 处理
            ExpertiseConfig config = focusManager.getExpertiseConfig(expertise.getId());

            // 获得所有与该 Expertise 相关的 Effect
            for (Effect effectInstance : config.getEffect().getEffects().get(this.effectType)) {

                // 将 Effect 转换为 BlockDropEffect
                BlockDropEffect effect = (BlockDropEffect) effectInstance;
                Material effectBlockMaterial = effect.getBlockMaterial();

                if (effectBlockMaterial == null || effectBlockMaterial == blockMaterial) {

                    int amount = effect.getAmount();
                    double percentage = effect.getChance();
                    Material material = effect.getMaterial();
                    boolean affectAllDrops = material == null;

                    for (Item item : loot) {
                        ItemStack drop = item.getItemStack();

                        if (!affectAllDrops && drop.getType() != material) {
                            continue;
                        }

                        boolean triggered = ProbabilityUtil.isTriggered(percentage);
                        Logger.debug(Level.INFO,  "BlockDropListener: Expertise " + expertise.getId() + " triggered: " + triggered + " for block: " + blockMaterial + ", item: " + drop.getType() + ", amount: " + amount);

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
