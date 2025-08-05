package p1xel.minecraft.bukkit.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.managers.ShopManager;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.storage.Locale;
import p1xel.minecraft.bukkit.utils.storage.Shop;

import java.util.UUID;

public class ShopListener implements Listener {

    private final NamespacedKey shopKey = new NamespacedKey("mycompany", "is_company_shop");
    private final NamespacedKey companyKey = new NamespacedKey("mycompany", "company");
    private final NamespacedKey shopUUIDKey = new NamespacedKey("mycompany", "shop_uuid");
    private final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();
    private final ShopManager shopManager = MyCompany.getCacheManager().getShopManager();

    @EventHandler
    public void onShopCreated(SignChangeEvent event) {

        Block block = event.getBlock();
        // Check if it is wall sign
        if (!(block.getState().getBlockData() instanceof WallSign)) {
            return;
        }

        // Check the content of the sign
        if (!event.getLine(0).equalsIgnoreCase(Config.getString("chest-shop.sign-create.identifier"))) {
            return;
        }

        double price = 0.0;
        try {
            price = Double.parseDouble(event.getLine(1));
        } catch (NumberFormatException ignored) {
        }

        if (price <= 0) {
            event.setLine(1, Locale.getMessage("shop.status.something-wrong"));
            return;
        }

        //WallSign signData  = (WallSign) block.getState().getBlockData();
        //BlockFace attached  = signData.getFacing().getOppositeFace();
        //Block chestBlock = block.getRelative(attached);
        Directional directional = (Directional) block.getBlockData();
        BlockFace attachedFace = directional.getFacing().getOppositeFace();
        Block chestBlock = block.getRelative(attachedFace);

        if (chestBlock.getType() != Material.CHEST) {
            return;
        }

        Player player = event.getPlayer();
        Chest chest = (Chest) chestBlock.getState();
        if (Boolean.TRUE.equals(chest.getPersistentDataContainer().get(shopKey, PersistentDataType.BOOLEAN))) {
            player.sendMessage(Locale.getMessage("shop.already-created"));
            event.setLine(1, Locale.getMessage("shop.status.something-wrong"));
            return;
        }

        UUID playerUniqueId = player.getUniqueId();
        UUID companyUniqueId = MyCompany.getCacheManager().getUserManager().getCompanyUUID(playerUniqueId);

        if (companyUniqueId == null) {
            player.sendMessage(Locale.getMessage("no-company"));
            event.setLine(1, Locale.getMessage("shop.status.something-wrong"));
            return;
        }

        Location location = event.getBlock().getLocation();
        UUID shopUniqueId = shopManager.createShop(companyUniqueId, location, price, player.getName());
        chest.getPersistentDataContainer().set(shopKey, PersistentDataType.BOOLEAN, true);
        chest.getPersistentDataContainer().set(companyKey, PersistentDataType.STRING, companyUniqueId.toString());
        chest.getPersistentDataContainer().set(shopUUIDKey, PersistentDataType.STRING, shopUniqueId.toString());
        chest.update();
        updateSign((Sign) block.getState());

    }

    public void updateSign(Sign sign) {
//        WallSign signData  = (WallSign) sign.getBlockData();
//        BlockFace attached  = signData.getFacing().getOppositeFace();
//        Block chestBlock = sign.getBlock().getRelative(attached);
        Directional directional = (Directional) sign.getBlock().getBlockData();
        BlockFace attachedFace = directional.getFacing().getOppositeFace();
        Block chestBlock = sign.getBlock().getRelative(attachedFace);
        Chest chestState = (Chest) chestBlock.getState();
        PersistentDataContainer container = chestState.getPersistentDataContainer();
        UUID companyUniqueId = UUID.fromString(container.get(companyKey, PersistentDataType.STRING));
        UUID shopUniqueId = UUID.fromString(container.get(shopUUIDKey, PersistentDataType.STRING));
        String status = "not-specific";
        Shop shop = shopManager.getShop(companyUniqueId, shopUniqueId);
        ItemStack item = shop.getItem();
        if (item != null) {
            status = "selling";
            Chest chest = (Chest) chestBlock.getState();
            Inventory inventory = chest.getInventory();
            int count = 0;
            for (ItemStack i : inventory.getContents()) {
                if (i != null && i.isSimilar(item)) {
                    count += item.getAmount();
                }
            }
            if (count <= 0) {
                status = "out-of-stock";
            }
        }
        String finalStatus = status;
        Bukkit.getScheduler().runTask(MyCompany.getInstance(), () -> {
            int i = 0;
            SignSide side = sign.getSide(Side.FRONT);
            for (String key : Locale.yaml.getConfigurationSection("shop.sign").getKeys(false)) {
                String line = Locale.getMessage("shop.sign." + key);
                line = line.replaceAll("%company%", companyManager.getName(companyUniqueId));
                if (item != null) {
                    line = line.replaceAll("%item%", item.getItemMeta().getDisplayName());
                } else {
                    line = line.replaceAll("%item%", "");
                }
                line = line.replaceAll("%status%", Locale.getMessage("shop.status." + finalStatus));
                line = line.replaceAll("%price%", String.valueOf(shop.getPrice()));
                side.setLine(i, line);
                i++;
            }
            sign.update();
        });


    }

    public void updateSign(Block chest) {

    }


}
