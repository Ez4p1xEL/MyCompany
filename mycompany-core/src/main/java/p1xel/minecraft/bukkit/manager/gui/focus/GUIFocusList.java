package p1xel.minecraft.bukkit.manager.gui.focus;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.manager.gui.CompanyMenu;
import p1xel.minecraft.bukkit.manager.gui.GUIConfirmation;
import p1xel.minecraft.bukkit.object.focus.CompanyFocus;
import p1xel.minecraft.bukkit.object.focus.Focus;
import p1xel.minecraft.bukkit.util.ConditionUtil;
import p1xel.minecraft.bukkit.util.focus.FocusUtil;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUIFocusList extends CompanyMenu {

    private CompanyFocus companyFocus;
    private Focus focus;
    private String focusId;

    public GUIFocusList(UUID playerUniqueId, int page) {
        super(playerUniqueId);
        this.page = page;
        this.menu = new Menu(MenuUtil.getMenu("focus_list"));
        if (hasCompany) {
            this.companyFocus = focusManager.getCompanyFocus(companyUniqueId);
            init();
        }
    }

    @Override
    public void init() {

        Inventory inventory = Bukkit.createInventory(this, menu.getSize(), menu.getTitle().replace("%company%", companyName));

        boolean hasPreviousPage = false;
        boolean hasNextPage = false;
        if (page > 1) {
            hasPreviousPage = true;
        }

        this.focus = companyFocus.getFocus();
        this.focusId = focus.getId();
        List<String> focuses = FocusUtil.ENABLED_FOCUS_LIST;

        int focus_per_page = menu.getInt("focus-per-page", 10);

        if (focuses.size() > page*focus_per_page) {
            hasNextPage = true;
        }
        focuses = focuses.subList(focus_per_page * (page-1), Math.min((page) * focus_per_page, focuses.size()));

        // 显示previous_page 和 next_page按钮
        if (!hasPreviousPage) {
            menu.getItems().remove("previous_page");
        }
        if (!hasNextPage) {
            menu.getItems().remove("next_page");
        }

        Map<String, String> placeholders = Map.ofEntries(
                Map.entry("%player%", playerName),
                Map.entry("%position%", position),
                Map.entry("%label%", positionLabel),
                Map.entry("%company_focus%", focusId),
                Map.entry("%company_focus_label%", FocusUtil.FOCUS_LABELS.getOrDefault(focusId, focusId))
        );


        // 复制 personnel 的 item
        if (focuses.size() < focus_per_page) {
            focus_per_page = focuses.size();
        }
        // 当前 personnel 的位置, 再获取它与当前行首个slot的位置的距离
        int current_slot = menu.getItems().get("focus").getSlots().getFirst();
        int line = (current_slot / 9)+1;
        int distance = current_slot - ((line-1)*9);

        int slot = current_slot;

        MenuItem focusItem = menu.getItems().get("focus").clone();
        menu.getItems().remove("focus");

        for (int i = 0; i < focus_per_page; i++) {

            // 计算当前行数
            int current_line = (slot / 9)+1;
            // 如果当前 slot 超过了当前行的最后一个 available slot, 则跳到下一行
            if (slot >= (current_line*9)-distance) {
                slot += distance+distance;
            }

            // get expertise info
            String focusId = focuses.get(i);

            String focusLabel = FocusUtil.FOCUS_LABELS.getOrDefault(focusId, focusId);
            List<String> focusDescription = FocusUtil.FOCUS_DESCRIPTIONS.getOrDefault(focusId, List.of());
            String focusDescriptionString = String.join("\n", focusDescription);
            String focusStatus = FocusUtil.getStatus(focusManager.getCompanyFocus(companyUniqueId).getFocus().getId().equals(focusId));

            String conditionMessage = "#remove#";
            if (FocusUtil.FOCUS_CONDITION_MAP.get(focusId) != null) {
                conditionMessage = String.join("\n", ConditionUtil.getConditionMessages(FocusUtil.FOCUS_CONDITION_MAP.get(focusId), user, company));
            }

            // left click message
            String unlock_action = this.focus.getId().equals(focusId) ? "unlocked" : "locked";
            String left_click_message = menu.getActions().get(unlock_action).get("left");

            // replace placeholders
            Map<String, String> employeePlaceholders = Map.ofEntries(
                    Map.entry("%focus_id%", focusId),
                    Map.entry("%focus_label%", focusLabel),
                    Map.entry("%focus_description%", focusDescriptionString),
                    Map.entry("%focus_unlock_status%", focusStatus),
                    Map.entry("%focus_unlock_condition%", conditionMessage),
                    Map.entry("%left_click%", left_click_message)
            );

            MenuItem cloneItem = focusItem.clone();

            // 如果有单独设置权限显示的物品Id
            MenuItem focusItemFromMenu = menu.getItems().get("focus_"+focusId.toLowerCase());
            if (focusItemFromMenu != null) {
                cloneItem.setMaterial(focusItemFromMenu.getMaterial());
                // 移除原有的focusItemFromMenu, 避免重复显示
                menu.getItems().remove("focus_"+focusId.toLowerCase());
            }

            // 如果当前focus已经解锁, 则给物品添加附魔效果
            if (focusId.equals(this.focus.id)) {
                cloneItem.getEnchantments().put(Enchantment.UNBREAKING, 1);
                cloneItem.getItemFlags().add(ItemFlag.HIDE_ENCHANTS);
            }

            cloneItem = replacePlaceholdersInMenuItems(cloneItem, employeePlaceholders);
            cloneItem.setCustomKey("focus:"+focusId);
            cloneItem.setSlots(Collections.singletonList(slot));
            cloneItem.setId(cloneItem.getId() + "_" + slot);

            menu.getItems().put(cloneItem.getId(), cloneItem);
            slot++;

        }

        inventory = setItems(inventory, this.menu, placeholders);
        inventory = fillEmptySlot(inventory, this.menu);

        this.inventory = inventory;

    }

    @Override
    public boolean check(String name, ClickType clickType) {
        Player player = Bukkit.getPlayer(playerUniqueId);
        if (name.equalsIgnoreCase("focus_info")) {
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            return true;
        }
        if (name.equalsIgnoreCase("next_page")) {
            new GUIFocusList(playerUniqueId, page+1).open();
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            return true;
        }
        if (name.equalsIgnoreCase("previous_page")) {
            new GUIFocusList(playerUniqueId, Math.min(1,page-1)).open();
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            return true;
        }

        if (name.equalsIgnoreCase("back_to_main")) {
            Inventory inv;
            if (previousMenu != null) {
                inv = previousMenu.getInventory();
            } else {
                inv = new GUIFocus(playerUniqueId, 1, false).getInventory();
            }
            player.openInventory(inv);
            player.playSound(player, Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
            return true;
        }

        if (name.startsWith("focus:")) {
            String focusId = name.split(":")[1];

            if (!userManager.hasPermission(playerUniqueId, Permission.FOCUS_SET)) {
                player.sendMessage(Locale.getMessage("not-permitted").replace("%permission%", Permission.FOCUS_SET.getName()));
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return true;
            }

            // Confirmation Menu
            new GUIConfirmation(playerUniqueId, "focus_set",
                    () -> {
                        if (!new PersonalAPI(playerUniqueId).setFocus(focusId)) {
                            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                            return;
                        }
                        new GUIFocusList(playerUniqueId, page).open();
                        player.playSound(player, Sound.ENTITY_VILLAGER_YES, 1f, 1f);
                    },
                    () -> new GUIFocusList(playerUniqueId, page).open()).open();

            return true;

        }

        return true;
    }
}
