package p1xel.minecraft.bukkit.managers.areas;

import org.bukkit.scheduler.BukkitTask;

public class AreaRentTimer {

    private final RentableCompanyArea area;
    private final long timestamp; // 发送时间
    private BukkitTask expirationTask;

    public AreaRentTimer(RentableCompanyArea area, long timestamp, BukkitTask expirationTask) {
        this.area = area;
        this.timestamp = timestamp;
        this.expirationTask = expirationTask;
    }

    public BukkitTask getExpirationTask() {
        return this.expirationTask;
    }

    public RentableCompanyArea getRentableCompanyArea() {
        return this.area;
    }

    public long getTimeStamp() {
        return this.timestamp;
    }

    public void setExpirationTask(BukkitTask expirationTask) {
        this.expirationTask = expirationTask;
    }

}
