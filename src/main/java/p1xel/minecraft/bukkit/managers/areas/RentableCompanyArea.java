package p1xel.minecraft.bukkit.managers.areas;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class RentableCompanyArea extends CompanyArea {

//    public RentableCompanyArea(UUID companyUniqueId, String areaName, String world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Location firstBlock, Location secondBlock) {
//        super(companyUniqueId, areaName, world, minX, maxX, minY, maxY, minZ, maxZ, firstBlock, secondBlock);
//    }
//
//    public RentableCompanyArea(UUID companyUniqueId, String areaName) {
//        super(companyUniqueId, areaName);
//    }

    private UUID companyUniqueId;
    private String areaName;
    private long startTime;
    private long endTime;
    private UUID renterCompanyUniqueId;
    double rentPrice;

    public RentableCompanyArea(UUID companyUniqueId, String areaName, long startTime, long endTime, double rentPrice) {
        super(companyUniqueId, areaName);
        this.companyUniqueId = companyUniqueId;
        this.areaName = areaName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.rentPrice = rentPrice;
    }

    public RentableCompanyArea(UUID companyUniqueId, String areaName, long endTime) {
        super(companyUniqueId, areaName);
        this.companyUniqueId = companyUniqueId;
        this.areaName = areaName;
        this.endTime = endTime;
    }

    public RentableCompanyArea(UUID companyUniqueId, String areaName, long endTime, UUID renterCompanyUniqueId) {
        super(companyUniqueId, areaName);
        this.companyUniqueId = companyUniqueId;
        this.areaName = areaName;
        this.endTime = endTime;
        this.renterCompanyUniqueId = renterCompanyUniqueId;
    }

    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }

    public double getRentPrice() { return rentPrice; }

    @Nullable
    public UUID getRenter() { return renterCompanyUniqueId;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RentableCompanyArea that)) return false;
        return Objects.equals(companyUniqueId, that.getCompanyUUID()) &&
                Objects.equals(areaName, that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyUniqueId, areaName);
    }

}
