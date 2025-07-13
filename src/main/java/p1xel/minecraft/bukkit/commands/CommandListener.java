package p1xel.minecraft.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.events.CompanyIncomeEvent;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandListener implements CommandExecutor {

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        boolean isAdmin = sender.hasPermission("mycompany.commands.admin");

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {

                if (!isAdmin) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                Config.reloadConfig();
                sender.sendMessage(Locale.getMessage("reload-success"));
                return true;

            }

            // 破產 / 解散
            if (args[0].equalsIgnoreCase("disband")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                if (!sender.hasPermission("mycompany.commands.disband")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                Player player = (Player) sender;
                UUID uniqueId = player.getUniqueId();
                if (!MyCompany.getCacheManager().getUserManager().getPosition(uniqueId).equalsIgnoreCase("Employer")) {
                    sender.sendMessage(Locale.getMessage("employer-only"));
                    return true;
                }

                UUID companyUniqueId = MyCompany.getCacheManager().getUserManager().getCompanyUUID(uniqueId);
                String companyName = MyCompany.getCacheManager().getCompanyManager().getName(companyUniqueId);
                //if (!MyCompany.getCacheManager().getCompanyManager())
                MyCompany.getCacheManager().getCompanyManager().disbandCompany(companyUniqueId);
                sender.sendMessage(Locale.getMessage("disband-success").replaceAll("%company%", companyName));

                for (Player online_player : Bukkit.getOnlinePlayers()) {

                    online_player.sendMessage(Locale.getMessage("broadcast.company-disband").replaceAll("%company%", companyName).replaceAll("%player%", sender.getName()));
                    online_player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 3f, 3f);

                }

                return true;

            }

            if (args[0].equalsIgnoreCase("resign")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                if (!sender.hasPermission("mycompany.commands.fire")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                Player player = (Player) sender;
                UUID uniqueId = player.getUniqueId();
                UUID companyUniqueId = MyCompany.getCacheManager().getUserManager().getCompanyUUID(uniqueId);
                // Check if the sender has company
                if (companyUniqueId == null) {
                    sender.sendMessage(Locale.getMessage("no-company"));
                    return true;
                }

                // Check if the sender is the employer
                if (MyCompany.getCacheManager().getUserManager().getPosition(uniqueId).equalsIgnoreCase("employer")) {
                    sender.sendMessage(Locale.getMessage("disband-instead"));
                    return true;
                }

                MyCompany.getCacheManager().getCompanyManager().dismissEmployee(companyUniqueId, uniqueId);
                String companyName = MyCompany.getCacheManager().getCompanyManager().getName(companyUniqueId);
                sender.sendMessage(Locale.getMessage("resign-success").replaceAll("%company%", companyName));

                UUID employerUniqueId = MyCompany.getCacheManager().getCompanyManager().getEmployer(companyUniqueId);
                Player employer = Bukkit.getPlayer(employerUniqueId);
                if (employer != null) {
                    employer.sendMessage(Locale.getMessage("resigned").replaceAll("%player%", sender.getName()));
                    employer.playSound(employer, Sound.ENTITY_VILLAGER_NO, 3f, 3f);
                }
                return true;

            }
        }

        if (args.length == 2) {

            if (args[0].equalsIgnoreCase("found")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                if (!sender.hasPermission("mycompany.commands.found")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                Player p = (Player) sender;
                UUID uniqueId = p.getUniqueId();
                if (MyCompany.getCacheManager().getUserManager().getCompanyUUID(uniqueId) != null) {
                    sender.sendMessage(Locale.getMessage("has-company"));
                    return true;
                }

                int length = args[1].length();
                int max_length = Config.getInt("company-settings.max-length");

                if (length > max_length) {
                    sender.sendMessage(Locale.getMessage("out-of-length").replaceAll("%length%", String.valueOf(max_length)));
                    return true;
                }

                if (MyCompany.getCacheManager().getCompanyManager().getCompaniesName().contains(args[1])) {
                    sender.sendMessage(Locale.getMessage("company-exist").replaceAll("%company%", args[1]));
                    return true;
                }

                double money = Config.getDouble("company-settings.founding-cost");
                if (MyCompany.getEconomy().getBalance(p) < money) {
                    sender.sendMessage(Locale.getMessage("not-enough-money").replaceAll("%money%", String.valueOf(money)));
                    return true;
                }

                MyCompany.getCacheManager().getCompanyManager().createCompany(args[1], ((Player) sender).getUniqueId());
                MyCompany.getEconomy().withdrawPlayer(p, money);
                sender.sendMessage(Locale.getMessage("found-success").replaceAll("%company%", args[1]));

                // Broadcast
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(Locale.getMessage("broadcast.company-found").replaceAll("%company%", args[1]).replaceAll("%player%", sender.getName()));
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 3f, 3f);
                }

                return true;

            }

            if (args[0].equalsIgnoreCase("fire")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                if (!sender.hasPermission("mycompany.commands.fire")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                Player player = (Player) sender;
                UUID uniqueId = player.getUniqueId();
                UUID companyUniqueId = MyCompany.getCacheManager().getUserManager().getCompanyUUID(uniqueId);
                // Check if the sender has company
                if (companyUniqueId == null) {
                    sender.sendMessage(Locale.getMessage("no-company"));
                    return true;
                }

                // Check if the sender is the employer
                if (!MyCompany.getCacheManager().getUserManager().getPosition(uniqueId).equalsIgnoreCase("employer")) {
                    sender.sendMessage(Locale.getMessage("employer-only"));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUniqueId = target.getUniqueId();
                // Check if the target player is existed.
                if (!target.hasPlayedBefore() || !MyCompany.getCacheManager().getUserManager().isUserExist(targetUniqueId)) {
                    sender.sendMessage(Locale.getMessage("player-not-exist").replaceAll("%player%", args[1]));
                    return true;
                }

                String targetName = target.getName(); assert targetName != null;

                // Check if the target has company
                UUID targetCompanyUniqueId = MyCompany.getCacheManager().getUserManager().getCompanyUUID(uniqueId);
                if (targetCompanyUniqueId == null) {
                    sender.sendMessage(Locale.getMessage("target-has-no-company").replaceAll("%player%", targetName));
                    return true;
                }

                // Check if the target is in the same company with sender
                if (!companyUniqueId.equals(targetCompanyUniqueId)) {
                    sender.sendMessage(Locale.getMessage("not-your-employee").replaceAll("%player%", targetName));
                    return true;
                }

                MyCompany.getCacheManager().getCompanyManager().dismissEmployee(companyUniqueId, targetUniqueId);
                sender.sendMessage(Locale.getMessage("fire-success").replaceAll("%player%", targetName));
                if (target.isOnline()) {
                    Player onlineTarget = (Player) target;
                    String companyName = MyCompany.getCacheManager().getCompanyManager().getName(companyUniqueId);
                    onlineTarget.sendMessage(Locale.getMessage("dismissed").replaceAll("%player%", sender.getName()).replaceAll("%company%", companyName));
                    onlineTarget.playSound(onlineTarget, Sound.ENTITY_VILLAGER_NO, 3f, 3f);
                }
                return true;

            }

            if (args[0].equalsIgnoreCase("employ")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                if (!sender.hasPermission("mycompany.commands.employ")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                Player player = (Player) sender;
                UUID uniqueId = player.getUniqueId();
                UUID companyUniqueId = MyCompany.getCacheManager().getUserManager().getCompanyUUID(uniqueId);
                // Check if the sender has company
                if (companyUniqueId == null) {
                    sender.sendMessage(Locale.getMessage("no-company"));
                    return true;
                }

                // Check if the sender is the employer
                if (!MyCompany.getCacheManager().getUserManager().getPosition(uniqueId).equalsIgnoreCase("employer")) {
                    sender.sendMessage(Locale.getMessage("employer-only"));
                    return true;
                }

                OfflinePlayer off_target = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUniqueId = off_target.getUniqueId();
                // Check if the target player is existed.
                if (!off_target.hasPlayedBefore() || !MyCompany.getCacheManager().getUserManager().isUserExist(targetUniqueId)) {
                    sender.sendMessage(Locale.getMessage("player-not-exist").replaceAll("%player%", args[1]));
                    return true;
                }

                String targetName = off_target.getName(); assert targetName != null;
                if (!off_target.isOnline()) {
                    sender.sendMessage(Locale.getMessage("player-not-online").replaceAll("5player%", targetName));
                    return true;
                }

                // Check if the target has company
                UUID targetCompanyUniqueId = MyCompany.getCacheManager().getUserManager().getCompanyUUID(targetUniqueId);
                if (targetCompanyUniqueId != null) {
                    sender.sendMessage(Locale.getMessage("target-has-company").replaceAll("%player%", targetName));
                    return true;
                }

                boolean sendRequest = MyCompany.getHireRequestManager().sendRequest(uniqueId, targetUniqueId);
                if (!sendRequest) {
                    sender.sendMessage(Locale.getMessage("contract-already-sent"));
                    return true;
                }

                Player target = (Player) off_target;
                int respondTime = Config.getInt("company-settings.hire-request-time");
                String companyName = MyCompany.getCacheManager().getCompanyManager().getName(companyUniqueId);

                sender.sendMessage(Locale.getMessage("contract-send").replaceAll("%player%", targetName).replaceAll("%second%", String.valueOf(respondTime)));
                player.playSound(player, Sound.BLOCK_ANVIL_PLACE, 3f, 3f);
                target.sendMessage(Locale.getMessage("contract-received").replaceAll("%player%", sender.getName()).replaceAll("%company%", companyName).replaceAll("%second%", String.valueOf(respondTime)));
                target.playSound(target, Sound.BLOCK_ANVIL_PLACE, 3f, 3f);
                return true;


            }

            if (args[0].equalsIgnoreCase("accept")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                if (!sender.hasPermission("mycompany.commands.accept")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                Player player = (Player) sender;
                UUID uniqueId = player.getUniqueId();
                // Check if the sender has company
                if (MyCompany.getCacheManager().getUserManager().getCompanyUUID(uniqueId) != null) {
                    sender.sendMessage(Locale.getMessage("has-company"));
                    return true;
                }

                OfflinePlayer off_target = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUniqueId = off_target.getUniqueId();
                // Check if the target player is existed.
                if (!off_target.hasPlayedBefore() || !MyCompany.getCacheManager().getUserManager().isUserExist(targetUniqueId)) {
                    sender.sendMessage(Locale.getMessage("player-not-exist").replaceAll("%player%", args[1]));
                    return true;
                }

                // Check if there is a request existed for employee and employer.
                if (!MyCompany.getHireRequestManager().canAccept(uniqueId, targetUniqueId)) {
                    sender.sendMessage(Locale.getMessage("no-contract"));
                    return true;
                }

                UUID companyUniqueId = MyCompany.getCacheManager().getUserManager().getCompanyUUID(targetUniqueId);
                String companyName = MyCompany.getCacheManager().getCompanyManager().getName(companyUniqueId);

                MyCompany.getHireRequestManager().acceptRequest(uniqueId, companyUniqueId);

                if (off_target.isOnline()) {
                    Player target = (Player) off_target;
                    target.sendMessage(Locale.getMessage("contract-accept-employer").replaceAll("%player%", sender.getName()));
                    target.playSound(target, Sound.BLOCK_ANVIL_PLACE, 3f, 3f);
                }

                sender.sendMessage(Locale.getMessage("contract-accept-employee").replaceAll("%company%", companyName));
                player.playSound(player, Sound.BLOCK_ANVIL_PLACE, 3f, 3f);

                return true;


            }

        }

        if (args.length <= 2) {

            if (args[0].equalsIgnoreCase("info")) {

                UUID companyUniqueId = null;
                String companyName = "";
                int cid = 0;
                String foundDate = "";
                int playerAmount = 0;
                UUID employerUniqueId = null;
                String employerName = "";
                String founderName = "";

                if (args.length == 1) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    if (!sender.hasPermission("mycompany.commands.info")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    Player player = (Player) sender;
                    UUID uniqueId = player.getUniqueId();
                    companyUniqueId = MyCompany.getCacheManager().getUserManager().getCompanyUUID(uniqueId);
                    // Check if the sender has company
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("no-company"));
                        return true;
                    }

                    CompanyManager company = MyCompany.getCacheManager().getCompanyManager();
                    companyName = company.getName(companyUniqueId);
                    cid = company.getId(companyUniqueId);
                    foundDate = company.getFoundDate(companyUniqueId);
                    playerAmount = company.getMemberAmount(companyUniqueId);
                    employerUniqueId = company.getEmployer(companyUniqueId);
                    employerName = Bukkit.getOfflinePlayer(employerUniqueId).getName();
                    founderName = company.getFounderName(companyUniqueId);

                }

                if (args.length == 2) {

                    if (!sender.hasPermission("mycompany.commands.info.other")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    try {
                        cid = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        cid = -1;
                    }
                    if (cid < 0) {
                        sender.sendMessage(Locale.getMessage("id-invalid"));
                        return true;
                    }

                    companyUniqueId = MyCompany.getCacheManager().getCompanyManager().getUUID(cid);
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("id-not-exist").replaceAll("%cid%", args[1]));
                        return true;
                    }

                    CompanyManager company = MyCompany.getCacheManager().getCompanyManager();
                    companyName = company.getName(companyUniqueId);
                    foundDate = company.getFoundDate(companyUniqueId);
                    playerAmount = company.getMemberAmount(companyUniqueId);
                    employerUniqueId = company.getEmployer(companyUniqueId);
                    employerName = Bukkit.getOfflinePlayer(employerUniqueId).getName();
                    founderName = company.getFounderName(companyUniqueId);

                }

                for (String message : Locale.yaml.getStringList("info.default")) {

                    if (message.contains("%employee%")) {

                        CompanyManager company = MyCompany.getCacheManager().getCompanyManager();
                        for (String position : company.getPositions(companyUniqueId)) {

                            List<UUID> employee_unformatted = company.getEmployeeList(companyUniqueId, position);
                            List<String> employeeList = new ArrayList<>();
                            for (UUID employeeUniqueId : employee_unformatted) {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(employeeUniqueId);
                                String employeeName = offlinePlayer.getName();
                                employeeList.add(employeeName);
                            }
                            String employee = String.join(", ", employeeList);
                            position = position.substring(0, 1).toUpperCase() + position.substring(1);

                            for (String employeeMessage : Locale.yaml.getStringList("info.employee")) {

                                employeeMessage = employeeMessage.replaceAll("%position%", position);
                                employeeMessage = employeeMessage.replaceAll("%employees%", employee);
                                sender.sendMessage(Locale.translate(employeeMessage));

                            }

                        }

                        continue;
                    }

                    message = message.replaceAll("%company%", companyName);
                    message = message.replaceAll("%cid%", String.valueOf(cid));
                    message = message.replaceAll("%date%", foundDate);
                    message = message.replaceAll("%amount%", String.valueOf(playerAmount));
                    message = message.replaceAll("%employer%", employerName);
                    message = message.replaceAll("%founder%", founderName);
                    sender.sendMessage(Locale.translate(message));

                }

                return true;

            }

        }

        if (args.length == 4) {

            if (args[0].equalsIgnoreCase("money")) {

                if (args[1].equalsIgnoreCase("give")) {

                    if (!isAdmin) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    // args[2] is CompanyId
                    // args[3] is money

                    double money = 0.0;
                    int cid = 0;
                    try {
                        money = Double.parseDouble(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Locale.getMessage("money-format-incorrect"));
                        return true;
                    }

                    try {
                        cid = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Locale.getMessage("id-invalid"));
                        return true;
                    }

                    CompanyManager manager = MyCompany.getCacheManager().getCompanyManager();
                    UUID uniqueId = manager.getUUID(cid);
                    if (uniqueId == null) {
                        sender.sendMessage(Locale.getMessage("id-not-exist").replaceAll("%cid%", args[1]));
                        return true;
                    }

                    CompanyIncomeEvent event = new CompanyIncomeEvent(uniqueId, money);
                    Bukkit.getPluginManager().callEvent(event);

                    money = event.getAmount();
                    String companyName = manager.getName(uniqueId);

                    manager.giveMoney(uniqueId, money);
                    sender.sendMessage(Locale.getMessage("money-give-success").replaceAll("%money%", String.valueOf(money)).replaceAll("%company%", companyName));
                    return true;

                }

                if (args[1].equalsIgnoreCase("take")) {

                    if (!isAdmin) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    // args[2] is CompanyId
                    // args[3] is money

                    double money = 0.0;
                    int cid = 0;
                    try {
                        money = Double.parseDouble(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Locale.getMessage("money-format-incorrect"));
                        return true;
                    }

                    try {
                        cid = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Locale.getMessage("id-invalid"));
                        return true;
                    }

                    CompanyManager manager = MyCompany.getCacheManager().getCompanyManager();
                    UUID uniqueId = manager.getUUID(cid);
                    if (uniqueId == null) {
                        sender.sendMessage(Locale.getMessage("id-not-exist").replaceAll("%cid%", args[1]));
                        return true;
                    }

                    String companyName = manager.getName(uniqueId);

                    manager.takeMoney(uniqueId, money);
                    sender.sendMessage(Locale.getMessage("money-take-success").replaceAll("%money%", String.valueOf(money)).replaceAll("%company%", companyName));
                    return true;

                }

            }


        }

        int helpPage = 1;

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("?")) {
                try {
                    helpPage = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Only show 7
        int max_arg = helpPage * 7;
        int show = 1;
        int current_commands = 7; //

        sender.sendMessage(Locale.getMessage("commands.top-bar"));
        sender.sendMessage(Locale.getMessage("commands.page").replaceAll("%page%", String.valueOf(helpPage).replaceAll("%max_page%", String.valueOf(current_commands))));
        sender.sendMessage(Locale.getMessage("commands.space-1"));
        for (String arg : Locale.yaml.getConfigurationSection("commands.").getKeys(false)) {

            if (arg.equals("top-bar") || arg.equals("space-1") || arg.equals("space-2") || arg.equals("bottom-bar")) {
               // sender.sendMessage(Locale.getMessage("commands." + arg));
                continue;
            }

            if (show > max_arg) {
                break;
            }

            if (show < max_arg - 7) {
                show++;
                continue;
            }

            if (!arg.equals("admin")) {
                if (!sender.hasPermission("mycompany.commands." + arg)) {
                    continue;
                }
                sender.sendMessage(Locale.getMessage("commands." + arg));
                show++;
                continue;
            }

            if (!isAdmin) {
                continue;
            }

            for (String admin : Locale.yaml.getConfigurationSection("commands.admin").getKeys(false)) {
                if (show > max_arg) {
                    break;
                }
                sender.sendMessage(Locale.getMessage("commands.admin." + admin));
                show++;
            }

        }

        sender.sendMessage(Locale.getMessage("commands.space-2"));
        sender.sendMessage(Locale.getMessage("commands.bottom-bar"));

        return true;
    }


}
