package p1xel.minecraft.bukkit.object.math;

public enum NumberType {


    ADD("add", "+"),
    MULTIPLY("multiply", "*");

    private final String id;
    private final String symbol;
    NumberType(String id, String symbol) {
        this.id = id;
        this.symbol = symbol;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }
}
