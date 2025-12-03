package p1xel.minecraft.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.AreaManager;
import p1xel.minecraft.bukkit.managers.CompanyManager;
import p1xel.minecraft.bukkit.managers.UserManager;
import p1xel.minecraft.bukkit.utils.permissions.Permission;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TabList implements TabCompleter {

    private final CompanyManager companyManager = MyCompany.getCacheManager().getCompanyManager();
    private final UserManager userManager = MyCompany.getCacheManager().getUserManager();
    private final AreaManager areaManager = MyCompany.getCacheManager().getAreaManager();
    List<String> args0 = new ArrayList<>();
    List<String> perms = new ArrayList<>();
    List<String> bool = new ArrayList<>(Arrays.asList("true", "false"));
    @Override
    @ParametersAreNonnullByDefault
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        if (args0.isEmpty()) {
            args0.add("?"); args0.add("area"); args0.add("found"); args0.add("disband"); args0.add("employ"); args0.add("fire"); args0.add("accept");
            args0.add("resign"); args0.add("info"); args0.add("position"); args0.add("money"); args0.add("balance"); args0.add("open"); args0.add("tp");
            args0.add("setloc");
            if (sender.hasPermission("mycompany.commands.admin")) {
                args0.add("givetool");
                args0.add("order");
                args0.add("reload");
            }
        }

        if (perms.isEmpty()) {
            perms.addAll(Permission.getAllInString());
        }

        List<String> result0 = new ArrayList<>();
        if (args.length == 1) {
            for (String a : args0) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result0.add(a);
                }
            }
            return result0;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("employ") || args[0].equalsIgnoreCase("accept") || (args[0].equalsIgnoreCase("givetool") && sender.hasPermission("mycompany.commands.admin"))) {
                List<String> result = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String name = player.getName();
                    if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                        result.add(name);
                    }
                }
                return result;
            }

            if (args[0].equalsIgnoreCase("fire")) {
                List<String> result = new ArrayList<>();
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    UUID playerUniqueId = player.getUniqueId();
                    UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                    if (companyUniqueId != null) {
                        if (userManager.hasPermission(playerUniqueId, Permission.FIRE)) {
                            for (String position : companyManager.getPositions(companyUniqueId)) {
                                for (UUID employeeUniqueId : companyManager.getEmployeeList(companyUniqueId, position)) {
                                    OfflinePlayer off_player = Bukkit.getOfflinePlayer(employeeUniqueId);
                                    String name = off_player.getName();
                                    if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                                        result.add(name);
                                    }
                                }
                            }
                        }
                    }
                }
                return result;
            }

            if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("tp")) {
                List<String> result = new ArrayList<>();
                for (int id : companyManager.getCIds().keySet()) {
                    result.add(String.valueOf(id));
                }
                return result;
            }

            if (args[0].equalsIgnoreCase("position")) {
                return Arrays.asList("set", "add", "remove", "setlabel", "permission", "setsalary");
            }

            if (args[0].equalsIgnoreCase("money")) {
                if (!sender.hasPermission("mycompany.commands.admin")) {
                    return new ArrayList<>();
                }
                return Arrays.asList("give", "take");
            }

            if (args[0].equalsIgnoreCase("area")) {
                return Arrays.asList("create", "delete", "setloc", "tp", "info", "list", "market");
            }


        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("position")) {
                if (args[1].equalsIgnoreCase("set")) {
                    List<String> result = new ArrayList<>();
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        UUID playerUniqueId = player.getUniqueId();
                        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                        if (companyUniqueId != null) {
                            for (String position : companyManager.getPositions(companyUniqueId)) {
                                for (UUID employeeUniqueId : companyManager.getEmployeeList(companyUniqueId, position)) {
                                    OfflinePlayer off_player = Bukkit.getOfflinePlayer(employeeUniqueId);
                                    String name = off_player.getName();
                                    if (name.toLowerCase().startsWith(args[2].toLowerCase())) {
                                        result.add(name);
                                    }
                                }
                            }
                        }
                    }
                    return result;
                }

                if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("setlabel") || args[1].equalsIgnoreCase("setsalary")) {
                    List<String> result = new ArrayList<>();
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        UUID playerUniqueId = player.getUniqueId();
                        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                        if (companyUniqueId != null) {
                            for (String position : companyManager.getPositions(companyUniqueId)) {
                                if (position.toLowerCase().startsWith(args[2].toLowerCase())) {
                                    result.add(position);
                                }
                            }
                        }
                    }
                    return result;
                }

                if (args[1].equalsIgnoreCase("add")) {
                    return new ArrayList<>();
                }

                if (args[1].equalsIgnoreCase("permission")) {
                    return Arrays.asList("add", "remove", "list");
                }

            }

            if (args[0].equalsIgnoreCase("money")) {

                if (!sender.hasPermission("mycompany.commands.admin")) {
                    return new ArrayList<>();
                }

                if (args[1].equalsIgnoreCase("take") ||  args[1].equalsIgnoreCase("give")) {
                    List<String> result = new ArrayList<>();
                    for (int id : companyManager.getCIds().keySet()) {
                        result.add(String.valueOf(id));
                    }
                    return result;
                }
            }

            if (args[0].equalsIgnoreCase("area")) {

                if (args[1].equalsIgnoreCase("tp")) {

                    List<String> result = new ArrayList<>();
                    for (int id : companyManager.getCIds().keySet()) {
                        result.add(String.valueOf(id));
                    }
                    return result;

                }

                if (args[1].equalsIgnoreCase("create")) {
                    return new ArrayList<>();
                }

                if (args[1].equalsIgnoreCase("delete")) {
                    List<String> result = new ArrayList<>();
                    if (sender instanceof Player player) {
                        UUID playerUniqueId = player.getUniqueId();
                        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                        if (companyUniqueId != null) {
                            result.addAll(areaManager.getAreas(companyUniqueId));
                        }
                    }
                    return result;
                }

                if (args[1].equalsIgnoreCase("market")) {
                    return Arrays.asList("", "buy", "rent");
                }

            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("position")) {
                if (args[1].equalsIgnoreCase("set")) {
                    List<String> result = new ArrayList<>();
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        UUID playerUniqueId = player.getUniqueId();
                        UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                        if (companyUniqueId != null) {
                            for (String position : companyManager.getPositions(companyUniqueId)) {
                                if (position.toLowerCase().startsWith(args[2].toLowerCase())) {
                                    result.add(position);
                                }
                            }
                        }
                    }
                    return result;
                }

                if (args[1].equalsIgnoreCase("permission")) {
                    if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove") || args[2].equalsIgnoreCase("list")) {

                        List<String> result = new ArrayList<>();
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            UUID playerUniqueId = player.getUniqueId();
                            UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                            if (companyUniqueId != null) {
                                for (String position : companyManager.getPositions(companyUniqueId)) {
                                    if (position.toLowerCase().startsWith(args[3].toLowerCase())) {
                                        result.add(position);
                                    }
                                }
                            }
                        }
                        return result;


                    }
                }
            }

            if (args[0].equalsIgnoreCase("area")) {

                if (args[1].equalsIgnoreCase("tp")) {
                    List<String> result = new ArrayList<>();
                    int cid;
                    try {
                        cid = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        return result;
                    }
                    UUID companyUniqueId = companyManager.getUUIDFromId(cid);
                    if (companyUniqueId != null) {
                        result.addAll(areaManager.getAreas(companyUniqueId));
                    }
                    return result;
                }

            }

            if (args[0].equalsIgnoreCase("position")) {
                if (args[1].equalsIgnoreCase("setsalary")) {
                    return new ArrayList<>();
                }
            }

            if (args[0].equalsIgnoreCase("money")) {
                if (args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("take")) {
                    return new ArrayList<>();
                }
            }
        }

        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("position")) {
                if (args[1].equalsIgnoreCase("permission")) {
                    if (args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove")) {

                        List<String> result = new ArrayList<>();
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            UUID playerUniqueId = player.getUniqueId();
                            UUID companyUniqueId = userManager.getCompanyUUID(playerUniqueId);
                            if (companyUniqueId != null) {
                                for (String permission : Permission.getAllInString()) {
                                    if (permission.toLowerCase().startsWith(args[4].toLowerCase())) {
                                        result.add(permission);
                                    }
                                }
                            }
                        }
                        return result;

                    }
                }
            }
        }

        return new ArrayList<>();
    }

}
