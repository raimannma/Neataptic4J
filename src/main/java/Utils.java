import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class Utils {
    public static ArrayList<Double> toList(final double[] input) {
        return Arrays.stream(input).boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    public static <T> ArrayList<T> toList(final T[] input) {
        final ArrayList<T> out = new ArrayList<>();
        Collections.addAll(out, input);
        return out;
    }

}
