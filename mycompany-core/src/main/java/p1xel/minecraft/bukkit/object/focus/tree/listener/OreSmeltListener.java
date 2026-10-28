package p1xel.minecraft.bukkit.object.focus.tree.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.object.effect.Effect;
import p1xel.minecraft.bukkit.object.effect.EffectType;
import p1xel.minecraft.bukkit.object.effect.OreSmeltEffect;
import p1xel.minecraft.bukkit.object.focus.CompanyFocus;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseInstance;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseListener;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseConfig;
import p1xel.minecraft.bukkit.util.Config;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.ProbabilityUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class OreSmeltListener extends ExpertiseListener {

    private final HashMap<Material, Material> oreToIngotMap = new HashMap<>();

    public OreSmeltListener(String name, EffectType effectType) {
        super(name, effectType);
        for (String materialId : Config.getConfigurationSection("reference.ore-to-smelted").getKeys(false)) {
            Material ore = Material.matchMaterial(materialId);
            Material ingot = Material.matchMaterial(Config.getString("reference.ore-to-smelted." + materialId));
            if (ore != null && ingot != null) {
                oreToIngotMap.put(ore, ingot);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockDropItemEvent event) {

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

            // 如果物品有 SILK_TOUCH 附魔，则不处理
            if (player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                continue;
            }

            List<Item> loot = new ArrayList<>(event.getItems());

            // 处理
            ExpertiseConfig config = focusManager.getExpertiseConfig(expertise.getId());

            // 获得所有与该 Expertise 相关的 Effect
            for (Effect effectInstance : config.getEffect().getEffects().get(this.effectType)) {

                // 将 Effect 转换为 BlockDropEffect
                OreSmeltEffect effect = (OreSmeltEffect) effectInstance;
                Material effectBlockMaterial = effect.getBlockMaterial();

                if (effectBlockMaterial == null || effectBlockMaterial == blockMaterial) {

                    double percentage = effect.getChance();

                    for (Item item : loot) {
                        ItemStack drop = item.getItemStack();

                        if (!oreToIngotMap.containsKey(drop.getType())) {
                            continue;
                        }

                        boolean triggered = ProbabilityUtil.isTriggered(percentage);
                        Logger.debug(Level.INFO,  "OreSmeltListener: Expertise " + expertise.getId() + " triggered: " + triggered + " for block: " + blockMaterial + ", item: " + drop.getType());

                        if (triggered) {
                            ItemStack ingot = drop.withType(oreToIngotMap.get(drop.getType()));
                            item.setItemStack(ingot);

                        }
                    }
                }
            }

        }

    }

}
