package p1xel.minecraft.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.api.PersonalAPI;
import p1xel.minecraft.bukkit.events.CompanyIncomeEvent;
import p1xel.minecraft.bukkit.managers.AreaManager;
import p1xel.minecraft.bukkit.managers.BuildingManager;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.UserManager;
import p1xel.minecraft.bukkit.managers.gui.*;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.permissions.Permission;
import p1xel.minecraft.bukkit.utils.storage.EmployeeOrders;
import p1xel.minecraft.bukkit.utils.storage.Locale;
import p1xel.minecraft.bukkit.utils.storage.backups.BackupCreator;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class CommandListener implements CommandExecutor {

    private final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();
    private final UserManager userManager = MyCompany.getCacheManager().getUserManager();
    private final BuildingManager buildingManager = MyCompany.getCacheManager().getBuildingManager();
    private final AreaManager areaManager = MyCompany.getCacheManager().getAreaManager();

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        boolean isAdmin = sender.hasPermission("mycompany.commands.admin");

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("open")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                if (!sender.hasPermission("mycompany.commands.open")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                Player player = (Player) sender;
                UUID playerUniqueId = player.getUniqueId();
                UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                // Check if the sender has company
                if (companyUniqueId == null) {
                    //sender.sendMessage(Locale.getMessage("no-company"));
                    GUIFound found = new GUIFound(playerUniqueId);
                    player.openInventory(found.getInventory());
                    return true;
                }
                GUIMain main = new GUIMain(playerUniqueId);
                player.openInventory(main.getInventory());
                return true;
            }

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
                if (!userManager.getPosition(uniqueId).equalsIgnoreCase("Employer")) {
                    sender.sendMessage(Locale.getMessage("employer-only"));
                    return true;
                }
                UUID companyUniqueId = userManager.getCompanyUUID(uniqueId);

                if (!MyCompany.getCacheManager().getShopManager().getShopsUUID(companyUniqueId).isEmpty()) {
                    sender.sendMessage(Locale.getMessage("remove-shop-before-disband"));
                    return true;
                }
                if (Config.getBool("company-settings.backup-folder-before-delete")) {
                    BackupCreator.createBackup(companyUniqueId);
                }
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
                UUID companyUniqueId = userManager.getCompanyUUID(uniqueId);
                // Check if the sender has company
                if (companyUniqueId == null) {
                    sender.sendMessage(Locale.getMessage("no-company"));
                    return true;
                }

                // Check if the sender is the employer
                if (userManager.getPosition(uniqueId).equalsIgnoreCase("employer")) {
                    sender.sendMessage(Locale.getMessage("disband-instead"));
                    return true;
                }

                new PersonalAPI(uniqueId).resignFromCompany();
                return true;

            }

            if (args[0].equalsIgnoreCase("setloc")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                if (!sender.hasPermission("mycompany.commands.setloc")) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                Player player = (Player) sender;
                UUID uniqueId = player.getUniqueId();
                UUID companyUniqueId = userManager.getCompanyUUID(uniqueId);
                // Check if the sender has company
                if (companyUniqueId == null) {
                    sender.sendMessage(Locale.getMessage("no-company"));
                    return true;
                }

                if (!userManager.getPosition(uniqueId).equalsIgnoreCase("employer")) {
                    sender.sendMessage(Locale.getMessage("employer-only"));
                    return true;
                }

                Location location = player.getLocation();
                if (!buildingManager.isEmployerArea(uniqueId, location)) {
                    sender.sendMessage(Locale.getMessage("not-your-protected-area"));
                    return true;
                }

                String name = buildingManager.getName(location);
                buildingManager.setName(companyUniqueId, name);
                buildingManager.setLocation(companyUniqueId, location);
                sender.sendMessage(Locale.getMessage("setloc-success"));
                return true;

            }
        }

        if (args.length == 2) {

            if (args[0].equalsIgnoreCase("found")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                new PersonalAPI((Player) sender).foundCompany(args[1]);
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
                UUID companyUniqueId = userManager.getCompanyUUID(uniqueId);
                // Check if the sender has company
                if (companyUniqueId == null) {
                    sender.sendMessage(Locale.getMessage("no-company"));
                    return true;
                }

//                // Check if the sender is the employer
//                if (!userManager.getPosition(uniqueId).equalsIgnoreCase("employer")) {
//                    sender.sendMessage(Locale.getMessage("employer-only"));
//                    return true;
//                }
                // Check if the sender has the permission
                if (!userManager.hasPermission(uniqueId, Permission.FIRE)) {
                    player.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.FIRE.getName()));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUniqueId = target.getUniqueId();
                // Check if the target player is existed.
                if (!target.hasPlayedBefore() || !userManager.isUserExist(targetUniqueId)) {
                    sender.sendMessage(Locale.getMessage("player-not-exist").replaceAll("%player%", args[1]));
                    return true;
                }

                String targetName = target.getName(); assert targetName != null;

                // Check if the target has company
                UUID targetCompanyUniqueId = userManager.getCompanyUUID(uniqueId);
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
                UUID companyUniqueId = userManager.getCompanyUUID(uniqueId);
                // Check if the sender has company
                if (companyUniqueId == null) {
                    sender.sendMessage(Locale.getMessage("no-company"));
                    return true;
                }

//                // Check if the sender is the employer
//                if (!userManager.getPosition(uniqueId).equalsIgnoreCase("employer")) {
//                    sender.sendMessage(Locale.getMessage("employer-only"));
//                    return true;
//                }
                // Check if the sender has the permission
                if (!userManager.hasPermission(uniqueId, Permission.EMPLOY)) {
                    sender.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.EMPLOY.getName()));
                    return true;
                }

                if (companyManager.getMemberAmount(companyUniqueId) >= Config.getInt("company-settings.maximum-players")) {
                    sender.sendMessage(Locale.getMessage("player-reach-maximum"));
                    return true;
                }

                OfflinePlayer off_target = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUniqueId = off_target.getUniqueId();
                // Check if the target player is existed.
                if (!off_target.hasPlayedBefore() || !userManager.isUserExist(targetUniqueId)) {
                    sender.sendMessage(Locale.getMessage("player-not-exist").replaceAll("%player%", args[1]));
                    return true;
                }

                String targetName = off_target.getName(); assert targetName != null;
                if (!off_target.isOnline()) {
                    sender.sendMessage(Locale.getMessage("player-not-online").replaceAll("%player%", targetName));
                    return true;
                }

                // Check if the target has company
                UUID targetCompanyUniqueId = userManager.getCompanyUUID(targetUniqueId);
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
                if (userManager.getCompanyUUID(uniqueId) != null) {
                    sender.sendMessage(Locale.getMessage("has-company"));
                    return true;
                }

                OfflinePlayer off_target = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUniqueId = off_target.getUniqueId();
                // Check if the target player is existed.
                if (!off_target.hasPlayedBefore() || !userManager.isUserExist(targetUniqueId)) {
                    sender.sendMessage(Locale.getMessage("player-not-exist").replaceAll("%player%", args[1]));
                    return true;
                }

                // Check if there is a request existed for employee and employer.
                if (!MyCompany.getHireRequestManager().canAccept(uniqueId, targetUniqueId)) {
                    sender.sendMessage(Locale.getMessage("no-contract"));
                    return true;
                }

                UUID companyUniqueId = userManager.getCompanyUUID(targetUniqueId);
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

            if (args[0].equalsIgnoreCase("givetool")) {

                if (!isAdmin) {
                    sender.sendMessage(Locale.getMessage("no-perm"));
                    return true;
                }

                OfflinePlayer off_target = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUniqueId = off_target.getUniqueId();
                // Check if the target player is existed.
                if (!off_target.hasPlayedBefore() || !userManager.isUserExist(targetUniqueId)) {
                    sender.sendMessage(Locale.getMessage("player-not-exist").replaceAll("%player%", args[1]));
                    return true;
                }

                String targetName = off_target.getName(); assert targetName != null;
                if (!off_target.isOnline()) {
                    sender.sendMessage(Locale.getMessage("player-not-online").replaceAll("%player%", targetName));
                    return true;
                }

                new PersonalAPI(targetUniqueId).getSelectionTool();
                return true;

            }

            if (args[0].equalsIgnoreCase("area")) {

                if (args[1].equalsIgnoreCase("info")) {

                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    if (!sender.hasPermission("mycompany.commands.area.info")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    UUID playerUniqueId = player.getUniqueId();
                    new PersonalAPI(playerUniqueId).getAreaInfo();
                    return true;

                }

                if (args[1].equalsIgnoreCase("setloc")) {

                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    if (!sender.hasPermission("mycompany.commands.area.setloc")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    UUID playerUniqueId = player.getUniqueId();
                    // Check if the sender has the permission
                    if (!userManager.hasPermission(playerUniqueId, Permission.AREA_SETLOC)) {
                        sender.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.AREA_SETLOC.getName()));
                        return true;
                    }

                    new PersonalAPI(playerUniqueId).setAreaLocation();
                    return true;

                }

                if (args[1].equalsIgnoreCase("list")) {

                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    if (!sender.hasPermission("mycompany.commands.area.list")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    UUID uniqueId = player.getUniqueId();
                    UUID companyUniqueId = userManager.getCompanyUUID(uniqueId);
                    // Check if the sender has company
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("no-company"));
                        return true;
                    }

                    Set<String> areas = areaManager.getAreas(companyUniqueId);
                    if (areas.isEmpty()) {
                        sender.sendMessage(Locale.getMessage("area-list-empty"));
                        return true;
                    }

                    sender.sendMessage(Locale.getMessage("area-list-success").replaceAll("%list%", areas.toString()));
                    return true;

                }

            }

        }

        if (args.length <= 2 && args.length > 0) {

            if (args[0].equalsIgnoreCase("info")) {

                UUID companyUniqueId = null;
                String companyName = "";
                int cid = 0;
                String foundDate = "";
                int playerAmount = 0;
                UUID employerUniqueId = null;
                String employerName = "";
                String founderName = "";

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }

                Player player = (Player) sender;

                if (args.length == 1) {

                    if (!sender.hasPermission("mycompany.commands.info")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    UUID uniqueId = player.getUniqueId();
                    companyUniqueId = userManager.getCompanyUUID(uniqueId);
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
                            if (position.equalsIgnoreCase("employer")) {
                                continue;
                            }

                            List<UUID> employee_unformatted = company.getEmployeeList(companyUniqueId, position);
                            String employee = "[]";
                            if (!employee_unformatted.isEmpty()) {
                                List<String> employeeList = new ArrayList<>();
                                for (UUID employeeUniqueId : employee_unformatted) {
                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(employeeUniqueId);
                                    String employeeName = offlinePlayer.getName();
                                    employeeList.add(employeeName);
                                }
                                employee = String.join(", ", employeeList);
                            }
                            //position = position.substring(0, 1).toUpperCase() + position.substring(1);

                            for (String employeeMessage : Locale.yaml.getStringList("info.employee")) {

                                employeeMessage = employeeMessage.replaceAll("%position%", position);
                                employeeMessage = employeeMessage.replaceAll("%label%", companyManager.getPositionLabel(companyUniqueId, position));
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
                    message = message.replaceAll("%employer_label%", companyManager.getEmployerLabel(companyUniqueId));
                    message = message.replaceAll("%employer%", employerName);
                    message = message.replaceAll("%founder%", founderName);
                    sender.sendMessage(Locale.translate(message));

                }

                return true;

            }

            if (args[0].equalsIgnoreCase("balance")) {

                UUID companyUniqueId = null;
                String companyName = "";
                int cid = 0;
                double balance = 0.0;
                double total_income = 0.0;
                double daily_income = 0.0;

                if (args.length == 1) {

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    if (!sender.hasPermission("mycompany.commands.balance")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    Player player = (Player) sender;
                    UUID uniqueId = player.getUniqueId();
                    companyUniqueId = userManager.getCompanyUUID(uniqueId);
                    // Check if the sender has company
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("no-company"));
                        return true;
                    }

                    companyName = companyManager.getName(companyUniqueId);
                    cid = companyManager.getId(companyUniqueId);
                    balance = companyManager.getCash(companyUniqueId);
                    total_income = companyManager.getTotalIncome(companyUniqueId);
                    daily_income = companyManager.getDailyIncome(companyUniqueId);

                }

                if (args.length == 2) {

                    if (!sender.hasPermission("mycompany.commands.balance.other")) {
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

                    companyName = companyManager.getName(companyUniqueId);
                    cid = companyManager.getId(companyUniqueId);
                    balance = companyManager.getCash(companyUniqueId);
                    total_income = companyManager.getTotalIncome(companyUniqueId);
                    daily_income = companyManager.getDailyIncome(companyUniqueId);

                }

                for (String message : Locale.yaml.getStringList("info.asset")) {
                    message = message.replaceAll("%company%", companyName);
                    message = message.replaceAll("%cid%", String.valueOf(cid));
                    message = message.replaceAll("%cash%", String.valueOf(balance));
                    message = message.replaceAll("%total%", String.valueOf(total_income));
                    message = message.replaceAll("%daily%", String.valueOf(daily_income));
                    message = Locale.translate(message);
                    sender.sendMessage(message);
                }
                return true;

            }

            if (args[0].equalsIgnoreCase("tp")) {

                UUID companyUniqueId = null;
                int cid = 0;
                Location location = null;

                if (!(sender instanceof Player)) {
                    sender.sendMessage(Locale.getMessage("must-be-player"));
                    return true;
                }
                Player player = (Player) sender;
                UUID playerUniqueId = player.getUniqueId();

                if (args.length == 1) {

                    if (!sender.hasPermission("mycompany.commands.tp")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                    // Check if the sender has company
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("no-company"));
                        return true;
                    }

                    location = buildingManager.getLocation(companyUniqueId);

                }

                if (args.length == 2) {

                    if (!sender.hasPermission("mycompany.commands.tp.other")) {
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

                    location = buildingManager.getLocation(companyUniqueId);

                }

                new PersonalAPI(playerUniqueId).teleportToCompany(location);
                return true;

            }

        }

        if (args.length >= 2 && args.length <= 3) {

            if (args[0].equalsIgnoreCase("area")) {

                if (args[1].equalsIgnoreCase("market")) {

                    String mode = "";

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    Player player = (Player) sender;
                    UUID playerUniqueId = player.getUniqueId();
                    UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                    // Check if the sender has company
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("no-company"));
                        return true;
                    }

                    if (args.length == 3) {
                        mode = args[2];
                    }


                    switch (mode) {
                        case "rent":
                            player.openInventory(new GUIAreaRentMarket(playerUniqueId, "rent", 1).getInventory());
                            break;
                        case "buy":
                            player.openInventory(new GUIAreaSaleMarket(playerUniqueId, "sell", 1).getInventory());
                            break;
                        default:
                            player.openInventory(new GUIAreaTradeMarket(playerUniqueId).getInventory());
                            break;
                    }

                    return true;

                }

            }

        }

        if (args.length == 3) {

            if (args[0].equalsIgnoreCase("area")) {

                if (args[1].equalsIgnoreCase("create")) {

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    if (!sender.hasPermission("mycompany.commands.area.create")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    Player player = (Player) sender;
                    UUID playerUniqueId = player.getUniqueId();
                    UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                    // Check if the sender has company
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("no-company"));
                        return true;
                    }

                    // Check if the sender has the permission
                    if (!userManager.hasPermission(playerUniqueId, Permission.AREA_CREATE)) {
                        sender.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.AREA_CREATE.getName()));
                        return true;
                    }

                    new PersonalAPI(playerUniqueId).createArea(args[2]);
                    return true;
                }

                if (args[1].equalsIgnoreCase("delete")) {

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    if (!sender.hasPermission("mycompany.commands.area.create")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    Player player = (Player) sender;
                    UUID playerUniqueId = player.getUniqueId();
                    UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                    // Check if the sender has company
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("no-company"));
                        return true;
                    }

                    // Check if the sender has the permission
                    if (!userManager.hasPermission(playerUniqueId, Permission.AREA_DELETE)) {
                        sender.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.AREA_DELETE.getName()));
                        return true;
                    }

                    new PersonalAPI(playerUniqueId).deleteArea(args[2]);
                    return true;

                }

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

            if (args[0].equalsIgnoreCase("order")) {

                if (args[1].equalsIgnoreCase("forcegive")) {

                    if (!isAdmin) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    // args[2] is player name
                    // args[3] is order name

                    OfflinePlayer off_target = Bukkit.getOfflinePlayer(args[2]);
                    UUID targetUniqueId = off_target.getUniqueId();
                    // Check if the target player is existed.
                    if (!off_target.hasPlayedBefore() || !userManager.isUserExist(targetUniqueId)) {
                        sender.sendMessage(Locale.getMessage("player-not-exist").replaceAll("%player%", args[1]));
                        return true;
                    }

                    String targetName = off_target.getName(); assert targetName != null;
                    if (!off_target.isOnline()) {
                        sender.sendMessage(Locale.getMessage("player-not-online").replaceAll("%player%", targetName));
                        return true;
                    }

                    UUID targetCompanyUniqueId = userManager.getCompanyUUID(targetUniqueId);
                    if (targetCompanyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("target-has-no-company").replaceAll("%player%", targetName));
                        return true;
                    }

                    if (!EmployeeOrders.getOrderList().contains(args[3])) {
                        sender.sendMessage(Locale.getMessage("order-not-exist").replaceAll("%order%", args[3]));
                        return true;
                    }

                    EmployeeOrders.acceptOrder(targetUniqueId, args[3]);
                    sender.sendMessage(Locale.getMessage("order-forcegive-success").replaceAll("%player%", args[2]).replaceAll("%order%", args[3]));
                    return true;

                }

            }

            if (args[0].equalsIgnoreCase("area")) {

                if (args[1].equalsIgnoreCase("tp")) {

                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    if (!sender.hasPermission("mycompany.commands.tp.other")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    int cid;

                    try {
                        cid = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        cid = -1;
                    }
                    if (cid < 0) {
                        sender.sendMessage(Locale.getMessage("id-invalid"));
                        return true;
                    }

                    UUID companyUniqueId = companyManager.getUUIDFromId(cid);
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("id-not-exist").replaceAll("%cid%", args[2]));
                        return true;
                    }

                    new PersonalAPI(player.getUniqueId()).teleportToLocation(companyUniqueId, args[3]);
                    return true;

                }

            }


        }

        // Position
        if (args.length >= 3 && args.length <= 5) {

            if (args[0].equalsIgnoreCase("position")) {

                if (args.length <= 4) {

                    if (args[1].equalsIgnoreCase("permission")) {

                        if (args[2].equalsIgnoreCase("list")) {

                            Collection<Permission> perms = Permission.getAll();
                            if (args.length == 4) {
                                if (!(sender instanceof Player)) {
                                    sender.sendMessage(Locale.getMessage("must-be-player"));
                                    return true;
                                }

                                if (!sender.hasPermission("mycompany.commands.position.permission.list")) {
                                    sender.sendMessage(Locale.getMessage("no-perm"));
                                    return true;
                                }

                                Player player = (Player) sender;
                                UUID uniqueId = player.getUniqueId();
                                UUID companyUniqueId = userManager.getCompanyUUID(uniqueId);
                                // Check if the sender has company
                                if (companyUniqueId == null) {
                                    sender.sendMessage(Locale.getMessage("no-company"));
                                    return true;
                                }

                                if (!userManager.getPosition(uniqueId).equalsIgnoreCase("employer")) {
                                    sender.sendMessage(Locale.getMessage("employer-only"));
                                    return true;
                                }

                                // args[3] = position, args[4] = permission

                                if (!companyManager.getPositions(companyUniqueId).contains(args[3])) {
                                    sender.sendMessage(Locale.getMessage("position-not-existed").replaceAll("%position%", args[3]));
                                    return true;
                                }

                                sender.sendMessage(Locale.getMessage("position-permission-list-position").replaceAll("%position%", args[3]));
                                perms = companyManager.getPositionPermission(companyUniqueId, args[3]);
                                for (Permission perm : perms) {
                                    String name = perm.getName();
                                    String message = Locale.getMessage("perm-list-page");
                                    message = message.replaceAll("%permission%", name);
                                    message = message.replaceAll("%label%", Locale.getMessage("position-permission." + name.toUpperCase()));
                                    sender.sendMessage(message);
                                }
                                return true;
                            }

                            sender.sendMessage(Locale.getMessage("position-permission-list"));
                            for (Permission perm : perms) {
                                String name = perm.getName();
                                String message = Locale.getMessage("perm-list-page");
                                message = message.replaceAll("%permission%", name);
                                message = message.replaceAll("%label%", Locale.getMessage("position-permission." + name.toUpperCase()));
                                sender.sendMessage(message);
                            }
                            return true;

                        }

                    }
                }

                if (args[1].equalsIgnoreCase("add")) {

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    if (!sender.hasPermission("mycompany.commands.position.add")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    Player player = (Player) sender;
                    UUID playerUniqueId = player.getUniqueId();
                    UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                    // Check if the sender has company
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("no-company"));
                        return true;
                    }

                    // Check if the sender has the permission
                    if (!userManager.hasPermission(playerUniqueId, Permission.POSITION_ADD)) {
                        sender.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.POSITION_ADD.getName()));
                        return true;
                    }

                    String LABEL = null;
                    if (args.length == 4) {
                        LABEL = args[3];
                    }
                    new PersonalAPI(playerUniqueId).addPosition(args[2], LABEL);
                    return true;

                }

                if (args[1].equalsIgnoreCase("remove")) {

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Locale.getMessage("must-be-player"));
                        return true;
                    }

                    if (!sender.hasPermission("mycompany.commands.position.remove")) {
                        sender.sendMessage(Locale.getMessage("no-perm"));
                        return true;
                    }

                    Player player = (Player) sender;
                    UUID playerUniqueId = player.getUniqueId();
                    UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                    // Check if the sender has company
                    if (companyUniqueId == null) {
                        sender.sendMessage(Locale.getMessage("no-company"));
                        return true;
                    }

                    // Check if the sender has the permission
                    if (!userManager.hasPermission(playerUniqueId, Permission.POSITION_REMOVE)) {
                        sender.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.POSITION_REMOVE.getName()));
                        return true;
                    }

                    new PersonalAPI(playerUniqueId).removePosition(args[2]);
                    return true;

                }

                if (args.length == 4) {

                    if (args[1].equalsIgnoreCase("setlabel")) {

                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Locale.getMessage("must-be-player"));
                            return true;
                        }

                        if (!sender.hasPermission("mycompany.commands.position.setlabel")) {
                            sender.sendMessage(Locale.getMessage("no-perm"));
                            return true;
                        }

                        Player player = (Player) sender;
                        UUID playerUniqueId = player.getUniqueId();
                        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);

                        // Check if the sender has company
                        if (companyUniqueId == null) {
                            sender.sendMessage(Locale.getMessage("no-company"));
                            return true;
                        }

                        // Check if the sender has the permission
                        if (!userManager.hasPermission(playerUniqueId, Permission.POSITION_SETLABEL)) {
                            sender.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.POSITION_SETLABEL.getName()));
                            return true;
                        }

                        new PersonalAPI(playerUniqueId).setPositionLabel(args[2], args[3]);
                        return true;

                    }

                    if (args[1].equalsIgnoreCase("setsalary")) {

                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Locale.getMessage("must-be-player"));
                            return true;
                        }

                        if (!sender.hasPermission("mycompany.commands.position.setsalary")) {
                            sender.sendMessage(Locale.getMessage("no-perm"));
                            return true;
                        }

                        Player player = (Player) sender;
                        UUID playerUniqueId = player.getUniqueId();
                        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);

                        // Check if the sender has company
                        if (companyUniqueId == null) {
                            sender.sendMessage(Locale.getMessage("no-company"));
                            return true;
                        }

                        if (!userManager.getPosition(playerUniqueId).equalsIgnoreCase("employer")) {
                            sender.sendMessage(Locale.getMessage("employer-only"));
                            return true;
                        }

                        new PersonalAPI(playerUniqueId).setSalary(args[2], args[3]);
                        return true;

                    }

                    if (args[1].equalsIgnoreCase("set")) {

                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Locale.getMessage("must-be-player"));
                            return true;
                        }

                        if (!sender.hasPermission("mycompany.commands.position.set")) {
                            sender.sendMessage(Locale.getMessage("no-perm"));
                            return true;
                        }

                        Player player = (Player) sender;
                        UUID playerUniqueId = player.getUniqueId();
                        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);

                        // Check if the sender has company
                        if (companyUniqueId == null) {
                            sender.sendMessage(Locale.getMessage("no-company"));
                            return true;
                        }

                        // Check if the sender has the permission
                        if (!userManager.hasPermission(playerUniqueId, Permission.SET_POSITION)) {
                            sender.sendMessage(Locale.getMessage("not-permitted").replaceAll("%permission%", Permission.SET_POSITION.getName()));
                            return true;
                        }

                        OfflinePlayer off_target = Bukkit.getOfflinePlayer(args[2]);
                        UUID targetUniqueId = off_target.getUniqueId();
                        new PersonalAPI(playerUniqueId).setEmployeePosition(targetUniqueId, args[2], args[3]);
                        return true;

                    }
                }

                if (args.length == 5) {

                    if (args[1].equalsIgnoreCase("permission")) {

                        if (args[2].equalsIgnoreCase("add")) {

                            if (!(sender instanceof Player)) {
                                sender.sendMessage(Locale.getMessage("must-be-player"));
                                return true;
                            }

                            if (!sender.hasPermission("mycompany.commands.position.permission.add")) {
                                sender.sendMessage(Locale.getMessage("no-perm"));
                                return true;
                            }

                            Player player = (Player) sender;
                            UUID uniqueId = player.getUniqueId();
                            UUID companyUniqueId = userManager.getCompanyUUID(uniqueId);
                            // Check if the sender has company
                            if (companyUniqueId == null) {
                                sender.sendMessage(Locale.getMessage("no-company"));
                                return true;
                            }

                            if (!userManager.getPosition(uniqueId).equalsIgnoreCase("employer")) {
                                sender.sendMessage(Locale.getMessage("employer-only"));
                                return true;
                            }

                            // args[3] = position, args[4] = permission

                            if (!companyManager.getPositions(companyUniqueId).contains(args[3])) {
                                sender.sendMessage(Locale.getMessage("position-not-existed").replaceAll("%position%", args[3]));
                                return true;
                            }

                            Permission perm = Permission.matchPermission(args[4]);
                            if (perm == null) {
                                sender.sendMessage(Locale.getMessage("position-permission-not-existed").replaceAll("%permission%", args[4]));
                                return true;
                            }

                            if (companyManager.getPositionPermission(companyUniqueId, args[3]).contains(perm)) {
                                sender.sendMessage(Locale.getMessage("position-has-permission").replaceAll("%permission%", args[4]).replaceAll("%position%", args[3]).replaceAll("%label%", companyManager.getPositionLabel(companyUniqueId, args[3])));
                                return true;
                            }

                            companyManager.addPositionPermission(companyUniqueId, args[3], perm);
                            sender.sendMessage(Locale.getMessage("position-permission-add").replaceAll("%permission%", args[4]).replaceAll("%position%", args[3]));
                            return true;

                        }

                        if (args[2].equalsIgnoreCase("remove")) {

                            if (!(sender instanceof Player)) {
                                sender.sendMessage(Locale.getMessage("must-be-player"));
                                return true;
                            }

                            if (!sender.hasPermission("mycompany.commands.position.permission.remove")) {
                                sender.sendMessage(Locale.getMessage("no-perm"));
                                return true;
                            }

                            Player player = (Player) sender;
                            UUID uniqueId = player.getUniqueId();
                            UUID companyUniqueId = userManager.getCompanyUUID(uniqueId);
                            // Check if the sender has company
                            if (companyUniqueId == null) {
                                sender.sendMessage(Locale.getMessage("no-company"));
                                return true;
                            }

                            if (!userManager.getPosition(uniqueId).equalsIgnoreCase("employer")) {
                                sender.sendMessage(Locale.getMessage("employer-only"));
                                return true;
                            }

                            // args[3] = position, args[4] = permission

                            if (!companyManager.getPositions(companyUniqueId).contains(args[3])) {
                                sender.sendMessage(Locale.getMessage("position-not-existed").replaceAll("%position%", args[3]));
                                return true;
                            }

                            Permission perm = Permission.matchPermission(args[4]);
                            if (perm == null) {
                                sender.sendMessage(Locale.getMessage("position-permission-not-existed").replaceAll("%permission%", args[4]));
                                return true;
                            }

                            if (!companyManager.getPositionPermission(companyUniqueId, args[3]).contains(perm)) {
                                sender.sendMessage(Locale.getMessage("position-has-no-permission").replaceAll("%permission%", args[4]).replaceAll("%position%", args[3]).replaceAll("%label%", companyManager.getPositionLabel(companyUniqueId, args[3])));
                                return true;
                            }

                            companyManager.removePositionPermission(companyUniqueId, args[3], perm);
                            sender.sendMessage(Locale.getMessage("position-permission-remove").replaceAll("%permission%", args[4]).replaceAll("%position%", args[3]));
                            return true;

                        }


                    }

                }

            }

        }

        if (args.length <= 2) {
            int helpPage = 1;

            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("?")) {
                    try {
                        helpPage = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    sender.sendMessage(Locale.getMessage("wrong-argument"));
                    return true;
                }
            }

            // Only show 7
            int max_arg = helpPage * 7;
            int show = 1;
            int current_commands = 7; //

            sender.sendMessage(Locale.getMessage("commands.top-bar"));
            sender.sendMessage(Locale.getMessage("commands.page").replaceAll("%page%", String.valueOf(helpPage)));
            sender.sendMessage(Locale.getMessage("commands.space-1"));
            for (String arg : Locale.yaml.getConfigurationSection("commands").getKeys(false)) {

                if (arg.equals("top-bar") || arg.equals("space-1") || arg.equals("space-2") || arg.equals("bottom-bar") || arg.equals("page")) {
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

            }

            for (String admin : Locale.yaml.getConfigurationSection("commands.admin").getKeys(false)) {

                if (!isAdmin) {
                    break;
                }

                if (show > max_arg) {
                    break;
                }
                sender.sendMessage(Locale.getMessage("commands.admin." + admin));
                show++;
            }

            sender.sendMessage(Locale.getMessage("commands.space-2"));
            sender.sendMessage(Locale.getMessage("commands.bottom-bar"));

            return true;
        }

        sender.sendMessage(Locale.getMessage("wrong-argument"));
        return true;
    }


}
