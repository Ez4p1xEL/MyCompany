package p1xel.minecraft.bukkit.object.focus.tree.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.object.effect.Effect;
import p1xel.minecraft.bukkit.object.effect.EffectType;
import p1xel.minecraft.bukkit.object.effect.ItemDurabilityConsumptionReductionEffect;
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

public class ItemDurabilityListener extends ExpertiseListener {

    private final HashMap<String, List<Material>> typeToItems = new HashMap<>();

    public ItemDurabilityListener(String name, EffectType effectType) {
        super(name, effectType);
        for (String typeName : Config.getConfigurationSection("reference.item-type").getKeys(false)) {
            List<String> itemList = Config.getStringList("reference.item-type." + typeName);
            for (String materialId : itemList) {
                Material material = Material.matchMaterial(materialId);
                if (material != null) {
                    typeToItems.computeIfAbsent(typeName, k -> new ArrayList<>()).add(material);
                } else {
                    Logger.debug(Level.WARNING, "ItemDurabilityListener: Material not found for ID: " + materialId);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(PlayerItemDamageEvent event) {

        ItemStack item = event.getItem();
        Material material = item.getType();
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

                // 将 Effect 转换为 ItemDurabilityConsumptionReductionEffect
                ItemDurabilityConsumptionReductionEffect effect = (ItemDurabilityConsumptionReductionEffect) effectInstance;
                boolean applyForAllItems = effect.isApplyForAllItems();
                String materialId = effect.getItemMaterialId();
                Material itemMaterial = effect.getItemMaterial();

                if (applyForAllItems || (materialId != null && material.equals(itemMaterial)) || (typeToItems.containsKey(materialId) && typeToItems.get(materialId).contains(material))) {

                    double chance = effect.getChance(); // trigger chance
                    double percentage = effect.getPercentage(); // reduce durability consumption percentage

                    boolean triggered = ProbabilityUtil.isTriggered(chance);
                    Logger.debug(Level.INFO, "ItemDurabilityListener: Expertise " + expertise.getId() + " triggered: " + triggered + " for item: " + material);

                    if (triggered) {
                        event.setDamage((int) Math.floor(event.getDamage() * (1 - percentage)));
                    }
                }

            }

        }

    }
}
