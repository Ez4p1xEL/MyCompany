package p1xel.minecraft.bukkit.object.action;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import p1xel.minecraft.bukkit.object.Company;
import p1xel.minecraft.bukkit.object.User;

import java.util.List;
import java.util.UUID;

public class ConsoleCommandAction extends Action {

    private final List<String> commands;
    private final JavaPlugin plugin;
    private final ConsoleCommandSender consoleSender;

    public ConsoleCommandAction(List<String> commands, JavaPlugin plugin) {
        super(ActionType.CONSOLE_COMMAND);
        this.commands = commands;
        this.plugin = plugin;
        this.consoleSender = plugin.getServer().getConsoleSender();
    }

    public List<String> getCommands() {
        return commands;
    }

    @Override
    public boolean execute(Company company, User user) {
        for (String cmd : commands) {
            boolean isSendToAllPersonnel = cmd.contains("%player%");
            if (isSendToAllPersonnel) {
                for (String position : company.getPositions()) {
                    for (UUID personnelUUID : company.getEmployeeList(position)) {
                        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(personnelUUID);
                        String modifiedCmd = cmd.replace("%player%", offlinePlayer.getName());
                        plugin.getServer().dispatchCommand(consoleSender, modifiedCmd);
                    }
                }
                continue;
            }

            plugin.getServer().dispatchCommand(consoleSender, cmd);

        }

        return true;
    }

}