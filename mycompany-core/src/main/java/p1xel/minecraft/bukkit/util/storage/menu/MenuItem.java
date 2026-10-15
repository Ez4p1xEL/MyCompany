package p1xel.minecraft.bukkit.util.storage.menu;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemFlag;

import java.util.*;

public class MenuItem {

    private String id;
    private String materialString;
    private Material material;
    private List<Integer> slots = new ArrayList<>();
    private String displayName = "";
    private List<String> lore = new ArrayList<>();
    private String customKey = null; // Optional custom key for special handling
    private HashMap<Enchantment, Integer> enchantments = new HashMap<>();
    private HashSet<ItemFlag> itemFlags = new HashSet<>();

    public MenuItem(String id, String matStr, List<Integer> slots) {
        this.id = id;
        this.materialString = matStr;
        if (matStr.startsWith("PLAYER_HEAD;")) {
            this.material = Material.PLAYER_HEAD;
        } else {
            Material material = Material.matchMaterial(matStr);
            // Default to AIR if material is invalid
            this.material = Objects.requireNonNullElse(material, Material.AIR);
        }
        this.slots = slots;
    }

    public MenuItem(MenuItem menuItem) {
        this.id = menuItem.getId();
        this.materialString = menuItem.getMaterialString();
        this.material = menuItem.getMaterial();
        this.slots = new ArrayList<>(menuItem.getSlots());
        this.displayName = menuItem.getDisplayName();
        this.lore = new ArrayList<>(menuItem.getLore());
        this.customKey = menuItem.getCustomKey();
        this.enchantments = new HashMap<>(menuItem.getEnchantments());
        this.itemFlags = new HashSet<>(menuItem.getItemFlags());
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getMaterialString() { return materialString; }

    public Material getMaterial() {
        return material;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getCustomKey() { return customKey; }

    public HashMap<Enchantment, Integer> getEnchantments() { return enchantments; }

    public HashSet<ItemFlag> getItemFlags() { return itemFlags; }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey;
    }

    public void setSlots(List<Integer> slots) {
        this.slots = slots;
    }

    public void setMaterial(Material material) { this.material = material;}

    public void setMaterialString(String materialString) {
        this.materialString = materialString;
    }

    public MenuItem clone() {
        return new MenuItem(this);
    }

}
