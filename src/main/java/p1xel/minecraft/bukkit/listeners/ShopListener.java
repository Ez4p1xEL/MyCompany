package p1xel.minecraft.bukkit.listeners;

import com.sun.tools.javac.jvm.Items;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.events.CompanyIncomeEvent;
import p1xel.minecraft.bukkit.managers.*;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.Logger;
import p1xel.minecraft.bukkit.utils.permissions.Permission;
import p1xel.minecraft.bukkit.utils.storage.Locale;
import p1xel.minecraft.bukkit.utils.storage.Shop;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class ShopListener implements Listener {

    private final NamespacedKey shopKey = new NamespacedKey("mycompany", "is_company_shop");
    private final NamespacedKey companyKey = new NamespacedKey("mycompany", "company");
    private final NamespacedKey shopUUIDKey = new NamespacedKey("mycompany", "shop_uuid");
    private final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();
    private final UserManager userManager = MyCompany.getCacheManager().getUserManager();
    private final ShopManager shopManager = MyCompany.getCacheManager().getShopManager();
    private HashMap<UUID, ShopItemBuyMode> purchase = new HashMap<>();

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
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);

        if (companyUniqueId == null) {
            player.sendMessage(Locale.getMessage("no-company"));
            event.setLine(1, Locale.getMessage("shop.status.something-wrong"));
            return;
        }

        if (!userManager.hasPermission(playerUniqueId, Permission.CHESTSHOP_CREATE)) {
            player.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.CHESTSHOP_CREATE.getName()));
            return;
        }

        Location location = chestBlock.getLocation();
        UUID shopUniqueId = shopManager.createShop(companyUniqueId, location, price, player.getName());
        chest.getPersistentDataContainer().set(shopKey, PersistentDataType.BOOLEAN, true);
        chest.getPersistentDataContainer().set(companyKey, PersistentDataType.STRING, companyUniqueId.toString());
        chest.getPersistentDataContainer().set(shopUUIDKey, PersistentDataType.STRING, shopUniqueId.toString());
        chest.update();
        updateSign((Sign) block.getState());

    }

    public void updateSign(Sign sign) {
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
                    count += i.getAmount();
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
                    line = line.replaceAll("%item%", item.getType().name());
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

    public void updateSign(Block chestBlock) {
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
                    count += i.getAmount();
                }
            }
            if (count <= 0) {
                status = "out-of-stock";
            }
        }
        String finalStatus = status;
        BlockFace[] faces = {
                BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST,
                BlockFace.UP, BlockFace.DOWN
        };

        Sign sign = null;
        for (BlockFace face : faces) {
            Block signBlock = chestBlock.getRelative(face);
            if (!(signBlock.getState().getBlockData() instanceof WallSign)) {
                continue;
            }
            Sign wallSign = (Sign) signBlock.getState();
            if (wallSign.getSide(Side.FRONT).getLine(0).equalsIgnoreCase(Locale.getMessage("shop.sign.line1").replaceAll("%company%", companyManager.getName(companyUniqueId)))) {
                sign = wallSign;
            }
        }

        if (sign == null) {
            return;
        }

        Sign finalSign = sign;
        Bukkit.getScheduler().runTask(MyCompany.getInstance(), () -> {
            int i = 0;
            SignSide side = finalSign.getSide(Side.FRONT);
            for (String key : Locale.yaml.getConfigurationSection("shop.sign").getKeys(false)) {
                String line = Locale.getMessage("shop.sign." + key);
                line = line.replaceAll("%company%", companyManager.getName(companyUniqueId));
                if (item != null) {
                    line = line.replaceAll("%item%", item.getType().name());
                } else {
                    line = line.replaceAll("%item%", "");
                }
                line = line.replaceAll("%status%", Locale.getMessage("shop.status." + finalStatus));
                line = line.replaceAll("%price%", String.valueOf(shop.getPrice()));
                side.setLine(i, line);
                i++;
            }
            finalSign.update();
        });
    }

    @EventHandler
    public void onItemSpecified(InventoryMoveItemEvent event) {

        Inventory inventory = event.getDestination();
        Location location = inventory.getLocation();
        if (location == null) { return; }
        Shop shop = shopManager.getShop(location);
        if (shop == null) { return; }
        ItemStack specified = shop.getItem();
        if (specified != null) { Logger.debug(Level.INFO, "Item: " + specified); return; }

        ItemStack item = event.getItem();
        if (item.getType() == Material.AIR) {
            return;
        }

        item.setAmount(1);

        shop.setItem(item);
        Bukkit.getScheduler().runTaskLater(MyCompany.getInstance(), () ->
            updateSign(shop.getChestBlock())
        , 10L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            Block block = event.getClickedBlock();
            if (block.getState().getBlockData() instanceof WallSign) {


                Sign sign = (Sign) block.getState();
                Directional directional = (Directional) sign.getBlock().getBlockData();
                BlockFace attachedFace = directional.getFacing().getOppositeFace();
                Block chestBlock = sign.getBlock().getRelative(attachedFace);
                Chest chestState = (Chest) chestBlock.getState();
                PersistentDataContainer container = chestState.getPersistentDataContainer();
                if (Boolean.TRUE.equals(container.get(shopKey, PersistentDataType.BOOLEAN))) {
                    event.setCancelled(true);
                }
                return;
            }

            if (block.getState() instanceof Chest) {
                Shop shop = shopManager.getShop(block.getLocation());
                if (shop == null) {
                    return;
                }

                Player player = event.getPlayer();
                UUID playerUniqueId = player.getUniqueId();
                UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                if (companyUniqueId == null) {
                    player.sendMessage(Locale.getMessage("no-perm"));
                    event.setCancelled(true);
                    return;
                }

                if (!companyUniqueId.equals(shop.getCompanyUUID())) {
                    player.sendMessage(Locale.getMessage("no-perm"));
                    event.setCancelled(true);
                    return;
                }

            }

            return;

        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {

            Block block = event.getClickedBlock();
            if (!(block.getState().getBlockData() instanceof WallSign)) {
                return;
            }

            Sign sign = (Sign) block.getState();
            Directional directional = (Directional) sign.getBlock().getBlockData();
            BlockFace attachedFace = directional.getFacing().getOppositeFace();
            Block chestBlock = sign.getBlock().getRelative(attachedFace);
            Chest chestState = (Chest) chestBlock.getState();
            PersistentDataContainer container = chestState.getPersistentDataContainer();
            if (Boolean.TRUE.equals(container.get(shopKey, PersistentDataType.BOOLEAN))) {
                updateSign(sign);
            }

            Player player = event.getPlayer();
            UUID playerUniqueId = player.getUniqueId();
            Shop shop = shopManager.getShop(chestBlock.getLocation());
            if (shop.getItem() == null) {
                return;
            }
            joinMode(playerUniqueId, shop);
            UUID companyUniqueId = shop.getCompanyUUID();
            int amount = 0;

            Inventory inventory = chestState.getBlockInventory();
            ItemStack item = shop.getItem();
            for (ItemStack i : inventory.getContents()) {
                if (i != null && i.isSimilar(item)) {
                    amount += i.getAmount();
                }
            }

            for (String message : Locale.yaml.getStringList("shop.sell-info")) {

                message = message.replaceAll("%company%", companyManager.getName(companyUniqueId));
                message = message.replaceAll("%item%", shop.getItem().getType().name());
                message = message.replaceAll("%price%", String.valueOf(shop.getPrice()));
                message = message.replaceAll("%amount%", String.valueOf(amount));
                message = message.replaceAll("%status%", Locale.getMessage("shop.status." + shop.getStatus(amount)));
                message = Locale.translate(message);
                player.sendMessage(message);

            }

        }

    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {

        // If block is the chest
        Block block = event.getBlock();
        if (block.getType() == Material.CHEST) {
            //Chest chest = (Chest) block.getState();
            Shop shop = shopManager.getShop(block.getLocation());
            if (shop != null) {

                Player player = event.getPlayer();
                UUID playerUniqueId = player.getUniqueId();
                UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                if (companyUniqueId == null) {
                    player.sendMessage(Locale.getMessage("no-perm"));
                    event.setCancelled(true);
                    return;
                }

                if (!userManager.hasPermission(playerUniqueId, Permission.CHESTSHOP_DELETE)) {
                    player.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.CHESTSHOP_DELETE.getName()));
                    event.setCancelled(true);
                    return;
                }

                shopManager.deleteShop(shop);
                player.sendMessage(Locale.getMessage("shop.shop-deleted"));
                return;

            }

        }

        if (block.getState().getBlockData() instanceof WallSign) {
            Sign sign = (Sign) block.getState();
            Directional directional = (Directional) sign.getBlock().getBlockData();
            BlockFace attachedFace = directional.getFacing().getOppositeFace();
            Block chestBlock = sign.getBlock().getRelative(attachedFace);
            Shop shop = shopManager.getShop(chestBlock.getLocation());
            if (shop != null) {
                Player player = event.getPlayer();
                player.sendMessage(Locale.getMessage("shop.break-chest-instead"));
                event.setCancelled(true);
            }
        }

    }

    public boolean joinMode(UUID playerUniqueId, Shop shop) {
        if (purchase.containsKey(playerUniqueId)) {
            purchase.get(playerUniqueId).getExpirationTask().cancel();
            purchase.remove(playerUniqueId);
            return false;
        }

        // Create the task
        ShopItemBuyMode join_mode = new ShopItemBuyMode(playerUniqueId, shop, System.currentTimeMillis(), null);

        // Task
        BukkitTask task = Bukkit.getScheduler().runTaskLater(MyCompany.getInstance(), () -> {
            leaveMode(join_mode);
        }, 20L * Config.getInt("chest-shop.buy-time"));

        join_mode.setExpirationTask(task);

        purchase.put(playerUniqueId, join_mode);
        return true;
    }

    private void leaveMode(ShopItemBuyMode mode) {
        UUID playerUniqueId = mode.getPlayerUniqueId();
        //Shop shop = mode.getShop();

        mode.getExpirationTask().cancel();
        purchase.remove(playerUniqueId);
    }

    @EventHandler
    public void onBuy(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        UUID playerUniqueId = player.getUniqueId();

        if (!purchase.containsKey(playerUniqueId)) {
            return;
        }

        Bukkit.getScheduler().runTask(MyCompany.getInstance(), () -> {

            int amount = 0;

            try {
                amount = Integer.parseInt(event.getMessage());
            } catch (NumberFormatException e) {
                return;
            }

            ShopItemBuyMode mode = purchase.get(playerUniqueId);
            Shop shop = mode.getShop();

            if (amount <= 0) {
                player.sendMessage(Locale.getMessage("shop.invalid-buy-amount"));
                event.setCancelled(true);
                mode.getExpirationTask().cancel();
                purchase.remove(playerUniqueId);
                return;
            }

            int stock = 0;
            Chest chestState = (Chest) shop.getChestBlock().getState();
            Inventory inventory = chestState.getBlockInventory();
            ItemStack item = shop.getItem();
            for (ItemStack i : inventory.getContents()) {
                if (i != null && i.isSimilar(item)) {
                    stock += i.getAmount();
                }
            }
            if (amount > stock) {
                player.sendMessage(Locale.getMessage("shop.not-enough-stock"));
                event.setCancelled(true);
                mode.getExpirationTask().cancel();
                purchase.remove(playerUniqueId);
                return;
            }

            double cost = shop.getPrice() * amount;
            double balance = MyCompany.getEconomy().getBalance(player);
            if (cost > balance) {
                player.sendMessage(Locale.getMessage("not-enough-money").replaceAll("%money%", String.valueOf(cost)));
                event.setCancelled(true);
                mode.getExpirationTask().cancel();
                purchase.remove(playerUniqueId);
                return;
            }

//        shop.takeStock(amount);
            int remaining = amount;
//            int count = 0;
//            for (ItemStack i : inventory.getContents()) {
//                if (remaining <= 0) {
//                    break;
//                }
//                if (i != null && i.isSimilar(item)) {
//                    int has = i.getAmount();
//                    if (has <= remaining) {
//                        remaining -= has;
//                        i.setAmount(0);
//                        inventory.setItem(count, i);
//                    }
//
//                    if (remaining < has) {
//                        remaining = 0;
//                        i.setAmount(has - remaining);
//                        inventory.setItem(count, i);
//                    }
//                }
//                count++;
//            }
            item.setAmount(remaining);
            inventory.removeItem(item);
            //chestState.update();
            Inventory playerInv = player.getInventory();
            //item.setAmount(amount);
            playerInv.addItem(item);
            MyCompany.getEconomy().withdrawPlayer(player, cost);

            UUID companyUniqueId = shop.getCompanyUUID();
            CompanyIncomeEvent incomeEvent = new CompanyIncomeEvent(companyUniqueId, cost);
            Bukkit.getPluginManager().callEvent(incomeEvent);
            cost = incomeEvent.getAmount();
            companyManager.giveMoney(companyUniqueId, cost);

            event.setCancelled(true);
            player.sendMessage(Locale.getMessage("shop.buy-success").replaceAll("%company%", companyManager.getName(companyUniqueId)));
            mode.getExpirationTask().cancel();
            purchase.remove(playerUniqueId);
        });

    }


}
