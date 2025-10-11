package p1xel.minecraft.bukkit.managers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import p1xel.minecraft.bukkit.MyCompany;
import p1xel.minecraft.bukkit.managers.hirerequests.HireRequest;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.storage.Locale;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class HireRequestManager {

    private HashMap<UUID, HireRequest> activeEmployerRequests = new HashMap<>();
    private Multimap<UUID, HireRequest> pendingPlayerRequests = ArrayListMultimap.create();

    public boolean sendRequest(UUID employerUniqueId, UUID playerUniqueId) {
        if (activeEmployerRequests.containsKey(employerUniqueId)) {
            // In cooldown
            return false;
        }

        // Create a request/contract
        HireRequest request = new HireRequest(employerUniqueId, playerUniqueId, System.currentTimeMillis(), null);

        // Task
        BukkitTask task = Bukkit.getScheduler().runTaskLater(MyCompany.getInstance(), () -> {
            expireRequest(request);
        }, 20L * Config.getInt("company-settings.hire-request-time")); // 30秒

        request.setExpirationTask(task);

        activeEmployerRequests.put(employerUniqueId, request);
        pendingPlayerRequests.put(playerUniqueId, request);
        return true;
    }

    public boolean canAccept(UUID playerUniqueId) {
        return pendingPlayerRequests.containsKey(playerUniqueId);
    }

    public boolean canAccept(UUID playerUniqueId, UUID employerUniqueId) {
        boolean acceptable = false;
        for (HireRequest request : pendingPlayerRequests.get(playerUniqueId)) {
            if (request.getEmployerUniqueId().equals(employerUniqueId)) {
                acceptable = true;
                break;
            }
        }
        return acceptable;
    }

    public void acceptRequest(UUID playerUniqueId, UUID companyUniqueId) {
        Collection<HireRequest> requests = pendingPlayerRequests.removeAll(playerUniqueId);
        for (HireRequest request : requests) {
            request.getExpirationTask().cancel();
            activeEmployerRequests.remove(request.getEmployerUniqueId());
        }

        MyCompany.getCacheManager().getCompanyManager().employPlayer(companyUniqueId, playerUniqueId, "employee");
    }

    private void expireRequest(HireRequest request) {
        UUID employerUniqueId = request.getEmployerUniqueId();
        activeEmployerRequests.remove(employerUniqueId);
        pendingPlayerRequests.remove(request.getPlayerUniqueId(), request);

        OfflinePlayer off_employer = Bukkit.getOfflinePlayer(employerUniqueId);
        String employerName = off_employer.getName();
        UUID playerUniqueId = request.getPlayerUniqueId();
        OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(playerUniqueId);
        String playerName = offlineplayer.getName();
        if (off_employer.isOnline()) {
            Player employer = (Player) off_employer;
            employer.sendMessage(Locale.getMessage("contract-expire-employer").replaceAll("%player%", playerName));
            employer.playSound(employer, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 3f);
        }

        if (offlineplayer.isOnline()) {
            Player player = (Player) offlineplayer;
            String companyName = MyCompany.getCacheManager().getCompanyManager().getName(MyCompany.getCacheManager().getUserManager().getCompanyUUID(employerUniqueId));
            player.sendMessage(Locale.getMessage("contract-expire-employee").replaceAll("%player%", employerName).replaceAll("%company%", companyName));
        }
    }
}
