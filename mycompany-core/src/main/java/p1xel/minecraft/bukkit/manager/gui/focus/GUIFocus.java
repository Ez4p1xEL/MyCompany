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
import p1xel.minecraft.bukkit.manager.gui.GUIMain;
import p1xel.minecraft.bukkit.object.condition.Condition;
import p1xel.minecraft.bukkit.object.focus.CompanyFocus;
import p1xel.minecraft.bukkit.object.focus.Focus;
import p1xel.minecraft.bukkit.object.focus.tree.ExpertiseInstance;
import p1xel.minecraft.bukkit.object.focus.tree.config.ExpertiseCondition;
import p1xel.minecraft.bukkit.util.ConditionUtil;
import p1xel.minecraft.bukkit.util.Logger;
import p1xel.minecraft.bukkit.util.focus.FocusUtil;
import p1xel.minecraft.bukkit.util.permission.Permission;
import p1xel.minecraft.bukkit.util.storage.Locale;
import p1xel.minecraft.bukkit.util.storage.menu.Menu;
import p1xel.minecraft.bukkit.util.storage.menu.MenuItem;
import p1xel.minecraft.bukkit.util.storage.menu.MenuUtil;

import java.util.*;

public class GUIFocus extends CompanyMenu {

    private CompanyFocus companyFocus;
    private Focus focus;
    private String focusId;
    private boolean showDisabledExpertises;

    public GUIFocus(UUID playerUniqueId, int page, boolean showDisabledExpertises) {
        super(playerUniqueId);
        this.page = page;
        this.showDisabledExpertises = showDisabledExpertises;
        this.menu = new Menu(MenuUtil.getMenu("focus"));
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
        List<String> expertises = FocusUtil.ENABLED_EXPERTISE_LIST.get(focusId);

        if (!showDisabledExpertises) {
            // Remove disabled Expertises from the list
            expertises.removeIf(expertiseId -> focus.getDisabledExpertiseInstance(expertiseId) != null);
        }

        int expertise_per_page = menu.getInt("expertise-per-page", 10);

        if (expertises.size() > page*expertise_per_page) {
            hasNextPage = true;
        }
        expertises = expertises.subList(expertise_per_page * (page-1), Math.min((page) * expertise_per_page, expertises.size()));

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
                Map.entry("%focus%", focusId),
                Map.entry("%focus_label%", FocusUtil.FOCUS_LABELS.getOrDefault(focusId, focusId))
        );


        // 复制 personnel 的 item
        if (expertises.size() < expertise_per_page) {
            expertise_per_page = expertises.size();
        }
        // 当前 personnel 的位置, 再获取它与当前行首个slot的位置的距离
        int current_slot = menu.getItems().get("expertise").getSlots().getFirst();
        int line = (current_slot / 9)+1;
        int distance = current_slot - ((line-1)*9);

        int slot = current_slot;

        MenuItem expertiseItem = menu.getItems().get("expertise").clone();
        menu.getItems().remove("expertise");

