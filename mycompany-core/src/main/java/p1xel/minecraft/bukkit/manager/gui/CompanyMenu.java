package p1xel.minecraft.bukkit.manager.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.User;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompanyMenu extends GUIAbstract implements InventoryHolder {

    public UUID playerUniqueId;
    protected boolean hasCompany;
    protected UUID companyUniqueId;
    protected User user;
    protected Company company;
    protected Inventory inventory;
    protected Player player;
    protected String playerName;
    protected Menu menu;
    protected String action;
    protected int page;
    protected boolean hasNextPage;
    protected boolean hasPreviousPage;

    protected String position;
    protected String positionLabel;
    protected String companyName;

    protected CompanyMenu previousMenu;

    public CompanyMenu(UUID playerUniqueId) {
        this.playerUniqueId = playerUniqueId;
        this.player = Bukkit.getPlayer(playerUniqueId);
        if (this.player == null) {
            return;
        }
        this.user = userManager.getUser(playerUniqueId);
        this.playerName = this.player.getName();
        this.companyUniqueId = user.getCompanyUUID();
        this.hasCompany = companyUniqueId != null;
        if (hasCompany) {
            this.company = cache.getCompany(companyUniqueId);
            this.position = user.getPosition();
            this.positionLabel = company.getPositionLabel(position);
            this.companyName = company.getName();
        }
    }

    @Override
    public void init() {

    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public boolean check(String name) {
        return false;
    }

    @Override
    public boolean check(String name, ClickType clickType) {
        return false;
    }

    public void setPreviousMenu(CompanyMenu previousMenu) {
        this.previousMenu = previousMenu;
    }

    public Inventory fillEmptySlot(Inventory inventory, Menu menu) {

        MenuItem menuItem = menu.getItems().get("default");
        if (menuItem == null) {
            return inventory;
        }
        Material material = menuItem.getMaterial();
        int size = menu.getSize();

        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) == null) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("");
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    container.set(menu_id_key, PersistentDataType.STRING, "default");
                    item.setItemMeta(meta);
                }
                inventory.setItem(i, item);
            }
        }

        return inventory;

    }

    public Inventory setItems(Inventory inventory, Menu menu, Map<String, String> placeholders) {

        for (MenuItem menuItem : menu.getItems().values()) {

            String id = menuItem.getId();
            // Skip the default item, as it is not meant to be displayed in the menu
            // Display the default items after the menu items have been set, to fill in the empty slots
            if (id.equals("default")) {
                continue;
            }
            Material material = menuItem.getMaterial();
            List<Integer> slots = menuItem.getSlots();
            String display_name = menuItem.getDisplayName();
            List<String> loreList = menuItem.getLore();

            // Replace placeholders in display name and lore
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String placeholder = entry.getKey();
                String value = entry.getValue();
                display_name = display_name.replace(placeholder, value);
                loreList = loreList.stream().map(line -> line.replace(placeholder, value)).filter(line -> !line.contains("#remove#")).collect(Collectors.toList());
            }

            // If the material is a player head, set the owner to the player's name
            boolean isSkullHead = false;
            String ownerName = null;
            if (material == Material.PLAYER_HEAD) {
                // Check if the menu item has a specific owner set in the lore, otherwise use the player's name
                String matStr = menuItem.getMaterialString();
                String[] args = matStr.split(";");
                if (args.length == 2 && args[0].equalsIgnoreCase("player_head")) {
                    String name = args[1];
                    name = name.replace("%player%", playerName);
                    ownerName = name;
                    isSkullHead = true;
                }
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (isSkullHead && ownerName != null) {
                SkullMeta skullMeta = (SkullMeta) meta;
                skullMeta.setOwner(ownerName);
            }
            meta.setDisplayName(Locale.translate(display_name));
            meta.setLore(Locale.translateList(loreList));

            // Custom Key
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(menu_id_key, PersistentDataType.STRING, id);
            String custom_key = menuItem.getCustomKey();
            if (custom_key != null) {
                container.set(menu_id_key, PersistentDataType.STRING, custom_key);
            }

            // Enchantments
            for (Map.Entry<Enchantment, Integer> entry : menuItem.getEnchantments().entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                meta.addEnchant(enchantment, level, true);
            }

            // Item Flags
            for (ItemFlag itemFlag : menuItem.getItemFlags()) {
                meta.addItemFlags(itemFlag);
            }

            item.setItemMeta(meta);

            for (int slot : slots) {
                inventory.setItem(slot, item);
            }

        }

        return inventory;

    }

    public MenuItem replacePlaceholdersInMenuItems(MenuItem menuItem, Map<String, String> placeholders) {
        String display_name = menuItem.getDisplayName();
        List<String> loreList = menuItem.getLore();

        // Replace placeholders in display name and lore
        // \n is used to split the lore into multiple lines

        if (display_name != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                display_name = display_name.replace(entry.getKey(), entry.getValue());
            }
            menuItem.setDisplayName(display_name);
        }

        if (loreList != null && !loreList.isEmpty()) {
            List<String> updatedLore = loreList;

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String placeholder = entry.getKey();
                String value = entry.getValue();

                updatedLore = updatedLore.stream()
                        .flatMap(line -> {
                            if (line == null) {
                                return Stream.of("");
                            }
                            if (!line.contains(placeholder)) {
                                return Stream.of(line);
                            }

                            // 1. 获取占位符前面的文本，提取其末尾生效的颜色代码
                            int index = line.indexOf(placeholder);
                            String prefixText = line.substring(0, index);
                            // Bukkit 原生方法：自动获取前面所有生效的颜色/格式代码（如 &f、§f、&a&l 等）

                            // 2. 如果包含换行，给每个换行符后面补充继承的颜色
                            String formattedValue = value;
                            if (value.contains("\n") && !prefixText.isEmpty()) {
                                // 将 \n 替换为 \n + 前缀颜色
                                formattedValue = value.replaceAll("\\r?\\n", "\n" + prefixText);
                            }

                            // 3. 执行替换并拆行
                            String replaced = line.replace(placeholder, formattedValue);
                            return Arrays.stream(replaced.split("\\r?\\n", -1));
                        })
                        .collect(Collectors.toList());
            }

            menuItem.setLore(updatedLore);
        }

        return menuItem;
    }

    public void open() {
        if (this.player == null) {
            return;
        }
        this.player.openInventory(this.inventory);
    }

    public void openPreviousMenu() {
        if (this.previousMenu != null) {
            this.previousMenu.open();
        }
    }

    public NamespacedKey getMenuIdKey() {
        return menu_id_key;
    }
}
