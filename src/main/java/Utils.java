import java.util.*;
import java.util.stream.Collectors;

public enum Utils {
    ;

    static Random random = new Random();

    public static int randInt(final int max) {
        return Utils.randInt(0, max);
    }

    public static int randInt(final int min, final int max) {
        return Utils.randInt((double) min, (double) max);
    }

    public static int randInt(final double min, final double max) {
        return (int) Math.floor(Utils.random.nextDouble() * (max - min) + min);
    }

    public static double randDouble(final double min, final double max) {
        return Utils.random.nextDouble() * (max - min) + min;
    }

    public static <T> T getRandomElem(final T[] arr) {
        if (arr.length == 0) {
            return null;
        }
        return arr[Utils.randInt(arr.length)];
    }

    public static <T> T getRandomElem(final List<T> arr) {
        if (arr.size() == 0) {
            return null;
        }
        return arr.get(Utils.randInt(arr.size()));
    }

    public static <T> ArrayList<T> toList(final T[] input) {
        final ArrayList<T> out = new ArrayList<>();
        Collections.addAll(out, input);
        return out;
    }

    public static ArrayList<Double> toList(final double[] input) {
        return Arrays.stream(input).boxed().collect(Collectors.toCollection(ArrayList::new));
    }
}
