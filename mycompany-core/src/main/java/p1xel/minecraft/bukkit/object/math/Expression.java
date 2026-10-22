package p1xel.minecraft.bukkit.object.math;

public class Expression {

    public static double evaluate(double value1, double value2, NumberType numberType) {

        switch (numberType) {
            case ADD:
                return value1 + value2;
            case MULTIPLY:
                return value1 * value2;
            default:
                throw new IllegalArgumentException("Unsupported number type: " + numberType);
        }

    }

}
