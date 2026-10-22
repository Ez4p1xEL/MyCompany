package p1xel.minecraft.bukkit.object.price;

public class PriceGroup {

    private final String id;
    private final double priceMultiplier;

    public PriceGroup(String id, double priceMultiplier) {
        this.id = id;
        this.priceMultiplier = priceMultiplier;
    }

    public String getId() {
        return id;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

}
