package p1xel.minecraft.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import p1xel.minecraft.bukkit.events.CompanyIncomeEvent;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.managers.UserManager;
import p1xel.minecraft.bukkit.utils.Logger;
import p1xel.minecraft.bukkit.utils.storage.EmployeeOrders;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class EmployeeOrder {

    private UUID playerUniqueId;
    private UUID companyUniqueId;
    private String order;
    private List<String> progress;
    private boolean finished = false;
    private HashMap<String, Integer> values = new HashMap<>(); // "questName:actionName:value"
    private HashMap<String, Boolean> finishes = new HashMap<>(); // "questName"
    private static final UserManager userManager = MyCompany.getCacheManager().getUserManager();
    private static final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();

    public EmployeeOrder(UUID playerUniqueId, String order, List<String> progress) {
        this.playerUniqueId = playerUniqueId;
        this.order = order;
        this.progress = progress;
        init();
        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
        if (companyUniqueId != null) {
            this.companyUniqueId = companyUniqueId;
        }
    }

    // "questName:actionName:value"

    private void init() {
        for (String prog : progress) {
            int lastColon = prog.lastIndexOf(":");

            String part1 = prog.substring(0, lastColon);
            int value = Integer.parseInt(prog.substring(lastColon + 1));
            values.put(part1, value);
            reachTarget(part1.split(":")[0], value);

        }
    }

    public UUID getPlayerUUID() {
        return playerUniqueId;
    }

    public int getTargetValue(String quest) {
        return EmployeeOrders.getValue(order, quest);
    }

    public void check(String quest, int amount) {
        if (amount >= getTargetValue(quest)) {
            reachTarget(quest, true);
            Player player = Bukkit.getPlayer(playerUniqueId);
            player.sendMessage(Locale.getMessage("quest-completed").replaceAll("%order%", EmployeeOrders.getLabel(order)).replaceAll("%progress%", EmployeeOrders.getProgressMessage(playerUniqueId, order, quest)));
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 3f, 3f);

            if (checkAll()) {
                setFinished();
                player.sendMessage(Locale.getMessage("order-completed").replaceAll("%order%", EmployeeOrders.getLabel(order)));
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 3f, 3f);
            }
        }
    }

    private void reachTarget(String quest, int amount) {
        finishes.put(quest, amount >= getTargetValue(quest));
    }

    private void reachTarget(String quest, boolean bool) {
        finishes.put(quest, bool);
    }

    public boolean checkAll() {
        boolean finished = true;
        for (boolean bool : finishes.values()) {
            if (!bool) {
                finished = false;
                break;
            }
        }

        return finished;
    }

    public void addProgressValue(String questAndAction, int amount) {
        Logger.debug(Level.INFO, "Progress value before adding: " + String.valueOf(values.get(questAndAction)));
        values.replace(questAndAction, values.get(questAndAction) + amount);
        check(questAndAction.split(":")[0], values.get(questAndAction));
        Logger.debug(Level.INFO, "Progress value after adding: " + String.valueOf(values.get(questAndAction)));
    }

    public int getProgressValue(String questAndAction) {
        return values.get(questAndAction);
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isFinished(String quest) {
        return finishes.get(quest);
    }

    public void setFinished() {
        finished = true;
        // rewards
        Player player = Bukkit.getPlayer(playerUniqueId);
        Logger.debug(Level.INFO, "The order " + order + " has been completed by player " + player.getName());

        for (String reward : EmployeeOrders.getRewards(order)) {

            String[] split = reward.split(";");
            String type = split[0];

            if (type.equalsIgnoreCase("item")) {

                String material = split[1];
                int amount;
                try {
                    amount = Integer.parseInt(split[2]);
                } catch (NumberFormatException e) {
                    continue;
                }

                Material mat = Material.matchMaterial(material);
                if (mat == null) {
                    continue;
                }
                ItemStack item = new ItemStack(mat, amount);
                player.getInventory().addItem(item);
                continue;

            }

            if (type.equalsIgnoreCase("company_income")) {

                double amount;
                try {
                    amount = Double.parseDouble(split[1]);
                } catch (NumberFormatException e) {
                    continue;
                }

                CompanyIncomeEvent event = new CompanyIncomeEvent(companyUniqueId, amount);
                Bukkit.getPluginManager().callEvent(event);

                amount = event.getAmount();

                companyManager.giveMoney(companyUniqueId, amount);
                continue;

            }

            if (type.equalsIgnoreCase("money")) {

                double amount;
                try {
                    amount = Double.parseDouble(split[1]);
                } catch (NumberFormatException e) {
                    continue;
                }

                MyCompany.getEconomy().depositPlayer(player, amount);
                continue;

            }

            if (type.equalsIgnoreCase("experience")) {

                int amount;
                try {
                    amount = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    continue;
                }
                player.giveExp(amount);
                continue;

            }

        }

        EmployeeOrders.removeCacheOrder(playerUniqueId, order);


    }

    public HashMap<String, Boolean> getFinishes() {
        return finishes;
    }

    public HashMap<String, Integer> getValues() {
        return values;
    }

    public String getName() {
        return order;
    }

}
