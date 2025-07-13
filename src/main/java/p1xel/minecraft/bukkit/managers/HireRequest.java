package p1xel.minecraft.bukkit.managers;

import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class HireRequest {

    private final UUID employerUniqueId;
    private final UUID playerUniqueId; // target player
    private final long timestamp; // 发送时间
    private BukkitTask expirationTask;

    public HireRequest(UUID employerUniqueId, UUID playerUniqueId, long timestamp, BukkitTask expirationTask) {
        this.employerUniqueId = employerUniqueId;
        this.playerUniqueId = playerUniqueId;
        this.timestamp = timestamp;
        this.expirationTask = expirationTask;
    }

    public BukkitTask getExpirationTask() {
        return this.expirationTask;
    }

    public UUID getEmployerUniqueId() {
        return this.employerUniqueId;
    }

    public UUID getPlayerUniqueId() {
        return this.playerUniqueId;
    }

    public long getTimeStamp() {
        return this.timestamp;
    }

    public void setExpirationTask(BukkitTask expirationTask) {
        this.expirationTask = expirationTask;
    }

}
