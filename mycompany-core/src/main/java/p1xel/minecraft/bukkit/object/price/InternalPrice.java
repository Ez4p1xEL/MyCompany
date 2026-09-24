package p1xel.minecraft.bukkit.object.price;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class InternalPrice {

    private final String id;
    private final boolean vanillaItem;
    private final Material material;
    private final ItemStack itemStack;
    private final double price;
    private final PriceGroup priceGroup;

    public InternalPrice(String id, boolean vanillaItem, ItemStack itemStack, double price, PriceGroup priceGroup) {
        this.id = id;
        this.vanillaItem = vanillaItem;
        this.material = itemStack.getType();
        this.itemStack = itemStack.clone();
        this.price = price;
        this.priceGroup = priceGroup;
    }

    public String getId() {
        return id;
    }

    public boolean isVanillaItem() {
        return vanillaItem;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getPrice() {
        return price;
    }

    public PriceGroup getPriceGroup() {
        return priceGroup;
    }

}
