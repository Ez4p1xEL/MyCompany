package p1xel.minecraft.bukkit.util.focus;

public class FixedFocusList {

    private final String[] focusList;

    public FixedFocusList() {
        this.focusList = new String[] {
                "none",
                "mob"
        };
    }

    public String[] getFocuses() {
        return focusList;
    }

}
