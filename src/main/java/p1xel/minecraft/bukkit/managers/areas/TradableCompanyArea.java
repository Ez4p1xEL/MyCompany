package p1xel.minecraft.bukkit.managers.areas;

import java.util.Objects;
import java.util.UUID;

public class TradableCompanyArea extends CompanyArea {

    private UUID companyUniqueId;
    private String areaName;
    double sellPrice;

    public TradableCompanyArea(UUID companyUniqueId, String areaName, double sellPrice) {
        super(companyUniqueId, areaName);
        this.companyUniqueId = companyUniqueId;
        this.areaName = areaName;
        this.sellPrice = sellPrice;
    }

    public double getSellPrice() { return sellPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradableCompanyArea that)) return false;
        return Objects.equals(companyUniqueId, that.getCompanyUUID()) &&
                Objects.equals(areaName, that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyUniqueId, areaName);
    }

}