        for (int i = 0; i < expertise_per_page; i++) {

            // 计算当前行数
            int current_line = (slot / 9)+1;
            // 如果当前 slot 超过了当前行的最后一个 available slot, 则跳到下一行
            if (slot >= (current_line*9)-distance) {
                slot += distance+distance;
            }

            // get expertise info
            ExpertiseInstance expertise = focus.getExpertiseInstance(expertises.get(i));
            if (expertise == null && showDisabledExpertises) {
                expertise = focus.getDisabledExpertiseInstance(expertises.get(i));
                if (expertise == null) {
                    Logger.warn("ExpertiseInstance for expertiseId " + expertises.get(i) + " is null. Please check the configuration.");
                    continue;
                }
            }

            String expertiseId = expertise.getId();
            String expertiseLabel = FocusUtil.EXPERTISE_LABELS.getOrDefault(expertiseId, expertiseId);
            List<String> expertiseDescription = FocusUtil.EXPERTISE_DESCRIPTIONS.getOrDefault(expertiseId, List.of());
            String expertiseDescriptionString = String.join("\n", expertiseDescription);
            String expertiseStatus = FocusUtil.getStatus(expertise.getUnlocked());
            String expertiseEnabledStatus = FocusUtil.getEnabledLabel(expertise.getEnabled());
            ExpertiseCondition expertiseCondition;
            try {
                expertiseCondition = focusManager.getExpertiseConfig(expertiseId).getCondition();
            } catch (NullPointerException exception) {
                Logger.warn("ExpertiseConfig for expertiseId " + expertiseId + " is null. Please check the configuration.");
                continue;
            }
            List<Condition> conditions = List.of(expertiseCondition.getConditions());
            String conditionMessage = "#remove#";
            if (!conditions.isEmpty()) {
                conditionMessage = String.join("\n", ConditionUtil.getConditionMessages(List.of(expertiseCondition.getConditions()), user, company));
            }

            // left click message
            String unlock_action = expertise.getUnlocked() ? "unlocked" : "locked";
            String left_click_message = menu.getActions().get(unlock_action).get("left");

            // replace placeholders
            Map<String, String> employeePlaceholders = Map.ofEntries(
                    Map.entry("%expertise_id%", expertiseId),
                    Map.entry("%expertise_label%", expertiseLabel),
                    Map.entry("%expertise_description%", expertiseDescriptionString),
                    Map.entry("%expertise_unlock_status%", expertiseStatus),
                    Map.entry("%expertise_enable_status%", expertiseEnabledStatus),
                    Map.entry("%expertise_unlock_condition%", conditionMessage),
                    Map.entry("%left_click%", left_click_message)
            );

            MenuItem cloneItem = expertiseItem.clone();

            // 如果有单独设置权限显示的物品Id
            MenuItem expertiseItemFromMenu = menu.getItems().get("expertise_"+expertiseId.toLowerCase());
            if (expertiseItemFromMenu != null) {
                cloneItem.setMaterial(expertiseItemFromMenu.getMaterial());
                // 移除原有的expertiseItemFromMenu, 避免重复显示
                menu.getItems().remove("expertise_"+expertiseId.toLowerCase());
            }

            // 如果该权限已启用, 则给物品添加附魔效果
            if (expertise.getUnlocked()) {
                cloneItem.getEnchantments().put(Enchantment.UNBREAKING, 1);
                cloneItem.getItemFlags().add(ItemFlag.HIDE_ENCHANTS);
            }

            cloneItem = replacePlaceholdersInMenuItems(cloneItem, employeePlaceholders);
            cloneItem.setCustomKey("expertise:"+expertiseId);
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
        if (name.equals("expertise_info")) {
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            return true;
        }
        if (name.equals("next_page")) {
            new GUIFocus(playerUniqueId, page+1, showDisabledExpertises).open();
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            return true;
        }
        if (name.equals("previous_page")) {
            new GUIFocus(playerUniqueId, Math.min(1,page-1), showDisabledExpertises).open();
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            return true;
        }

        if (name.equals("focus")) {
            new GUIFocusList(playerUniqueId, 1).open();
            player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1f, 1f);
            return true;
        }

        if (name.equals("back_to_main")) {
            Inventory inv;
            if (previousMenu != null) {
                inv = previousMenu.getInventory();
            } else {
                inv = new GUIMain(playerUniqueId).getInventory();
            }
            player.openInventory(inv);
            player.playSound(player, Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
            return true;
        }

        if (name.startsWith("expertise:")) {
            String expertiseId = name.split(":")[1];
            ExpertiseInstance expertise = focus.getExpertiseInstance(expertiseId);
            if (expertise.getUnlocked()) {
                return true;
            }
            if (!userManager.hasPermission(playerUniqueId, Permission.FOCUS_LEARN_EXPERTISE)) {
                player.sendMessage(Locale.getMessage("not-permitted").replace("%permission%", Permission.FOCUS_LEARN_EXPERTISE.getName()));
                return true;
            }

            if (!new PersonalAPI(playerUniqueId).unlockExpertise(expertiseId)) {
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return true;
            }
            new GUIFocus(playerUniqueId, page, showDisabledExpertises).open();
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            return true;


        }

        return true;
    }
}
